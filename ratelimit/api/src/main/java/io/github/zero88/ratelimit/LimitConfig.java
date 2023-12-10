package io.github.zero88.ratelimit;

import java.time.Duration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents for the rate-limit configuration that enables you to manage the event traffic for your specific purpose,
 * such as limit the access to REST APIs, throttle event-bus messages on specific address.
 * <p/>
 * When the first event is going to start. This event fixes the time window. Each event consumes quota from the
 * current window until the time expires. When quota is exhausted, the resulting action depends on the policy:
 * <ul>
 * <li>Rate limiting rejects the event.</li>
 * <li>Throttling queues the event for retry.</li>
 * </ul>
 * When the time window closes, quota is reset and a new window of the same fixed size starts.
 *
 * @since 1.0.0
 */
public interface LimitConfig {

    /**
     * @return The maximum number of event that can be run in parallel during the sliding time window in
     *     {@link #timeWindow()}
     */
    int limit();

    /**
     * @return the sliding time window during which the number of allowed events should not exceed the value
     *     specified in {@link #limit()}
     */
    @NotNull Duration timeWindow();

    /**
     * @return The throttle configuration
     * @see ThrottleConfig
     */
    @Nullable ThrottleConfig throttleConfig();

}
