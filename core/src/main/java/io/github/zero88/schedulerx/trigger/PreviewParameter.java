package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.TimeZone;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.DefaultOptions;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;

/**
 * The preview parameter to generate the next trigger time of the specific trigger.
 *
 * @since 2.0.0
 */
public final class PreviewParameter {

    private int times;
    private Instant startedAt = Instant.now();
    private ZoneId timeZone;
    private TriggerRule rule = TriggerRule.NOOP;

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
    public @NotNull Instant getStartedAt() {
        return startedAt;
    }

    /**
     * Set the started at time to generate the preview results
     *
     * @param startedAt started at time
     * @return this for fluent API
     */
    public @NotNull PreviewParameter setStartedAt(Instant startedAt) {
        if (startedAt != null) {
            this.startedAt = startedAt;
        }
        return this;
    }

    /**
     * @return the number of a preview item, maximum is {@link DefaultOptions#maxTriggerPreviewCount}
     */
    public int getTimes() {
        return Math.max(1, Math.min(times, DefaultOptions.getInstance().maxTriggerPreviewCount));
    }

    /**
     * Set the number of a preview item in the preview results
     *
     * @param times the number of a preview item
     * @return this for fluent API
     */
    public @NotNull PreviewParameter setTimes(int times) {
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

    /**
     * @return the trigger rule
     * @see TriggerRule
     */
    public @NotNull TriggerRule getRule() {
        return rule;
    }

    /**
     * Set a trigger rule in the preview parameter
     *
     * @param rule trigger rule
     * @return this for fluent API
     */
    public @NotNull PreviewParameter setRule(TriggerRule rule) {
        if (rule != null) {
            this.rule = rule;
        }
        return this;
    }

}
