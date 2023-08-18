package io.github.zero88.schedulerx.spi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.CronTrigger;

public class CronTriggerRepresentationExample implements CronTriggerRepresentationMapper {

    @Override
    public TriggerRepresentation apply(@NotNull CronTrigger trigger) {
        return new TriggerRepresentation() {
            @Override
            public @NotNull String display(@Nullable String lang) {
                if ("es".equals(lang)) {
                    return "Esta es una representación personalizada: " + trigger;
                }
                return "This is a custom representation: " + trigger;
            }
        };
    }

}
