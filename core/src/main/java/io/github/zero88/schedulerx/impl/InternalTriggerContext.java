package io.github.zero88.schedulerx.impl;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.TriggerContext;

@Internal
public interface InternalTriggerContext extends TriggerContext {

    boolean shouldRun();

    static InternalTriggerContext create(boolean shouldRun, TriggerContext ctx) {
        return new InternalTriggerContext() {
            @Override
            public boolean shouldRun() { return shouldRun; }

            @Override
            public @Nullable Object info() { return ctx.info(); }

            @Override
            public @NotNull String type() { return ctx.type(); }
        };
    }

}
