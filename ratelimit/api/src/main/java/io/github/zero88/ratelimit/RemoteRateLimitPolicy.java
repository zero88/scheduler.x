package io.github.zero88.ratelimit;

import org.jetbrains.annotations.NotNull;

/**
 * Provides a light-weight proxy to rate-limit policy which state actually stored in external storage, like cache system
 * or relational database, etc.
 */
public interface RemoteRateLimitPolicy extends RateLimitPolicy {

    @NotNull Connector backend();

}
