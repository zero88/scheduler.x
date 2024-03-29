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

import io.github.zero88.schedulerx.AsyncJob;
import io.github.zero88.schedulerx.ExecutionContext;
import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.JobExecutor;
import io.github.zero88.schedulerx.JobExecutorConfig;
import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.SchedulerConfig;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.TimeClock;
import io.github.zero88.schedulerx.TimeoutBlock;
import io.github.zero88.schedulerx.TimeoutPolicy;
import io.github.zero88.schedulerx.WorkerExecutorFactory;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.github.zero88.schedulerx.trigger.TriggerCondition.ReasonCode;
import io.github.zero88.schedulerx.trigger.TriggerContext;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;
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
public abstract class AbstractScheduler<IN, OUT, T extends Trigger>
    implements Scheduler<T>, SchedulerConfig<T, OUT>, JobExecutorConfig<IN, OUT>, JobExecutor<OUT> {

    @SuppressWarnings("java:S3416")
    protected static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    private final @NotNull Vertx vertx;
    private final @NotNull SchedulerStateInternal<OUT> state;
    private final @NotNull SchedulingMonitorInternal<OUT> monitor;
    private final @NotNull JobData<IN> jobData;
    private final @NotNull Job<IN, OUT> job;
    private final @NotNull T trigger;
    private final @NotNull TriggerEvaluator evaluator;
    private final @NotNull TimeoutPolicy timeoutPolicy;
    private final @NotNull TimeClock clock;
    private final Lock lock = new ReentrantLock();
    private boolean didStart = false;
    private boolean didTriggerValidation = false;
    private IllegalArgumentException invalidTrigger;

    protected AbstractScheduler(Vertx vertx, TimeClock clock, SchedulingMonitor<OUT> monitor, Job<IN, OUT> job,
                                JobData<IN> jobData, TimeoutPolicy timeoutPolicy, T trigger,
                                TriggerEvaluator evaluator) {
        this.vertx         = Objects.requireNonNull(vertx, "Vertx instance is required");
        this.job           = Objects.requireNonNull(job, "Job is required");
        this.trigger       = Objects.requireNonNull(trigger, "Trigger is required");
        this.clock         = Optional.ofNullable(clock).orElseGet(TimeClockImpl::new);
        this.jobData       = Optional.ofNullable(jobData).orElseGet(JobData::empty);
        this.timeoutPolicy = Optional.ofNullable(timeoutPolicy).orElseGet(TimeoutPolicy::byDefault);
        this.monitor       = new SchedulingMonitorImpl<>(vertx, monitor);
        this.state         = new SchedulerStateImpl<>(this.clock);
        this.evaluator     = new InternalTriggerEvaluator(this, evaluator);
    }

    @Override
    public final @NotNull Vertx vertx() { return vertx; }

    @Override
    public final @NotNull TimeClock clock() { return clock; }

    @Override
    public final @NotNull SchedulingMonitor<OUT> monitor() { return monitor.unwrap(); }

    @Override
    public final @NotNull Job<IN, OUT> job() { return job; }

    @Override
    public final @NotNull JobData<IN> jobData() { return jobData; }

    @Override
    public @NotNull TimeoutPolicy timeoutPolicy() { return timeoutPolicy; }

    @Override
    public @NotNull TriggerEvaluator triggerEvaluator() { return evaluator; }

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
                    invalidTrigger = (IllegalArgumentException) ex;
                } else {
                    invalidTrigger = new IllegalArgumentException(
                        "Encounter an unexpected exception when validating trigger", ex);
                }
                throw invalidTrigger;
            } finally {
                didTriggerValidation = true;
            }
        } finally {
            lock.unlock();
        }
    }

    public final void start() { start(null); }

    @Override
    public final void start(WorkerExecutor workerExecutor) {
        TriggerRule rule;
        try {
            rule = trigger().rule();
        } catch (Exception ex) {
            onUnableSchedule(ex);
            return;
        }
        lock.lock();
        try {
            if (didStart) {
                throw new IllegalStateException("The scheduler is already started!");
            }
            final WorkerExecutor executor = workerExecutor == null
                                            ? WorkerExecutorFactory.createExecutionWorker(vertx, timeoutPolicy)
                                            : workerExecutor;
            final Instant now = clock.now();
            final Duration delay = rule.calculateRegisterTime(now);
            if (delay.isZero()) {
                doStart(executor);
            } else {
                log(now, "Wait for " + brackets(delay) + " before registering the trigger " +
                         "in the scheduler at the beginning time in the trigger rule");
                state.timerId(vertx().setTimer(delay.toMillis(), ignore -> doStart(executor)));
            }
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
            if (!(job instanceof AsyncJob)) {
                ((ExecutionContextInternal<OUT>) executionContext).internalComplete();
            }
        } catch (Exception ex) {
            executionContext.fail(ex);
        }
    }

    @Override
    public final void cancel() {
        if (!state.completed()) {
            log(clock.now(), "On cancel");
            doStop(state.timerId(), TriggerContextFactory.cancel(trigger().type(), state.tick()));
        }
    }

    protected final void doStart(WorkerExecutor workerExecutor) {
        this.registerTimer(workerExecutor).onSuccess(this::onSchedule).onFailure(this::onUnableSchedule);
    }

    protected final void doStop(long timerId, TriggerContext context) {
        if (context.isStopped()) {
            unregisterTimer(timerId);
            onComplete(context);
        }
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
     * Register a timer id in internal state and increase tick time when the system timer fires
     *
     * @param timerId the system timer id
     * @return the current number times of the tick counter
     */
    protected final long onFire(long timerId) {
        return state.timerId(timerId).increaseTick();
    }

    /**
     * Processing the trigger right away after the system timer fires
     */
    protected final void onProcess(WorkerExecutor workerExecutor, TriggerContext triggerContext) {
        log(Objects.requireNonNull(triggerContext.firedAt()), "On fire");
        this.onEvaluationBeforeTrigger(workerExecutor, triggerContext)
            .onSuccess(ctx -> onTrigger(workerExecutor, ctx))
            .onFailure(t -> onMisfire(TriggerContextFactory.skip(triggerContext, t instanceof TimeoutException
                                                                                 ? ReasonCode.EVALUATION_TIMEOUT
                                                                                 : ReasonCode.UNEXPECTED_ERROR, t)));
    }

    protected final void onTrigger(WorkerExecutor workerExecutor, TriggerContext triggerContext) {
        if (!triggerContext.isReady()) {
            onMisfire(triggerContext);
            return;
        }
        final long round = state.increaseRound();
        final Duration timeout = timeoutPolicy().executionTimeout();
        final ExecutionContextInternal<OUT> exeCtx = new ExecutionContextImpl<>(vertx, clock, triggerContext, round);
        log(exeCtx.triggeredAt(), "On trigger", triggerContext.tick(), round);
        Future.join(onEvaluationAfterTrigger(workerExecutor, triggerContext, round),
                    executeBlocking(workerExecutor, p -> executeJob(exeCtx.setup(wrapTimeout(timeout, p)))))
              .onComplete(ar -> onResult(exeCtx, ar.cause()));
    }

    protected final void onSchedule(long timerId) {
        ExecutionResult<OUT> result;
        if (state.pending()) {
            final TriggerContext context = TriggerContextFactory.scheduled(trigger().type());
            result = ExecutionResultImpl.<OUT>builder()
                                        .setExternalId(jobData.externalId())
                                        .setAvailableAt(state.markAvailable())
                                        .setTriggerContext(context)
                                        .setTick(context.tick())
                                        .setRound(context.tick())
                                        .build();
        } else {
            final TriggerContext context = TriggerContextFactory.rescheduled(trigger().type(), state.tick());
            result = ExecutionResultImpl.<OUT>builder()
                                        .setExternalId(jobData.externalId())
                                        .setAvailableAt(state.availableAt())
                                        .setTriggerContext(context)
                                        .setRescheduledAt(clock.now())
                                        .setTick(context.tick())
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
                                                    .setUnscheduledAt(clock.now())
                                                    .setTick(ctx.tick())
                                                    .setRound(ctx.tick())
                                                    .build());
    }

    protected final Future<TriggerContext> onEvaluationBeforeTrigger(WorkerExecutor worker, TriggerContext ctx) {
        return executeBlocking(worker, p -> {
            log(clock.now(), "On before trigger");
            this.wrapTimeout(timeoutPolicy.evaluationTimeout(), p)
                .handle(evaluator.beforeTrigger(trigger, ctx, jobData.externalId()));
        });
    }

    protected final Future<TriggerContext> onEvaluationAfterTrigger(WorkerExecutor worker, TriggerContext ctx,
                                                                    long round) {
        return executeBlocking(worker, p -> {
            log(clock.now(), "On after trigger");
            this.wrapTimeout(timeoutPolicy.evaluationTimeout(), p)
                .handle(evaluator.afterTrigger(trigger(), ctx, jobData.externalId(), round)
                                 .onSuccess(c -> doStop(state.timerId(), c))
                                 .otherwise(t -> {
                                     LOGGER.error(genMsg(ctx.tick(), round, clock.now(), "On after trigger::error"), t);
                                     return ctx;
                                 }));
        });
    }

    protected final void onMisfire(@NotNull TriggerContext triggerCtx) {
        final Instant finishedAt = state.markFinished(triggerCtx.tick());
        final String reasonCode = triggerCtx.condition().reasonCode();
        final String event = "On misfire::" + reasonCode;
        if (ReasonCode.UNEXPECTED_ERROR.equals(reasonCode)) {
            LOGGER.error(genMsg(triggerCtx.tick(), state.round(), finishedAt, event), triggerCtx.condition().cause());
        } else {
            log(finishedAt, event, triggerCtx.tick());
        }
        monitor.onMisfire(ExecutionResultImpl.<OUT>builder()
                                             .setExternalId(jobData.externalId())
                                             .setAvailableAt(state.availableAt())
                                             .setTriggerContext(triggerCtx)
                                             .setTick(triggerCtx.tick())
                                             .setFiredAt(triggerCtx.firedAt())
                                             .setRound(state.round())
                                             .setFinishedAt(finishedAt)
                                             .build());
    }

    protected final void onResult(@NotNull ExecutionContext<OUT> executionContext, @Nullable Throwable asyncCause) {
        final ExecutionContextInternal<OUT> ctx = (ExecutionContextInternal<OUT>) executionContext;
        final TriggerContext triggerContext = TriggerContextFactory.executed(ctx.triggerContext());
        final Instant finishedAt = state.markFinished(triggerContext.tick());
        log(finishedAt, "On result", triggerContext.tick(), ctx.round());
        if (asyncCause instanceof TimeoutException) {
            LOGGER.warn(genMsg(triggerContext.tick(), ctx.round(), finishedAt, asyncCause.getMessage()));
        } else if (asyncCause != null) {
            LOGGER.error(genMsg(triggerContext.tick(), ctx.round(), finishedAt, "On result::System error"), asyncCause);
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
        if (ctx.isForceStop()) {
            doStop(state.timerId(), TriggerContextFactory.stop(triggerContext, ReasonCode.STOP_BY_JOB));
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
        String tId = String.valueOf(state.timerId());
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

    @SuppressWarnings("rawtypes")
    private static class InternalTriggerEvaluator extends DefaultTriggerEvaluator {

        private final @NotNull AbstractScheduler scheduler;

        private InternalTriggerEvaluator(@NotNull AbstractScheduler scheduler, TriggerEvaluator evaluator) {
            this.scheduler = scheduler;
            andThen(Optional.ofNullable(evaluator).orElseGet(DefaultTriggerEvaluator::new));
        }

        @Override
        protected Future<TriggerContext> internalBeforeTrigger(@NotNull Trigger trigger, @NotNull TriggerContext ctx,
                                                               @Nullable Object externalId) {
            return Future.succeededFuture(ctx.isKickoff() ? doCheck(ctx) : ctx);
        }

        private @NotNull TriggerContext doCheck(TriggerContext ctx) {
            if (scheduler.state.pending()) {
                return TriggerContextFactory.skip(ctx, ReasonCode.NOT_YET_SCHEDULED);
            }
            if (scheduler.state.completed()) {
                return TriggerContextFactory.skip(ctx, ReasonCode.ALREADY_STOPPED);
            }
            if (scheduler.state.executing()) {
                return TriggerContextFactory.skip(ctx, ReasonCode.JOB_IS_RUNNING);
            }
            final Instant firedAt = Objects.requireNonNull(ctx.firedAt());
            final TriggerRule rule = scheduler.trigger().rule();
            if (rule.isPending(firedAt)) {
                return TriggerContextFactory.unready(ctx, ReasonCode.CONDITION_IS_NOT_MATCHED);
            }
            if (rule.isExceeded(firedAt)) {
                return TriggerContextFactory.stop(ctx, ReasonCode.STOP_BY_CONFIG);
            }
            return rule.satisfy(firedAt)
                   ? TriggerContextFactory.ready(ctx)
                   : TriggerContextFactory.skip(ctx, ReasonCode.CONDITION_IS_NOT_MATCHED);
        }

    }

}
