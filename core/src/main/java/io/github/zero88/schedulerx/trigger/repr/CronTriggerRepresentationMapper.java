package io.github.zero88.schedulerx.trigger.repr;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.CronTrigger;

public interface CronTriggerRepresentationMapper extends TriggerRepresentationMapper<CronTrigger> {

    @Override
    default @NotNull String type() { return CronTrigger.TRIGGER_TYPE; }

}
