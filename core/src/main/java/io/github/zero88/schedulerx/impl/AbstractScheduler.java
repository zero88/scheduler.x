package io.github.zero88.schedulerx.impl;

import static io.github.zero88.schedulerx.impl.Utils.brackets;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.ExecutionContext;
import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.JobExecutor;
import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.TimeoutBlock;
import io.github.zero88.schedulerx.TimeoutPolicy;
import io.github.zero88.schedulerx.WorkerExecutorFactory;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.github.zero88.schedulerx.trigger.TriggerCondition.ReasonCode;
import io.github.zero88.schedulerx.trigger.TriggerCondition.TriggerStatus;
import io.github.zero88.schedulerx.trigger.TriggerContext;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * The base scheduler
 *
 * @param <IN>  Type of job input data
 * @param <OUT> Type of output data
 * @param <T>   Type of trigger
 */
@Internal
public abstract class AbstractScheduler<IN, OUT, T extends Trigger> implements Scheduler<IN, OUT, T>, JobExecutor<OUT> {

    @SuppressWarnings("java:S3416")
    protected static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    private final @NotNull Vertx vertx;
    private final @NotNull SchedulerStateInternal<OUT> state;
    private final @NotNull SchedulingMonitor<OUT> monitor;
    private final @NotNull JobData<IN> jobData;
    private final @NotNull Job<IN, OUT> job;
    private final @NotNull T trigger;
    private final @NotNull TimeoutPolicy timeoutPolicy;
    private final Lock lock = new ReentrantLock();
    private boolean didStart = false;
    private boolean didTriggerValidation = false;
    private IllegalArgumentException invalidTrigger;

    protected AbstractScheduler(@NotNull Vertx vertx, @NotNull SchedulingMonitor<OUT> monitor,
                                @NotNull JobData<IN> jobData, @NotNull Job<IN, OUT> job, @NotNull T trigger,
                                @NotNull TimeoutPolicy timeoutPolicy) {
        this.vertx         = vertx;
        this.trigger       = trigger;
        this.monitor       = monitor;
        this.jobData       = jobData;
        this.job           = job;
        this.timeoutPolicy = timeoutPolicy;
        this.state         = new SchedulerStateImpl<>();
    }

    @Override
    public final @NotNull Vertx vertx() { return this.vertx; }

    @Override
    public final @NotNull SchedulingMonitor<OUT> monitor() { return this.monitor; }

    @Override
    public final @NotNull JobData<IN> jobData() { return this.jobData; }

    @Override
    public final @NotNull Job<IN, OUT> job() { return this.job; }

    @Override
    public @NotNull TimeoutPolicy timeoutPolicy() { return this.timeoutPolicy; }

