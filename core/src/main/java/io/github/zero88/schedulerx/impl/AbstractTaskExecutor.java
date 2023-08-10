package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutionContext;
import io.github.zero88.schedulerx.TaskExecutor;
import io.github.zero88.schedulerx.TaskExecutorMonitor;
import io.github.zero88.schedulerx.TaskResult;
import io.github.zero88.schedulerx.TriggerTaskExecutor;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * The base task executor
 *
 * @param <IN>  Type of input data
 * @param <OUT> Type of output data
 * @param <T>   Type of trigger
 */
public abstract class AbstractTaskExecutor<IN, OUT, T extends Trigger> implements TriggerTaskExecutor<IN, OUT, T> {

    @SuppressWarnings("java:S3416")
    protected static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutor.class);

    private final @NotNull Vertx vertx;
    private final @NotNull TaskExecutorStateInternal<OUT> state;
    private final @NotNull TaskExecutorMonitor<OUT> monitor;
    private final @NotNull JobData<IN> jobData;
    private final @NotNull Task<IN, OUT> task;
    private final @NotNull T trigger;
    private final Lock lock = new ReentrantLock();
    private boolean didTriggerValidation = false;
    private IllegalArgumentException invalidTrigger;

    protected AbstractTaskExecutor(@NotNull Vertx vertx, @NotNull TaskExecutorMonitor<OUT> monitor,
                                   @NotNull JobData<IN> jobData, @NotNull Task<IN, OUT> task, @NotNull T trigger) {
        this.vertx   = vertx;
        this.monitor = monitor;
        this.jobData = jobData;
        this.task    = task;
        this.trigger = trigger;
        this.state   = new TaskExecutorStateImpl<>();
    }

    @Override
    public final @NotNull Vertx vertx() { return this.vertx; }

    @Override
    public final @NotNull TaskExecutorMonitor<OUT> monitor() { return this.monitor; }

    @Override
    public final @NotNull JobData<IN> jobData() { return this.jobData; }

    @Override
    public final @NotNull Task<IN, OUT> task() { return this.task; }

    @Override
    public final @NotNull T trigger() {
        lock.lock();
        try {
            if (didTriggerValidation) {
                if (invalidTrigger == null) { return trigger; }
                throw invalidTrigger;
            }
            try {
                //noinspection unchecked
                return (T) this.trigger.validate();
            } catch (IllegalArgumentException ex) {
                this.invalidTrigger = ex;
                throw ex;
            } finally {
                didTriggerValidation = true;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final void start(WorkerExecutor workerExecutor) {
        doStart(workerExecutor);
    }

    @Override
    public final void executeTask(@NotNull TaskExecutionContext<OUT> executionContext) {
        try {
            debug(state.tick(), state.round(), executionContext.executedAt(), "Executing task");
            task.execute(jobData(), executionContext);
            if (!task.isAsync()) {
                ((TaskExecutionContextInternal<OUT>) executionContext).internalComplete();
            }
        } catch (Exception ex) {
            executionContext.fail(ex);
        }
    }

    @Override
    public final void cancel() {
        if (!state.completed()) {
            debug(state.tick(), state.round(), Instant.now(), "Canceling task");
            doStop(state.timerId());
            onCompleted();
        }
    }

    protected abstract @NotNull Future<Long> addTimer(@NotNull Promise<Long> promise, WorkerExecutor workerExecutor);

    protected final boolean shouldRun(@NotNull Instant triggerAt) {
        final long tick = state.increaseTick();
        if (state.completed()) {
            debug(tick, state.round(), triggerAt, "Execution is already completed");
        }
        if (state.executing()) {
            debug(tick, state.round(), triggerAt, "Skip execution due to task is still running");
            monitor.onMisfire(TaskResultImpl.<OUT>builder()
                                            .setAvailableAt(state.availableAt())
                                            .setTick(state.tick())
                                            .setTriggeredAt(triggerAt)
                                            .build());
        }
        return state.idle() && trigger().shouldExecute(triggerAt);
    }

    protected final boolean shouldStop(@Nullable TaskExecutionContext<OUT> executionContext, long round) {
        return (executionContext != null && executionContext.isForceStop()) || trigger().shouldStop(round);
    }

    protected final void doStart(WorkerExecutor workerExecutor) {
        this.addTimer(Promise.promise(), workerExecutor)
            .onSuccess(this::onReceiveTimer)
            .onFailure(t -> monitor.onUnableSchedule(TaskResultImpl.<OUT>builder()
                                                                   .setTick(state.tick())
                                                                   .setRound(state.round())
                                                                   .setAvailableAt(state.availableAt())
                                                                   .setUnscheduledAt(Instant.now())
                                                                   .setError(t)
                                                                   .build()));
    }

    protected void doStop(long timerId) {
        vertx.cancelTimer(timerId);
    }

    protected final void debug(long tick, long round, @NotNull Instant at, @NotNull String event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TaskExecutor[" + tick + "][" + round + "][" + at + "]::" + event);
        }
    }

    protected final void onReceiveTimer(long timerId) {
        TaskResult<OUT> result;
        if (state.pending()) {
            result = TaskResultImpl.<OUT>builder()
                                   .setAvailableAt(state.timerId(timerId).markAvailable().availableAt())
                                   .build();
        } else {
            result = TaskResultImpl.<OUT>builder()
                                   .setTick(state.timerId(timerId).tick())
                                   .setRound(state.round())
                                   .setAvailableAt(state.availableAt())
                                   .setRescheduledAt(Instant.now())
                                   .build();
        }
        monitor.onSchedule(result);
    }

    protected final void run(WorkerExecutor workerExecutor) {
        final Instant triggerAt = Instant.now();
        if (shouldRun(triggerAt)) {
            TaskExecutionContextInternal<OUT> ctx = new TaskExecutionContextImpl<>(vertx, state.increaseRound(),
                                                                                   triggerAt);
            debug(state.tick(), ctx.round(), triggerAt, "Trigger executing task");
            if (workerExecutor != null) {
                workerExecutor.executeBlocking(promise -> executeTask(onExecute(promise, ctx)), this::onResult);
            } else {
                vertx.executeBlocking(promise -> executeTask(onExecute(promise, ctx)), this::onResult);
            }
        }
    }

    private TaskExecutionContextInternal<OUT> onExecute(@NotNull Promise<Object> promise,
                                                        @NotNull TaskExecutionContextInternal<OUT> executionContext) {
        state.markExecuting();
        return executionContext.setup(promise, Instant.now());
    }

    @SuppressWarnings("unchecked")
    protected final void onResult(@NotNull AsyncResult<Object> asyncResult) {
        state.markIdle();
        final Instant finishedAt = Instant.now();
        if (asyncResult.failed()) {
            LOGGER.warn("TaskExecutor[" + state.tick() + "][" + state.round() + "][" + finishedAt + "]" +
                        "::Internal execution error", asyncResult.cause());
        }
        TaskExecutionContextInternal<OUT> executionContext = (TaskExecutionContextInternal<OUT>) asyncResult.result();
        if (asyncResult.succeeded()) {
            debug(state.tick(), executionContext.round(), finishedAt, "Handling task result");
            monitor.onEach(TaskResultImpl.<OUT>builder()
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
            onCompleted();
        }
    }

    protected final void onCompleted() {
        state.markCompleted();
        final Instant completedAt = Instant.now();
        debug(state.tick(), state.round(), completedAt, "Execution is completed");
        monitor.onCompleted(TaskResultImpl.<OUT>builder()
                                          .setAvailableAt(state.availableAt())
                                          .setTick(state.tick())
                                          .setRound(state.round())
                                          .setCompleted(state.completed())
                                          .setCompletedAt(completedAt)
                                          .setData(state.lastData())
                                          .setError(state.lastError())
                                          .build());
    }

}
