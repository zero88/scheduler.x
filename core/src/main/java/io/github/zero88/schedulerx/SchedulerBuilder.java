package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Vertx;

/**
 * Represents for the high level of a builder that construct {@code Scheduler}
 *
 * @param <IN>        Type of job input data
 * @param <OUT>       Type of task result data
 * @param <TRIGGER>   Type of Trigger
 * @param <SCHEDULER> Type of Scheduler
 * @param <SELF>      Type of Scheduler Builder
 * @see Trigger
 * @see Scheduler
 * @since 2.0.0
 */
public interface SchedulerBuilder<IN, OUT, TRIGGER extends Trigger, SCHEDULER extends Scheduler<IN, OUT, TRIGGER>,
                                     SELF extends SchedulerBuilder<IN, OUT, TRIGGER, SCHEDULER, SELF>>
    extends TaskExecutorProperties<IN, OUT> {

    @NotNull TRIGGER trigger();

    @NotNull SELF setVertx(@NotNull Vertx vertx);

    @NotNull SELF setTask(@NotNull Task<IN, OUT> task);

    @NotNull SELF setTrigger(@NotNull TRIGGER trigger);

    @NotNull SELF setJobData(@NotNull JobData<IN> jobData);

    @NotNull SELF setMonitor(@NotNull SchedulingMonitor<OUT> monitor);

    @NotNull SCHEDULER build();

}
