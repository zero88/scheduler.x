package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents for log monitor
 *
 * @since 1.0.0
 */
public interface TaskExecutorLogMonitor extends TaskExecutorMonitor {

    Logger LOGGER = LoggerFactory.getLogger(TaskExecutorLogMonitor.class);
    TaskExecutorMonitor LOG_MONITOR = new TaskExecutorLogMonitor() {};

    @Override
    default void onUnableSchedule(@NotNull TaskResult result) {
        LOGGER.error("Unable schedule task at [{}] due to error", result.unscheduledAt(), result.error());
    }

    @Override
    default void onSchedule(@NotNull TaskResult result) {
        if (result.isReschedule()) {
            LOGGER.debug("TaskExecutor is rescheduled at [{}] round [{}]", result.rescheduledAt(), result.round());
        } else {
            LOGGER.debug("TaskExecutor is available at [{}]", result.availableAt());
        }
    }

    @Override
    default void onMisfire(@NotNull TaskResult result) {
        LOGGER.debug("Misfire tick [{}] at [{}]", result.tick(), result.triggeredAt());
    }

    @Override
    default void onEach(@NotNull TaskResult result) {
        LOGGER.debug("Finish round [{}] - Is Error [{}] | Executed at [{}] - Finished at [{}]", result.round(),
                     result.isError(), result.executedAt(), result.finishedAt());
    }

    @Override
    default void onCompleted(@NotNull TaskResult result) {
        LOGGER.debug("Completed task in round [{}] at [{}]", result.round(), result.completedAt());
    }

}
