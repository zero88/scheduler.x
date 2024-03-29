package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;
import io.vertx.core.Vertx;

/**
 * Represents for the high level of a builder that construct {@code Scheduler}
 *
 * @param <IN>        Type of job input data
 * @param <OUT>       Type of job result data
 * @param <TRIGGER>   Type of Trigger
 * @param <SCHEDULER> Type of Scheduler
 * @param <SELF>      Type of Scheduler Builder
 * @see Trigger
 * @see Scheduler
 * @since 2.0.0
 */
public interface SchedulerBuilder<IN, OUT, TRIGGER extends Trigger, SCHEDULER extends Scheduler<TRIGGER>,
                                     SELF extends SchedulerBuilder<IN, OUT, TRIGGER, SCHEDULER, SELF>> {

    @NotNull SELF setVertx(@NotNull Vertx vertx);

    @NotNull SELF setMonitor(@NotNull SchedulingMonitor<OUT> monitor);

    @NotNull SELF setTrigger(@NotNull TRIGGER trigger);

    @NotNull SELF setTriggerEvaluator(@NotNull TriggerEvaluator evaluator);

    @NotNull SELF setJob(@NotNull Job<IN, OUT> job);

    @NotNull SELF setJobData(@NotNull JobData<IN> jobData);

    @NotNull SELF setTimeoutPolicy(@NotNull TimeoutPolicy timeoutPolicy);

    @NotNull SCHEDULER build();

}
