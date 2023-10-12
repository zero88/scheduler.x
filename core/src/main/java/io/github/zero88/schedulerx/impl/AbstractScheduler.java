package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
            log(executionContext.executedAt(), "Start to execute the task");
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
        this.registerTimer(Promise.promise(), workerExecutor)
            .onSuccess(this::onSchedule)
            .onFailure(this::onUnableSchedule);
    }

    protected final void doStop(long timerId, TriggerContext context) {
        unregisterTimer(timerId);
        onCompleted(context);
    }

    protected abstract @NotNull Future<Long> registerTimer(@NotNull Promise<Long> promise,
                                                           @Nullable WorkerExecutor workerExecutor);

    protected abstract void unregisterTimer(long timerId);

    protected final TriggerTransitionContext shouldRun(@NotNull TriggerTransitionContext triggerContext) {
        log(Instant.now(), "Evaluating the trigger condition...");
        if (triggerContext.condition().status() != TriggerStatus.KICKOFF) {
            return triggerContext;
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

    protected final void run(WorkerExecutor workerExecutor, TriggerTransitionContext triggerContext) {
        state.increaseTick();
        final TriggerTransitionContext transitionCtx = shouldRun(triggerContext);
        if (transitionCtx.isReady()) {
            final ExecutionContextInternal<OUT> ctx = new ExecutionContextImpl<>(vertx, state.increaseRound(),
                                                                                 transitionCtx);
            log(ctx.triggeredAt(), "Triggering the task execution...");
            if (workerExecutor != null) {
                workerExecutor.executeBlocking(promise -> executeTask(onExecute(promise, ctx)),
                                               asyncResult -> onResult(transitionCtx, asyncResult));
            } else {
                vertx.executeBlocking(promise -> executeTask(onExecute(promise, ctx)),
                                      asyncResult -> onResult(transitionCtx, asyncResult));
            }
        } else {
            onMisfire(transitionCtx);
        }
    }

    protected final void log(@NotNull Instant at, @NotNull String event) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(genMsg(state.tick(), state.round(), at, event));
        }
    }

    protected final void onSchedule(long timerId) {
        final TriggerContext ctx = TriggerContextFactory.scheduled(trigger().type());
        ExecutionResult<OUT> result;
        if (state.pending()) {
            result = ExecutionResultImpl.<OUT>builder()
                                        .setExternalId(jobData.externalId())
                                        .setAvailableAt(state.timerId(timerId).markAvailable())
                                        .setTriggerContext(ctx)
                                        .build();
        } else {
            result = ExecutionResultImpl.<OUT>builder()
                                        .setExternalId(jobData.externalId())
                                        .setAvailableAt(state.availableAt())
                                        .setRescheduledAt(Instant.now())
                                        .setTriggerContext(ctx)
                                        .setTick(state.timerId(timerId).tick())
                                        .setRound(state.round())
                                        .build();
        }
        monitor.onSchedule(result);
    }

    protected final void onUnableSchedule(Throwable cause) {
        final TriggerContext ctx = TriggerContextFactory.error(trigger.type(), ReasonCode.FAILED_TO_SCHEDULE, cause);
        monitor.onUnableSchedule(ExecutionResultImpl.<OUT>builder()
                                                    .setExternalId(jobData.externalId())
                                                    .setTick(state.tick())
                                                    .setRound(state.round())
                                                    .setUnscheduledAt(Instant.now())
                                                    .setTriggerContext(ctx)
                                                    .build());
    }

    protected final void onMisfire(@NotNull TriggerTransitionContext triggerContext) {
        log(Objects.requireNonNull(triggerContext.firedAt()),
            "Skip the execution::" + triggerContext.condition().reasonCode());
        monitor.onMisfire(ExecutionResultImpl.<OUT>builder()
                                             .setExternalId(jobData.externalId())
                                             .setTick(state.tick())
                                             .setAvailableAt(state.availableAt())
                                             .setFiredAt(triggerContext.firedAt())
                                             .setTriggerContext(triggerContext)
                                             .build());
    }

    @SuppressWarnings("unchecked")
    protected final void onResult(@NotNull TriggerTransitionContext triggerContext,
                                  @NotNull AsyncResult<Object> asyncResult) {
        final Instant finishedAt = state.markIdle();
        TriggerTransitionContext transitionCtx;
        if (asyncResult.succeeded()) {
            log(finishedAt, "Received the task result");
            final ExecutionContextInternal<OUT> executionCtx = (ExecutionContextInternal<OUT>) asyncResult.result();
            monitor.onEach(ExecutionResultImpl.<OUT>builder()
                                              .setExternalId(jobData.externalId())
                                              .setAvailableAt(state.availableAt())
                                              .setTick(state.tick())
                                              .setRound(executionCtx.round())
                                              .setTriggerContext(triggerContext)
                                              .setTriggeredAt(executionCtx.triggeredAt())
                                              .setExecutedAt(executionCtx.executedAt())
                                              .setFinishedAt(finishedAt)
                                              .setData(state.addData(executionCtx.round(), executionCtx.data()))
                                              .setError(state.addError(executionCtx.round(), executionCtx.error()))
                                              .build());
            transitionCtx = shouldStop(triggerContext, executionCtx.isForceStop(), state.round());
        } else {
            LOGGER.warn(genMsg(state.tick(), state.round(), finishedAt, "Programming error"), asyncResult.cause());
            transitionCtx = shouldStop(triggerContext, false, state.round());
        }
        if (transitionCtx.isStopped()) {
            doStop(state.timerId(), transitionCtx);
        }
    }

    protected final void onCompleted(TriggerContext context) {
        final Instant completedAt = state.markCompleted();
        log(completedAt, "The task execution is completed");
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

    private ExecutionContextInternal<OUT> onExecute(@NotNull Promise<Object> promise,
                                                    @NotNull ExecutionContextInternal<OUT> executionContext) {
        return executionContext.setup(promise, state.markExecuting());
    }

    private String genMsg(long tick, long round, @NotNull Instant at, @NotNull String event) {
        return "Scheduling[" + tick + "][" + round + "][" + at + "]::[" + jobData.externalId() + "] - " + event;
    }

}
