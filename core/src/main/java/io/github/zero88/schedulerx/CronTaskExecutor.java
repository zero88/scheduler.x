package io.github.zero88.schedulerx;

import io.github.zero88.schedulerx.impl.CronTaskExecutorBuilder;
import io.github.zero88.schedulerx.trigger.CronTrigger;

public interface CronTaskExecutor extends TriggerTaskExecutor<CronTrigger> {

    static CronTaskExecutorBuilder builder() { return new CronTaskExecutorBuilder(); }

}
