package io.github.zero88.schedulerx.trigger.repr;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.IntervalTrigger;

public interface IntervalTriggerRepresentationMapper extends TriggerRepresentationMapper<IntervalTrigger> {

    @Override
    default @NotNull String type() { return IntervalTrigger.TRIGGER_TYPE; }

}
