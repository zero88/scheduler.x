package io.github.zero88.schedulerx;

import static io.github.zero88.schedulerx.impl.Utils.brackets;

import java.text.MessageFormat;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * Represents for a log monitor that observes and do log on each lifecycle event of the job executor.
 *
 * @param <OUT> Type of job result data
 * @apiNote This interface is renamed from {@code TaskExecutorLogMonitor} since {@code 2.0.0}
 * @since 2.0.0
 */
public interface SchedulingLogMonitor<OUT> extends SchedulingMonitor<OUT> {

    Logger LOGGER = LoggerFactory.getLogger(SchedulingLogMonitor.class);

    static <OUT> SchedulingLogMonitor<OUT> create() { return new SchedulingLogMonitor<>() { }; }

    @Override
    default void onUnableSchedule(@NotNull ExecutionResult<OUT> result) {
        LOGGER.error(MessageFormat.format("Trigger{0} is unable to schedule at {1}{2}{3}",
                                          brackets(result.triggerContext().type() + "::" + result.externalId()),
                                          brackets(result.tick() + "/" + result.round()),
                                          brackets("unscheduledAt|" + result.unscheduledAt()),
                                          brackets("cause|" + result.triggerContext().condition().cause())));
    }

    @Override
    default void onSchedule(@NotNull ExecutionResult<OUT> result) {
        if (result.isReschedule()) {
            LOGGER.debug(MessageFormat.format("Trigger{0} has been rescheduled at {1}{2}",
                                              brackets(result.triggerContext().type() + "::" + result.externalId()),
                                              brackets(result.tick() + "/" + result.round()),
                                              brackets("rescheduledAt|" + result.rescheduledAt())));
        } else {
            LOGGER.debug(MessageFormat.format("Trigger{0} has been registered at {1}{2}",
                                              brackets(result.triggerContext().type() + "::" + result.externalId()),
                                              brackets("-/-"), brackets("availableAt|" + result.availableAt())));
        }
    }

    @Override
    default void onMisfire(@NotNull ExecutionResult<OUT> result) {
        LOGGER.debug(MessageFormat.format("Trigger{0} has been misfire at {1}{2}{3}{4}",
                                          brackets(result.triggerContext().type() + "::" + result.externalId()),
                                          brackets(result.tick() + "/" + result.round()),
                                          brackets("firedAt|" + result.firedAt()),
                                          brackets("finishedAt|" + result.finishedAt()),
                                          brackets("reason|" + result.triggerContext().condition().reasonCode())));
    }

    @Override
    default void onEach(@NotNull ExecutionResult<OUT> result) {
        LOGGER.debug(MessageFormat.format("Trigger{0} has been executed at {1}{2}{3}{4}{5}",
                                          brackets(result.triggerContext().type() + "::" + result.externalId()),
                                          brackets(result.tick() + "/" + result.round()),
                                          brackets("firedAt|" + result.firedAt()),
                                          brackets("triggerAt|" + result.triggeredAt()),
                                          brackets("startedAt|" + result.executedAt()),
                                          brackets("endedAt|" + result.finishedAt())));
    }

    @Override
    default void onCompleted(@NotNull ExecutionResult<OUT> result) {
        LOGGER.debug(MessageFormat.format("Trigger{0} has been completed at {1}{2}{3}",
                                          brackets(result.triggerContext().type() + "::" + result.externalId()),
                                          brackets(result.tick() + "/" + result.round()),
                                          brackets("completedAt|" + result.completedAt()),
                                          brackets("reason|" + result.triggerContext().condition().reasonCode())));
    }

}
