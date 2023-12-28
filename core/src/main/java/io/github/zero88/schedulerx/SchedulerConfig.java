package io.github.zero88.schedulerx;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;

/**
 * Shared immutable fields between {@code Scheduler} and its builder.
 * <p/>
 * This class is designed to internal usage, don't refer it in your code.
 *
 * @param <TRIGGER> Type of trigger
 * @param <OUT>     Type of job result data
 * @since 2.0.0
 */
@Internal
public interface SchedulerConfig<TRIGGER extends Trigger, OUT> extends HasTrigger<TRIGGER> {

    @Nullable TimeClock clock();

    /**
     * Defines a scheduling monitor
     *
     * @return scheduling monitor
     * @see SchedulingMonitor
     */
    @NotNull SchedulingMonitor<OUT> monitor();

    /**
     * The trigger evaluator
     *
     * @return trigger evaluator
     */
    @NotNull TriggerEvaluator triggerEvaluator();

}
