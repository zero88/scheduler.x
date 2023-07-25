package io.github.zero88.schedulerx.impl;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.IntervalTaskExecutor;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;

public class IntervalTaskExecutorBuilder
    extends AbstractTaskExecutorBuilder<IntervalTrigger, IntervalTaskExecutor, IntervalTaskExecutorBuilder> {

    public @NotNull IntervalTaskExecutor build() {
        return new IntervalTaskExecutorImpl(vertx(), monitor(), jobData(), task(), trigger());
    }

}
