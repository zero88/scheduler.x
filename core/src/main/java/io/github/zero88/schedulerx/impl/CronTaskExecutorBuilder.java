package io.github.zero88.schedulerx.impl;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.CronTaskExecutor;
import io.github.zero88.schedulerx.trigger.CronTrigger;

public final class CronTaskExecutorBuilder
    extends AbstractTaskExecutorBuilder<CronTrigger, CronTaskExecutor, CronTaskExecutorBuilder> {

    public @NotNull CronTaskExecutor build() {
        return new CronTaskExecutorImpl(vertx(), monitor(), jobData(), task(), trigger());
    }

}
