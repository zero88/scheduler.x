package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A runtime trigger information
 */
public interface TriggerContext extends HasTriggerType {

    @Nullable Object info();

    static TriggerContext empty(@NotNull String type) {
        return create(type, null);
    }

    static <T> TriggerContext create(@NotNull String type, T info) {
        return new TriggerContext() {
            @Override
            public @NotNull String type() { return type; }

            @Override
            public T info() { return info; }
        };
    }

}
