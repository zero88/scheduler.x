package io.github.zero88.schedulerx.impl;

import java.time.Duration;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * The utility helps to a temporal value to the human-readable text.
 */
@Internal
public final class HumanReadableTimeFormat {

    private HumanReadableTimeFormat() { }

    public static String format(Duration duration) {
        return duration.toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase();
    }

}
