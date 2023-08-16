package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.ExecutionContext;
import io.github.zero88.schedulerx.TaskExecutor;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.trigger.Trigger;
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
    protected static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutor.class);

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
            trace(executionContext.executedAt(), "Start to execute the task");
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
            trace(Instant.now(), "Canceling the task");
            doStop(state.timerId());
        }
    }

    protected final void doStart(WorkerExecutor workerExecutor) {
        this.registerTimer(Promise.promise(), workerExecutor)
            .onSuccess(this::onSchedule)
            .onFailure(this::onUnableSchedule);
    }

    protected final void doStop(long timerId) {
        unregisterTimer(timerId);
        onCompleted();
    }

    protected abstract @NotNull Future<Long> registerTimer(@NotNull Promise<Long> promise,
                                                           @Nullable WorkerExecutor workerExecutor);

    protected void unregisterTimer(long timerId) { vertx.cancelTimer(timerId); }

    protected InternalTriggerContext shouldRun(@NotNull Instant triggerAt, @NotNull TriggerContext triggerContext) {
        state.increaseTick();
        if (state.completed()) {
            trace(triggerAt, "The task execution is already completed");
        }
        if (state.executing()) {
            onMisfire(triggerAt, "The task is still running");
        }
        return InternalTriggerContext.create(state.idle() && trigger().shouldExecute(triggerAt), triggerContext);
    }

    protected final boolean shouldStop(@Nullable ExecutionContext<OUT> executionContext, long round) {
        return (executionContext != null && executionContext.isForceStop()) || trigger().shouldStop(round);
    }

    protected final void run(WorkerExecutor workerExecutor, TriggerContext triggerContext) {
        final Instant triggerAt = Instant.now();
        final InternalTriggerContext internalContext = shouldRun(triggerAt, triggerContext);
        if (internalContext.shouldRun()) {
            final TriggerContext triggerCtx = TriggerContext.create(internalContext.type(), internalContext.info());
            final ExecutionContextInternal<OUT> ctx = new ExecutionContextImpl<>(vertx, state.increaseRound(),
                                                                                 triggerAt, triggerCtx);
            trace(triggerAt, "Trigger the task execution");
            if (workerExecutor != null) {
                workerExecutor.executeBlocking(promise -> executeTask(onExecute(promise, ctx)), this::onResult);
            } else {
                vertx.executeBlocking(promise -> executeTask(onExecute(promise, ctx)), this::onResult);
            }
        }
    }

    protected final void trace(@NotNull Instant at, @NotNull String event) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(genMsg(state.tick(), state.round(), at, event));
        }
    }

    protected final void onSchedule(long timerId) {
        ExecutionResult<OUT> result;
        if (state.pending()) {
            result = ExecutionResultImpl.<OUT>builder()
                                        .setExternalId(jobData.externalId())
                                        .setAvailableAt(state.timerId(timerId).markAvailable().availableAt())
                                        .build();
        } else {
            result = ExecutionResultImpl.<OUT>builder()
                                        .setExternalId(jobData.externalId())
                                        .setTick(state.timerId(timerId).tick())
                                        .setRound(state.round())
                                        .setAvailableAt(state.availableAt())
                                        .setRescheduledAt(Instant.now())
                                        .build();
        }
        monitor.onSchedule(result);
    }

    protected final void onUnableSchedule(Throwable t) {
        monitor.onUnableSchedule(ExecutionResultImpl.<OUT>builder()
                                                    .setExternalId(jobData.externalId())
                                                    .setTick(state.tick())
                                                    .setRound(state.round())
                                                    .setUnscheduledAt(Instant.now())
                                                    .setError(t)
                                                    .build());
    }

    protected final void onMisfire(@NotNull Instant triggerAt, String reason) {
        trace(triggerAt, "Skip the execution::" + reason);
        monitor.onMisfire(ExecutionResultImpl.<OUT>builder()
                                             .setExternalId(jobData.externalId())
                                             .setTick(state.tick())
                                             .setAvailableAt(state.availableAt())
                                             .setTriggeredAt(triggerAt)
                                             .build());
    }

    @SuppressWarnings("unchecked")
    protected final void onResult(@NotNull AsyncResult<Object> asyncResult) {
        state.markIdle();
        final Instant finishedAt = Instant.now();
        if (asyncResult.failed()) {
            LOGGER.warn(genMsg(state.tick(), state.round(), finishedAt, "Programming error"), asyncResult.cause());
        }
        ExecutionContextInternal<OUT> executionContext = (ExecutionContextInternal<OUT>) asyncResult.result();
        if (asyncResult.succeeded()) {
            trace(finishedAt, "Received the task result");
            monitor.onEach(ExecutionResultImpl.<OUT>builder()
                                              .setExternalId(jobData.externalId())
                                              .setAvailableAt(state.availableAt())
                                              .setTick(state.tick())
                                              .setRound(executionContext.round())
                                              .setTriggeredAt(executionContext.triggeredAt())
                                              .setExecutedAt(executionContext.executedAt())
                                              .setFinishedAt(finishedAt)
                                              .setData(state.addData(executionContext.round(), executionContext.data()))
                                              .setError(state.addError(executionContext.round(), executionContext.error()))
                                              .setCompleted(state.completed())
                                              .build());
        }
        if (shouldStop(executionContext, state.round())) {
            doStop(state.timerId());
        }
    }

    protected final void onCompleted() {
        state.markCompleted();
        final Instant completedAt = Instant.now();
        trace(completedAt, "The task execution is completed");
        monitor.onCompleted(ExecutionResultImpl.<OUT>builder()
                                               .setExternalId(jobData.externalId())
                                               .setAvailableAt(state.availableAt())
                                               .setTick(state.tick())
                                               .setRound(state.round())
                                               .setCompleted(state.completed())
                                               .setCompletedAt(completedAt)
                                               .setData(state.lastData())
                                               .setError(state.lastError())
                                               .build());
    }

    private ExecutionContextInternal<OUT> onExecute(@NotNull Promise<Object> promise,
                                                    @NotNull ExecutionContextInternal<OUT> executionContext) {
        state.markExecuting();
        return executionContext.setup(promise, Instant.now());
    }

    private String genMsg(long tick, long round, @NotNull Instant at, @NotNull String event) {
        return "Scheduling[" + tick + "][" + round + "][" + at + "]::[" + jobData.externalId() + "] - " + event;
    }

}