package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.impl.DefaultTriggerEvaluator;

/**
 * Represents for the trigger evaluator to assess in 2 cases:
 * <ul>
 *     <li>if the trigger is can run before each execution round is started.</li>
 *     <li>if the trigger should stop executing immediately after one round of execution begins.</li>
 * </ul>
 *
 * @see BeforeTriggerEvaluator
 * @see AfterTriggerEvaluator
 * @since 2.0.0
 */
public interface TriggerEvaluator extends BeforeTriggerEvaluator, AfterTriggerEvaluator {

    /**
     * Create a trigger evaluator with the before evaluator
     *
     * @return new trigger evaluator instance
     * @see BeforeTriggerEvaluator
     */
    static TriggerEvaluator byBefore(BeforeTriggerEvaluator beforeEvaluator) { return create(beforeEvaluator, null); }

    /**
     * Create a trigger evaluator with the after evaluator
     *
     * @return new trigger evaluator instance
     * @see AfterTriggerEvaluator
     */
    static TriggerEvaluator byAfter(AfterTriggerEvaluator afterEvaluator) { return create(null, afterEvaluator); }

    /**
     * Create a trigger evaluator with the before and after evaluator
     *
     * @return new trigger evaluator instance
     * @see BeforeTriggerEvaluator
     * @see AfterTriggerEvaluator
     */
    static TriggerEvaluator create(BeforeTriggerEvaluator beforeEvaluator, AfterTriggerEvaluator afterEvaluator) {
        return DefaultTriggerEvaluator.init(beforeEvaluator, afterEvaluator);
    }

    /**
     * Chain with another trigger evaluator.
     *
     * @param another another evaluator
     * @return a reference to this for fluent API
     */
    @NotNull TriggerEvaluator andThen(@Nullable TriggerEvaluator another);

}
