package io.github.zero88.schedulerx.trigger.repr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.Trigger;

final class SimpleTriggerRepresentationMapper implements TriggerRepresentationMapper<Trigger> {

    private static class Holder {

        private static final TriggerRepresentationMapper<Trigger> INSTANCE = new SimpleTriggerRepresentationMapper();

    }

    public static TriggerRepresentationMapper<Trigger> getInstance() {
        return Holder.INSTANCE;
    }

    private SimpleTriggerRepresentationMapper() { }

    @Override
    public @NotNull String type() { return "any"; }

    @Override
    public TriggerRepresentation apply(@NotNull Trigger trigger) {
        return (@Nullable String lang) -> trigger.toString();
    }

}
