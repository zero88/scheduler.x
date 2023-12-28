package io.github.zero88.schedulerx;

import java.time.Instant;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

/**
 * Represents for time clock
 *
 * @since 2.0.0
 */
@Internal
public interface TimeClock {

    /**
     * Obtains the current instant from the system clock.
     *
     * @return the current instant using the system clock, not null
     */
    @NotNull Instant now();

}
