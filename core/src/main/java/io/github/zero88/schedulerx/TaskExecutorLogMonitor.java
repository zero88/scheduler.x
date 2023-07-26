package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * Represents for a log monitor that observes and do log on each lifecycle event of the task executor.
 *
 * @since 1.0.0
 */
public interface TaskExecutorLogMonitor extends TaskExecutorMonitor {

    Logger LOGGER = LoggerFactory.getLogger(TaskExecutorLogMonitor.class);
    TaskExecutorLogMonitor LOG_MONITOR = new TaskExecutorLogMonitor() { };

    @Override
    default void onUnableSchedule(@NotNull TaskResult result) {
        LOGGER.error("Unable schedule task at [" + result.unscheduledAt() + "] due to error", result.error());
    }

    @Override
    default void onSchedule(@NotNull TaskResult result) {
        if (result.isReschedule()) {
            LOGGER.debug(
                "TaskExecutor is rescheduled at [" + result.rescheduledAt() + "] round [" + result.round() + "]");
        } else {
            LOGGER.debug("TaskExecutor is available at [" + result.availableAt() + "]");
        }
    }

    @Override
    default void onMisfire(@NotNull TaskResult result) {
        LOGGER.debug("Misfire tick [" + result.tick() + "] at [" + result.triggeredAt() + "]");
    }

    @Override
    default void onEach(@NotNull TaskResult result) {
        LOGGER.debug("Finish round [" + result.round() + "] - Is Error [" + result.isError() + "] | Executed at [" +
                     result.executedAt() + "] - Finished at [" + result.finishedAt() + "]");
    }

    @Override
    default void onCompleted(@NotNull TaskResult result) {
        LOGGER.debug("Completed task in round [" + result.round() + "] at [" + result.completedAt() + "]");
    }

}
