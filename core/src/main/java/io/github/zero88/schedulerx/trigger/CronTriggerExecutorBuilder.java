package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.impl.AbstractTaskExecutorBuilder;

/**
 * Represents a builder that constructs {@link CronTriggerExecutor}
 *
 * @since 2.0.0
 */
public final class CronTriggerExecutorBuilder
    extends AbstractTaskExecutorBuilder<CronTrigger, CronTriggerExecutor, CronTriggerExecutorBuilder> {

    public @NotNull CronTriggerExecutor build() {
        return new CronTriggerExecutorImpl(vertx(), monitor(), jobData(), task(), trigger());
    }

}
