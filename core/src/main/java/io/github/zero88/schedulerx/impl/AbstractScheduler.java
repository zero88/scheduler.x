package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.ExecutionContext;
import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.github.zero88.schedulerx.trigger.TriggerCondition.ReasonCode;
import io.github.zero88.schedulerx.trigger.TriggerCondition.TriggerStatus;
import io.github.zero88.schedulerx.trigger.TriggerContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * The base scheduler
 *
 * @param <IN>  Type of task input data
 * @param <OUT> Type of output data
 * @param <T>   Type of trigger
 */
@Internal
public abstract class AbstractScheduler<IN, OUT, T extends Trigger> implements Scheduler<IN, OUT, T> {

    @SuppressWarnings("java:S3416")
    protected static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    private final @NotNull Vertx vertx;
    private final @NotNull SchedulerStateInternal<OUT> state;
    private final @NotNull SchedulingMonitor<OUT> monitor;
    private final @NotNull JobData<IN> jobData;
    private final @NotNull Task<IN, OUT> task;
    private final @NotNull T trigger;
    private final Lock lock = new ReentrantLock();
    private boolean didStart = false;
    private boolean didTriggerValidation = false;
    private IllegalArgumentException invalidTrigger;

    protected AbstractScheduler(@NotNull Vertx vertx, @NotNull SchedulingMonitor<OUT> monitor,
                                @NotNull JobData<IN> jobData, @NotNull Task<IN, OUT> task, @NotNull T trigger) {
        this.vertx   = vertx;
        this.monitor = monitor;
        this.jobData = jobData;
        this.task    = task;
        this.trigger = trigger;
        this.state   = new SchedulerStateImpl<>();
    }

    @Override
    public final @NotNull Vertx vertx() { return this.vertx; }

    @Override
    public final @NotNull SchedulingMonitor<OUT> monitor() { return this.monitor; }

    @Override
    public final @NotNull JobData<IN> jobData() { return this.jobData; }

    @Override
    public final @NotNull Task<IN, OUT> task() { return this.task; }

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
            doStart(workerExecutor);
            didStart = true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final void executeTask(@NotNull ExecutionContext<OUT> executionContext) {
        try {
            log(executionContext.executedAt(), "On execute");
            task.execute(jobData(), executionContext);
            if (!task.isAsync()) {
                ((ExecutionContextInternal<OUT>) executionContext).internalComplete();
            }
        } catch (Exception ex) {
            executionContext.fail(ex);
        }
    }

