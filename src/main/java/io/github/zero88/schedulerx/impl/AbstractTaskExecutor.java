package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero88.schedulerx.TriggerTaskExecutor;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutor;
import io.github.zero88.schedulerx.TaskExecutorLogMonitor;
import io.github.zero88.schedulerx.TaskExecutorMonitor;
import io.github.zero88.schedulerx.TaskExecutorState;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public abstract class AbstractTaskExecutor<T extends Trigger>
    implements TriggerTaskExecutor<T, TaskExecutionContextInternal> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutor.class);
    @NonNull
    private final Vertx vertx;
    @NonNull
    private final TaskExecutorStateInternal state = new TaskExecutorStateImpl();
    @Default
    @NonNull
    private final TaskExecutorMonitor monitor = TaskExecutorLogMonitor.LOG_MONITOR;
    @NonNull
    @Default
    private final JobData jobData = JobData.EMPTY;
    @NonNull
    private final Task task;
    @NonNull
    private final T trigger;

    @Override
    public @NonNull TaskExecutorState state() {
        return state;
    }

    @Override
    public void start(WorkerExecutor workerExecutor) {
        this.addTimer(Promise.promise(), workerExecutor)
            .onSuccess(this::onReceiveTimer)
            .onFailure(t -> monitor().onUnableSchedule(TaskResultImpl.builder()
                                                                     .tick(state().tick())
                                                                     .round(state().round())
                                                                     .availableAt(state().availableAt())
                                                                     .unscheduledAt(Instant.now())
                                                                     .error(t)
                                                                     .build()));
    }

    @Override
    public void executeTask(@NonNull TaskExecutionContextInternal executionContext) {
        try {
            debug(state().tick(), state.round(), executionContext.executedAt(), "Executing task");
            task.execute(jobData(), executionContext);
            if (!task.isAsync()) {
                executionContext.internalComplete();
            }
            if (executionContext.isForceStop()) {
                cancel();
            }
        } catch (Exception ex) {
            executionContext.fail(ex);
        }
    }

    public void cancel() {
        if (!state().completed()) {
            debug(state().tick(), state().round(), Instant.now(), "Canceling task");
            vertx().cancelTimer(state().timerId());
            onCompleted();
        }
    }

    protected abstract @NonNull Future<Long> addTimer(@NonNull Promise<Long> promise, WorkerExecutor workerExecutor);

    protected abstract boolean shouldCancel(long round);

    protected void debug(long tick, long round, @NonNull Instant at, @NonNull String event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TaskExecutor[{}][{}][{}]::{}", tick, round, at, event);
        }
    }

    protected void onReceiveTimer(long timerId) {
        TaskResultImpl result;
        if (state().pending()) {
            result = TaskResultImpl.builder().availableAt(state.timerId(timerId).markAvailable().availableAt()).build();
        } else {
            result = TaskResultImpl.builder()
                                   .tick(state.timerId(timerId).tick())
                                   .round(state().round())
                                   .availableAt(state().availableAt())
                                   .rescheduledAt(Instant.now())
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

    protected boolean shouldRun(@NonNull Instant triggerAt) {
        final long tick = state.increaseTick();
        if (state().completed()) {
            debug(tick, state().round(), triggerAt, "Execution is already completed");
        }
        if (state().executing()) {
            debug(tick, state().round(), triggerAt, "Skip execution due to task is still running");
            monitor().onMisfire(TaskResultImpl.builder()
                                              .availableAt(state().availableAt())
                                              .tick(state().tick())
                                              .triggeredAt(triggerAt)
                                              .build());
        }
        return state().idle();
    }

    private TaskExecutionContextInternal setupContext(@NonNull Promise<Object> promise,
                                                      @NonNull TaskExecutionContextImpl executionContext) {
        state.markExecuting();
        return executionContext.setup(promise, Instant.now());
    }

    protected void onResult(@NonNull AsyncResult<Object> asyncResult) {
        state.markIdle();
        final Instant finishedAt = Instant.now();
        if (asyncResult.failed()) {
            LOGGER.warn("TaskExecutor[{}][{}][{}]::{}", state().tick(), state().round(), finishedAt,
                        "Internal execution error", asyncResult.cause());
        }
        if (asyncResult.succeeded()) {
            final TaskExecutionContextInternal result = (TaskExecutionContextInternal) asyncResult.result();
            debug(state().tick(), result.round(), finishedAt, "Handling task result");
            monitor().onEach(TaskResultImpl.builder()
                                           .availableAt(state().availableAt())
                                           .tick(state().tick())
                                           .round(result.round())
                                           .triggeredAt(result.triggeredAt())
                                           .executedAt(result.executedAt())
                                           .finishedAt(finishedAt)
                                           .data(state.addData(result.round(), result.data()))
                                           .error(state.addError(result.round(), result.error()))
                                           .completed(state().completed())
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
        monitor().onCompleted(TaskResultImpl.builder()
                                            .availableAt(state().availableAt())
                                            .tick(state().tick())
                                            .round(state().round())
                                            .completed(state().completed())
                                            .completedAt(completedAt)
                                            .data(state().lastData())
                                            .error(state().lastError())
                                            .build());
    }

}
