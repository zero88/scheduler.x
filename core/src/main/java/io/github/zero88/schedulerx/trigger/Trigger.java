package io.github.zero88.schedulerx.trigger;

import java.time.OffsetDateTime;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.trigger.repr.TriggerRepresentation;
import io.github.zero88.schedulerx.trigger.repr.TriggerRepresentationServiceLoader;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents for inspecting settings specific to a Trigger, which is used to fire a <code>{@link Job}</code> at given
 * moments in time.
 *
 * @since 1.0.0
 */
public interface Trigger extends HasTriggerType, TriggerRepresentation {

    /**
     * Defines the trigger rule
     *
     * @return the trigger rule
     * @see TriggerRule
     * @since 2.0.0
     */
    @JsonProperty
    default @NotNull TriggerRule rule() {
        return TriggerRule.NOOP;
    }

    /**
     * Do validate trigger in runtime.
     *
     * @return this for fluent API
     * @throws IllegalArgumentException if any configuration is wrong
     * @since 2.0.0
     */
    @NotNull Trigger validate();

    /**
     * Simulate the next trigger times based on default preview parameter({@link PreviewParameter#byDefault()})
     *
     * @return the list of the next trigger time
     * @since 2.0.0
     */
    default @NotNull List<OffsetDateTime> preview() { return preview(PreviewParameter.byDefault()); }

    /**
     * Simulate the next trigger times based on given preview parameter
     *
     * @param parameter the preview parameter
     * @return the list of the next trigger time
     * @see PreviewParameter
     * @since 2.0.0
     */
    @NotNull List<OffsetDateTime> preview(@NotNull PreviewParameter parameter);

    /**
     * Serialize this trigger to json object that helps to persist in external system
     *
     * @return trigger in json
     * @since 2.0.0
     */
    @JsonValue
    default JsonObject toJson() {
        JsonObject json = JsonObject.of("type", type());
        if (rule() != TriggerRule.NOOP) { json.put("rule", rule()); }
        return json;
    }

    @Override
    default @NotNull String display(@Nullable String lang) {
        return TriggerRepresentationServiceLoader.getInstance().getProvider(type()).apply(this).display(lang);
    }

}
