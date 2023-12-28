package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.TimeClock;

@Internal
class TimeClockImpl implements TimeClock {

    @Override
    public @NotNull Instant now() {
        return Instant.now();
    }

}
