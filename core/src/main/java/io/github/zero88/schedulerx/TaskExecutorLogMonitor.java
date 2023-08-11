package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * Represents for a log monitor that observes and do log on each lifecycle event of the task executor.
 *
 * @param <OUTPUT> Type of Result data
 * @since 1.0.0
 */
public interface TaskExecutorLogMonitor<OUTPUT> extends TaskExecutorMonitor<OUTPUT> {

    Logger LOGGER = LoggerFactory.getLogger(TaskExecutorLogMonitor.class);

    static <OUT> TaskExecutorLogMonitor<OUT> create() {
        return new TaskExecutorLogMonitor<OUT>() { };
    }

    @Override
    default void onUnableSchedule(@NotNull TaskResult<OUTPUT> result) {
        LOGGER.error(
            "Task[" + result.externalId() + "] is unable to schedule at[" + result.unscheduledAt() + "] due to error",
            result.error());
    }

    @Override
    default void onSchedule(@NotNull TaskResult<OUTPUT> result) {
        if (result.isReschedule()) {
            LOGGER.debug(
                "Task[" + result.externalId() + "] is rescheduled at[" + result.rescheduledAt() + "] after round[" +
                result.round() + "]");
        } else {
            LOGGER.debug("Task[" + result.externalId() + "] is scheduled at[" + result.availableAt() + "]");
        }
    }

    @Override
    default void onMisfire(@NotNull TaskResult<OUTPUT> result) {
        LOGGER.debug(
            "Task[" + result.externalId() + "] is misfire at tick[" + result.tick() + "] in round[" + result.round() +
            "] at[" + result.triggeredAt() + "]");
    }

    @Override
    default void onEach(@NotNull TaskResult<OUTPUT> result) {
        LOGGER.debug("Task[" + result.externalId() + "] has been executed in round[" + result.round() + "] startedAt[" +
                     result.executedAt() + "] - endedAt[" + result.finishedAt() + "] - Error[" + result.isError() +
                     "]");
    }

    @Override
    default void onCompleted(@NotNull TaskResult<OUTPUT> result) {
        LOGGER.debug("Task[" + result.externalId() + "] is completed in round[" + result.round() + "] at[" +
                     result.completedAt() + "]");
    }

}
