package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.util.TimeZone;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Represents for inspecting settings specific to a CronTrigger.
 * <p/>
 * A cron trigger is a schedule that runs on specific different times that is defined with Unix 'cron-like' schedule
 * definitions.
 *
 * @since 1.0.0
 */
@JsonDeserialize(builder = CronTriggerBuilder.class)
public interface CronTrigger extends Trigger, TimebasedTrigger {

    String TRIGGER_TYPE = "cron";

    static CronTriggerBuilder builder() { return new CronTriggerBuilder(); }

    @Override
    default @NotNull String type() { return TRIGGER_TYPE; }

    /**
     * Returns the cron expression
     *
     * @see CronExpression
     */
    @NotNull String getExpression();

    /**
     * Returns the time zone for which the {@code cronExpression} of this {@code CronTrigger} will be resolved.
     */
    @NotNull TimeZone getTimeZone();

    @Override
    @NotNull CronTrigger validate();

    @Override
    default JsonObject toJson() {
        return Trigger.super.toJson()
                            .mergeIn(JsonObject.of("expression", getExpression(), "timeZone", getTimeZone().getID()));
    }

}
