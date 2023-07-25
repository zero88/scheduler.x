package io.github.zero88.schedulerx;

import io.github.zero88.schedulerx.impl.IntervalTaskExecutorBuilder;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;

public interface IntervalTaskExecutor extends TriggerTaskExecutor<IntervalTrigger> {

    static IntervalTaskExecutorBuilder builder() { return new IntervalTaskExecutorBuilder(); }

}
