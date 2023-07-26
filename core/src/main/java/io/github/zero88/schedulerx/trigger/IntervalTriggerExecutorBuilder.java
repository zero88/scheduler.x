package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.impl.AbstractTaskExecutorBuilder;

/**
 * Represents a builder that constructs {@link IntervalTriggerExecutor}
 *
 * @since 2.0.0
 */
public final class IntervalTriggerExecutorBuilder
    extends AbstractTaskExecutorBuilder<IntervalTrigger, IntervalTriggerExecutor, IntervalTriggerExecutorBuilder> {

    public @NotNull IntervalTriggerExecutor build() {
        return new IntervalTriggerExecutorImpl(vertx(), monitor(), jobData(), task(), trigger());
    }

}