    @Override
    @SuppressWarnings({ "java:S1193", "unchecked" })
    public final @NotNull T trigger() {
        lock.lock();
        try {
            if (didTriggerValidation) {
                if (invalidTrigger == null) { return trigger; }
                throw invalidTrigger;
            }
            try {
                return (T) this.trigger.validate();
            } catch (Exception ex) {
                if (ex instanceof IllegalArgumentException) {
                    this.invalidTrigger = (IllegalArgumentException) ex;
                } else {
                    this.invalidTrigger = new IllegalArgumentException(
                        "Encounter an unexpected exception when validating trigger", ex);
                }
                throw this.invalidTrigger;
            } finally {
                didTriggerValidation = true;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final void start(WorkerExecutor workerExecutor) {
        lock.lock();
        try {
            if (didStart) {
                throw new IllegalStateException("The executor is already started!");
            }
            doStart(workerExecutor == null ? WorkerExecutorFactory.create(vertx, timeoutPolicy) : workerExecutor);
            didStart = true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final void executeJob(@NotNull ExecutionContext<OUT> executionContext) {
        try {
            log(executionContext.executedAt(), "On execute");
            job.execute(jobData(), executionContext);
            if (!job.isAsync()) {
                ((ExecutionContextInternal<OUT>) executionContext).internalComplete();
            }
        } catch (Exception ex) {
            executionContext.fail(ex);
        }
    }

    @Override
    public final void cancel() {
        if (!state.completed()) {
            log(Instant.now(), "On cancel");
            doStop(state.timerId(), TriggerContextFactory.stop(trigger().type(), ReasonCode.STOP_BY_MANUAL));
        }
    }

    protected final void doStart(WorkerExecutor workerExecutor) {
        this.registerTimer(workerExecutor).onSuccess(this::onSchedule).onFailure(this::onUnableSchedule);
    }

    protected final void doStop(long timerId, TriggerContext context) {
        unregisterTimer(timerId);
        onComplete(context);
    }

    /**
     * Register a new timer in the system based on the trigger configuration
     */
    protected abstract @NotNull Future<Long> registerTimer(@Nullable WorkerExecutor workerExecutor);

    /**
     * Unregister current timer id out of the system
     */
    protected abstract void unregisterTimer(long timerId);

    /**
     * Check a trigger kickoff context whether to be able to run new execution or not
     */
    protected final TriggerTransitionContext shouldRun(@NotNull TriggerTransitionContext kickOffContext) {
        log(Instant.now(), "On evaluate");
        if (state.pending()) {
            return TriggerContextFactory.skip(kickOffContext, ReasonCode.NOT_YET_SCHEDULED);
        }
        if (state.completed()) {
            return TriggerContextFactory.skip(kickOffContext, ReasonCode.ALREADY_STOPPED);
        }
        if (state.executing()) {
            return TriggerContextFactory.skip(kickOffContext, ReasonCode.JOB_IS_RUNNING);
        }
        return evaluateTriggerRule(kickOffContext);
    }

    /**
     * Check a trigger context whether to be able to stop by configuration or force stop
     */
    protected final TriggerTransitionContext shouldStop(@NotNull TriggerTransitionContext triggerContext,
                                                        boolean isForceStop, long round) {
        if (isForceStop) {
            return TriggerContextFactory.stop(triggerContext, ReasonCode.STOP_BY_JOB);
        }
        return trigger().shouldStop(round)
               ? TriggerContextFactory.stop(triggerContext, ReasonCode.STOP_BY_CONFIG)
               : triggerContext;
    }

    /**
     * Evaluate a trigger kickoff context on trigger rule
     */
    protected TriggerTransitionContext evaluateTriggerRule(@NotNull TriggerTransitionContext triggerContext) {
        if (!triggerContext.isKickoff()) {
            throw new IllegalStateException("Trigger condition status must be " + TriggerStatus.KICKOFF);
        }
        final Instant firedAt = Objects.requireNonNull(triggerContext.firedAt(),
                                                       "Kickoff context is missing a fired at time");
        if (trigger().rule().isExceeded(firedAt)) {
            return TriggerContextFactory.stop(triggerContext, ReasonCode.STOP_BY_CONFIG);
        }
        return trigger().shouldExecute(firedAt)
               ? TriggerContextFactory.ready(triggerContext)
               : TriggerContextFactory.skip(triggerContext, ReasonCode.CONDITION_IS_NOT_MATCHED);
    }

    /**
     * Register a timer id in internal state and increase tick time when the system timer fires
     *
     * @param timerId the system timer id
     * @return the current number times of the tick counter
     */
    protected final long onFire(long timerId) {
        return state.timerId(timerId).increaseTick();
    }

    /**
     * Processing the trigger when the system timer fires
     */
    protected final void onProcess(WorkerExecutor workerExecutor, TriggerTransitionContext kickoffContext) {
        log(Objects.requireNonNull(kickoffContext.firedAt()), "On fire");
        this.<TriggerTransitionContext>executeBlocking(workerExecutor,
                                                       p -> wrapTimeout(timeoutPolicy().evaluationTimeout(),
                                                                        p).complete(shouldRun(kickoffContext)))
            .onSuccess(context -> onTrigger(workerExecutor, context))
            .onFailure(t -> onMisfire(TriggerContextFactory.skip(kickoffContext, t instanceof TimeoutException
                                                                                 ? ReasonCode.EVALUATION_TIMEOUT
                                                                                 : ReasonCode.UNEXPECTED_ERROR, t)));
    }

    protected final void onTrigger(WorkerExecutor workerExecutor, TriggerTransitionContext triggerContext) {
        if (!triggerContext.isReady()) {
            onMisfire(triggerContext);
            return;
        }
        final ExecutionContextInternal<OUT> executionContext = new ExecutionContextImpl<>(vertx, triggerContext,
                                                                                          state.increaseRound());
        log(executionContext.triggeredAt(), "On trigger", triggerContext.tick(), executionContext.round());
        this.executeBlocking(workerExecutor, p -> executeJob(
                executionContext.setup(wrapTimeout(timeoutPolicy().executionTimeout(), p))))
            .onComplete(ar -> onResult(executionContext, ar.cause()));
    }

    protected final void onSchedule(long timerId) {
        final TriggerContext context = TriggerContextFactory.scheduled(trigger().type());
        ExecutionResult<OUT> result;
        if (state.pending()) {
            result = ExecutionResultImpl.<OUT>builder()
                                        .setExternalId(jobData.externalId())
                                        .setAvailableAt(state.markAvailable())
                                        .setTriggerContext(context)
                                        .build();
        } else {
            result = ExecutionResultImpl.<OUT>builder()
                                        .setExternalId(jobData.externalId())
                                        .setAvailableAt(state.availableAt())
                                        .setTriggerContext(context)
                                        .setRescheduledAt(Instant.now())
                                        .setTick(state.tick())
                                        .setRound(state.round())
                                        .build();
        }
        monitor.onSchedule(result);
    }

    protected final void onUnableSchedule(Throwable cause) {
        final TriggerContext ctx = TriggerContextFactory.error(trigger.type(), ReasonCode.FAILED_TO_SCHEDULE, cause);
        monitor.onUnableSchedule(ExecutionResultImpl.<OUT>builder()
                                                    .setExternalId(jobData.externalId())
                                                    .setTriggerContext(ctx)
                                                    .setUnscheduledAt(Instant.now())
                                                    .setTick(-1)
                                                    .setRound(-1)
                                                    .build());
    }

    protected final void onMisfire(@NotNull TriggerTransitionContext triggerContext) {
        final Instant finishedAt = state.markFinished(triggerContext.tick());
        log(finishedAt, "On misfire::" + triggerContext.condition().reasonCode(), triggerContext.tick());
        monitor.onMisfire(ExecutionResultImpl.<OUT>builder()
                                             .setExternalId(jobData.externalId())
                                             .setAvailableAt(state.availableAt())
                                             .setTriggerContext(triggerContext)
                                             .setTick(triggerContext.tick())
                                             .setFiredAt(triggerContext.firedAt())
                                             .setRound(state.round())
                                             .setFinishedAt(finishedAt)
                                             .build());
    }

    protected final void onResult(@NotNull ExecutionContext<OUT> executionContext, @Nullable Throwable asyncCause) {
        final ExecutionContextInternal<OUT> ctx = (ExecutionContextInternal<OUT>) executionContext;
        final TriggerTransitionContext triggerContext = TriggerContextFactory.executed(ctx.triggerContext());
        final Instant finishedAt = state.markFinished(triggerContext.tick());
        log(finishedAt, "On result", triggerContext.tick(), ctx.round());
        if (asyncCause instanceof TimeoutException) {
            LOGGER.warn(genMsg(state.tick(), state.round(), finishedAt, asyncCause.getMessage()));
        } else if (asyncCause != null) {
            LOGGER.error(genMsg(triggerContext.tick(), ctx.round(), finishedAt, "System error"), asyncCause);
        }
        monitor.onEach(ExecutionResultImpl.<OUT>builder()
                                          .setExternalId(jobData.externalId())
                                          .setAvailableAt(state.availableAt())
                                          .setTriggerContext(triggerContext)
                                          .setTick(triggerContext.tick())
                                          .setFiredAt(triggerContext.firedAt())
                                          .setRound(ctx.round())
                                          .setTriggeredAt(ctx.triggeredAt())
                                          .setExecutedAt(ctx.executedAt())
                                          .setFinishedAt(finishedAt)
                                          .setData(state.addData(ctx.round(), ctx.data()))
                                          .setError(state.addError(ctx.round(),
                                                                   Optional.ofNullable(ctx.error()).orElse(asyncCause)))
                                          .build());
        final TriggerTransitionContext transitionCtx = shouldStop(triggerContext, ctx.isForceStop(), ctx.round());
        if (transitionCtx.isStopped()) {
            doStop(state.timerId(), transitionCtx);
        }
    }

    protected final void onComplete(TriggerContext context) {
        final Instant completedAt = state.markCompleted();
        log(completedAt, "On complete");
        monitor.onCompleted(ExecutionResultImpl.<OUT>builder()
                                               .setExternalId(jobData.externalId())
                                               .setAvailableAt(state.availableAt())
                                               .setTriggerContext(context)
                                               .setTick(state.tick())
                                               .setRound(state.round())
                                               .setCompletedAt(completedAt)
                                               .setData(state.lastData())
                                               .setError(state.lastError())
                                               .build());
    }

    protected final void log(@NotNull Instant at, @NotNull String event) {
        log(at, event, state.tick(), state.round());
    }

    protected final void log(@NotNull Instant at, @NotNull String event, long tick) {
        log(at, event, tick, state.round());
    }

    protected final void log(@NotNull Instant at, @NotNull String event, long tick, long round) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(genMsg(tick, round, at, event));
        }
    }

    private String genMsg(long tick, long round, @NotNull Instant at, @NotNull String event) {
        String tId = state.pending() ? "-" : String.valueOf(state.timerId());
        String tickAndRound = state.pending() ? "-/-" : (tick + "/" + round);
        return MessageFormat.format("Scheduling{0}{1}::{2} - {3}",
                                    brackets(tId + "::" + trigger.type() + "::" + jobData.externalId()),
                                    brackets(tickAndRound), brackets(at), event);
    }

    private <R> Future<R> executeBlocking(WorkerExecutor workerExecutor, Consumer<Promise<R>> operation) {
        return workerExecutor == null
               ? vertx.executeBlocking(operation::accept, false)
               : workerExecutor.executeBlocking(operation::accept, false);
    }

    private <R> Promise<R> wrapTimeout(Duration timeout, Promise<R> promise) {
        return new TimeoutBlock(vertx, timeout).wrap(promise);
    }

}
