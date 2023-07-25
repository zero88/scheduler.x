package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutionContext;
import io.github.zero88.schedulerx.TaskExecutor;
import io.github.zero88.schedulerx.TaskExecutorMonitor;
import io.github.zero88.schedulerx.TaskExecutorState;
import io.github.zero88.schedulerx.TaskResult;
import io.github.zero88.schedulerx.TriggerTaskExecutor;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

public abstract class AbstractTaskExecutor<T extends Trigger> implements TriggerTaskExecutor<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutor.class);
    @NotNull
    private final Vertx vertx;
    @NotNull
    private final TaskExecutorStateInternal state;
    @NotNull
    private final TaskExecutorMonitor monitor;
    @NotNull
    private final JobData jobData;
    @NotNull
    private final Task task;
    @NotNull
    private final T trigger;

    protected AbstractTaskExecutor(@NotNull Vertx vertx, @NotNull TaskExecutorMonitor monitor, @NotNull JobData jobData,
                                   @NotNull Task task, @NotNull T trigger) {
        this.vertx   = vertx;
        this.monitor = monitor;
        this.jobData = jobData;
        this.task    = task;
        this.trigger = trigger;
        this.state   = new TaskExecutorStateImpl();
    }

    @Override
    public @NotNull TaskExecutorState state() { return state; }

    @Override
    public @NotNull Vertx vertx() { return this.vertx; }

    @Override
    public @NotNull TaskExecutorMonitor monitor() { return this.monitor; }

    @Override
    public @NotNull JobData jobData() { return this.jobData; }

    @Override
    public @NotNull Task task() { return this.task; }

    public @NotNull T trigger()                   { return this.trigger; }

    @Override
    public void start(WorkerExecutor workerExecutor) {
        this.addTimer(Promise.promise(), workerExecutor)
            .onSuccess(this::onReceiveTimer)
            .onFailure(t -> monitor().onUnableSchedule(TaskResult.builder()
                                                                 .setTick(state().tick())
                                                                 .setRound(state().round())
                                                                 .setAvailableAt(state().availableAt())
                                                                 .setUnscheduledAt(Instant.now())
                                                                 .setError(t)
                                                                 .build()));
    }

    @Override
    public void executeTask(@NotNull TaskExecutionContext executionContext) {
        try {
            debug(state().tick(), state.round(), executionContext.executedAt(), "Executing task");
            task.execute(jobData(), executionContext);
            if (!task.isAsync()) {
                ((TaskExecutionContextInternal) executionContext).internalComplete();
            }
            if (executionContext.isForceStop()) {
                cancel();
            }
        } catch (Exception ex) {
            executionContext.fail(ex);
        }
    }

    @Override
    public void cancel() {
        if (!state().completed()) {
            debug(state().tick(), state().round(), Instant.now(), "Canceling task");
            vertx().cancelTimer(state().timerId());
            onCompleted();
        }
    }

    protected abstract @NotNull Future<Long> addTimer(@NotNull Promise<Long> promise, WorkerExecutor workerExecutor);

    protected abstract boolean shouldCancel(long round);

    protected void debug(long tick, long round, @NotNull Instant at, @NotNull String event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TaskExecutor[{}][{}][{}]::{}", tick, round, at, event);
        }
    }

    protected void onReceiveTimer(long timerId) {
        TaskResult result;
        if (state().pending()) {
            result = TaskResult.builder().setAvailableAt(state.timerId(timerId).markAvailable().availableAt()).build();
        } else {
            result = TaskResult.builder()
                               .setTick(state.timerId(timerId).tick())
                               .setRound(state().round())
                               .setAvailableAt(state().availableAt())
                               .setRescheduledAt(Instant.now())
                               .build();
        }
        monitor().onSchedule(result);
    }

    protected void run(WorkerExecutor workerExecutor) {
        final Instant triggerAt = Instant.now();
        if (shouldRun(triggerAt)) {
            final TaskExecutionContextImpl context = new TaskExecutionContextImpl(vertx(), state.increaseRound(),
                                                                                  triggerAt);
            debug(state().tick(), context.round(), triggerAt, "Trigger executing task");
            if (workerExecutor != null) {
                workerExecutor.executeBlocking(promise -> executeTask(setupContext(promise, context)), this::onResult);
            } else {
                vertx().executeBlocking(promise -> executeTask(setupContext(promise, context)), this::onResult);
            }
        }
    }

    protected boolean shouldRun(@NotNull Instant triggerAt) {
        final long tick = state.increaseTick();
        if (state().completed()) {
            debug(tick, state().round(), triggerAt, "Execution is already completed");
        }
        if (state().executing()) {
            debug(tick, state().round(), triggerAt, "Skip execution due to task is still running");
            monitor().onMisfire(TaskResult.builder()
                                          .setAvailableAt(state().availableAt())
                                          .setTick(state().tick())
                                          .setTriggeredAt(triggerAt)
                                          .build());
        }
        return state().idle();
    }

    private TaskExecutionContextInternal setupContext(@NotNull Promise<Object> promise,
                                                      @NotNull TaskExecutionContextInternal executionContext) {
        state.markExecuting();
        return executionContext.setup(promise, Instant.now());
    }

    protected void onResult(@NotNull AsyncResult<Object> asyncResult) {
        state.markIdle();
        final Instant finishedAt = Instant.now();
        if (asyncResult.failed()) {
            LOGGER.warn("TaskExecutor[{}][{}][{}]::{}", state().tick(), state().round(), finishedAt,
                        "Internal execution error", asyncResult.cause());
        }
        if (asyncResult.succeeded()) {
            final TaskExecutionContextInternal result = (TaskExecutionContextInternal) asyncResult.result();
            debug(state().tick(), result.round(), finishedAt, "Handling task result");
            monitor().onEach(TaskResult.builder()
                                       .setAvailableAt(state().availableAt())
                                       .setTick(state().tick())
                                       .setRound(result.round())
                                       .setTriggeredAt(result.triggeredAt())
                                       .setExecutedAt(result.executedAt())
                                       .setFinishedAt(finishedAt)
                                       .setData(state.addData(result.round(), result.data()))
                                       .setError(state.addError(result.round(), result.error()))
                                       .setCompleted(state().completed())
                                       .build());
        }
        if (shouldCancel(state().round())) {
            cancel();
        }
    }

    protected void onCompleted() {
        state.markCompleted();
        final Instant completedAt = Instant.now();
        debug(state().tick(), state().round(), completedAt, "Execution is completed");
        monitor().onCompleted(TaskResult.builder()
                                        .setAvailableAt(state().availableAt())
                                        .setTick(state().tick())
                                        .setRound(state().round())
                                        .setCompleted(state().completed())
                                        .setCompletedAt(completedAt)
                                        .setData(state().lastData())
                                        .setError(state().lastError())
                                        .build());
    }

}
