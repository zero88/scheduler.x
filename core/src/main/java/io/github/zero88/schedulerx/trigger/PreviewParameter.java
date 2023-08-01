package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The preview parameter to generate the next trigger time of the specific trigger.
 *
 * @since 2.0.0
 */
public final class PreviewParameter {

    public static final int MAX_TIMES = 30;

    private Instant startedAt;
    private int times;
    private ZoneId timeZone;

    /**
     * Create default the preview parameter with startedAt is now and times = 10
     *
     * @return the preview parameter
     */
    public static PreviewParameter byDefault() {
        return new PreviewParameter().setTimes(10);
    }

    /**
     * @return the started at time to generate the preview results
     */
    public Instant getStartedAt() {
        return Optional.ofNullable(startedAt).orElseGet(Instant::now);
    }

    /**
     * Set the started at time to generate the preview results
     *
     * @param startedAt started at time
     * @return this for fluent API
     */
    public PreviewParameter setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
        return this;
    }

    /**
     * @return the number of a preview item, maximum is {@link #MAX_TIMES}
     */
    public int getTimes() {
        return Math.max(1, Math.min(times, MAX_TIMES));
    }

    /**
     * Set the number of a preview item in the preview results
     *
     * @param times the number of a preview item
     * @return this for fluent API
     */
    public PreviewParameter setTimes(int times) {
        this.times = times;
        return this;
    }

    /**
     * @return Get the expected timezone in the preview results
     */
    public @Nullable ZoneId getTimeZone() {
        return timeZone;
    }

    /**
     * Set an expected timezone in the preview results
     *
     * @param zoneId zoneId
     * @return this for fluent API
     */
    public PreviewParameter setTimeZone(ZoneId zoneId) {
        this.timeZone = zoneId;
        return this;
    }

    /**
     * Set an expected timezone in the preview results
     *
     * @param timeZone timezone
     * @return this for fluent API
     */
    public PreviewParameter setTimeZone(@NotNull TimeZone timeZone) {
        this.timeZone = Objects.requireNonNull(timeZone).toZoneId();
        return this;
    }

    /**
     * Set an expected timezone in the preview results
     *
     * @param zoneOffset zone offset
     * @return this for fluent API
     */
    public PreviewParameter setTimeZone(ZoneOffset zoneOffset) {
        this.timeZone = Objects.requireNonNull(zoneOffset).normalized();
        return this;
    }

}