    @Override
    public final void cancel() {
        if (!state.completed()) {
            log(Instant.now(), "Canceling the task");
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

    protected abstract @NotNull Future<Long> registerTimer(@Nullable WorkerExecutor workerExecutor);

    protected abstract void unregisterTimer(long timerId);

    protected final TriggerTransitionContext shouldRun(@NotNull TriggerTransitionContext triggerContext) {
        log(Instant.now(), "On evaluate");
        if (!triggerContext.isKickoff()) {
            throw new IllegalStateException("Trigger condition status must be " + TriggerStatus.KICKOFF);
        }
        if (state.pending()) {
            return TriggerContextFactory.skip(triggerContext, ReasonCode.NOT_YET_SCHEDULED);
        }
        if (state.completed()) {
            return TriggerContextFactory.skip(triggerContext, ReasonCode.ALREADY_STOPPED);
        }
        if (state.executing()) {
            return TriggerContextFactory.skip(triggerContext, ReasonCode.TASK_IS_RUNNING);
        }
        return evaluateTrigger(triggerContext);
    }

    protected final TriggerTransitionContext shouldStop(@NotNull TriggerTransitionContext triggerContext,
                                                        boolean isForceStop, long round) {
        if (isForceStop) {
            return TriggerContextFactory.stop(triggerContext, ReasonCode.STOP_BY_TASK);
        }
        return trigger().shouldStop(round)
               ? TriggerContextFactory.stop(triggerContext, ReasonCode.STOP_BY_CONFIG)
               : triggerContext;
    }

    protected TriggerTransitionContext evaluateTrigger(@NotNull TriggerTransitionContext triggerContext) {
        final Instant firedAt = Objects.requireNonNull(triggerContext.firedAt());
        if (trigger().rule().isExceeded(firedAt)) {
            return TriggerContextFactory.stop(triggerContext, ReasonCode.STOP_BY_CONFIG);
        }
        return trigger().shouldExecute(firedAt)
               ? TriggerContextFactory.ready(triggerContext)
               : TriggerContextFactory.skip(triggerContext, ReasonCode.CONDITION_IS_NOT_MATCHED);
    }

    /**
     * Register a timer id and increase tick time when the system timer fires
     *
     * @param timerId the system timer id
     * @return the current number times of the tick counter
     */
    protected final long onFire(long timerId) {
        return state.timerId(timerId).increaseTick();
    }

    protected final void onRun(WorkerExecutor workerExecutor, TriggerTransitionContext kickoffContext) {
        log(Objects.requireNonNull(kickoffContext.firedAt()), "On fire");
        this.<TriggerTransitionContext>executeBlocking(workerExecutor, p -> p.complete(shouldRun(kickoffContext)))
            .onSuccess(context -> onTrigger(workerExecutor, context))
            .onFailure(t -> onMisfire(TriggerContextFactory.skip(kickoffContext, ReasonCode.UNEXPECTED_ERROR, t)));
    }

    protected final void onTrigger(WorkerExecutor workerExecutor, TriggerTransitionContext triggerContext) {
        if (!triggerContext.isReady()) {
            onMisfire(triggerContext);
            return;
        }
        final ExecutionContextInternal<OUT> executionContext = new ExecutionContextImpl<>(vertx, triggerContext,
                                                                                          state.increaseRound());
        log(executionContext.triggeredAt(), "On trigger", triggerContext.tick(), executionContext.round());
        this.executeBlocking(workerExecutor, p -> executeTask(executionContext.setup(p)))
            .onComplete(ar -> onResult(triggerContext, ar));
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
                                                    .setTick(state.tick())
                                                    .setRound(state.round())
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

    @SuppressWarnings("unchecked")
    protected final void onResult(@NotNull TriggerTransitionContext triggerContext,
                                  @NotNull AsyncResult<Object> asyncResult) {
        final Instant finishedAt = state.markFinished(triggerContext.tick());
        TriggerTransitionContext transitionCtx;
        if (asyncResult.succeeded()) {
            final ExecutionContextInternal<OUT> executionCtx = (ExecutionContextInternal<OUT>) asyncResult.result();
            log(finishedAt, "On result", triggerContext.tick(), executionCtx.round());
            monitor.onEach(ExecutionResultImpl.<OUT>builder()
                                              .setExternalId(jobData.externalId())
                                              .setAvailableAt(state.availableAt())
                                              .setTriggerContext(triggerContext)
                                              .setTick(triggerContext.tick())
                                              .setFiredAt(triggerContext.firedAt())
                                              .setRound(executionCtx.round())
                                              .setTriggeredAt(executionCtx.triggeredAt())
                                              .setExecutedAt(executionCtx.executedAt())
                                              .setFinishedAt(finishedAt)
                                              .setData(state.addData(executionCtx.round(), executionCtx.data()))
                                              .setError(state.addError(executionCtx.round(), executionCtx.error()))
                                              .build());
            transitionCtx = shouldStop(triggerContext, executionCtx.isForceStop(), executionCtx.round());
        } else {
            LOGGER.warn(genMsg(state.tick(), state.round(), finishedAt, "Programming error"), asyncResult.cause());
            transitionCtx = shouldStop(triggerContext, false, state.round());
        }
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
        return "Scheduling[" + tId + "::" + trigger.type() + "::" + jobData.externalId() + "][" + tickAndRound + "]" +
               "::[" + at + "] - " + event;
    }

    private <R> Future<R> executeBlocking(WorkerExecutor workerExecutor, Consumer<Promise<R>> operation) {
        return workerExecutor == null
               ? vertx.executeBlocking(operation::accept, false)
               : workerExecutor.executeBlocking(operation::accept, false);
    }

}
