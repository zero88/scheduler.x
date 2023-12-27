package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents a builder that constructs {@link EventTrigger}
 *
 * @since 2.0.0
 */
@Internal
@JsonPOJOBuilder(withPrefix = "")
@JsonIgnoreProperties(value = { "type" })
public final class EventTriggerBuilder<T> {

    private TriggerRule rule;
    private String address;
    private boolean localOnly = false;
    private EventTriggerPredicate<T> predicate;

    EventTriggerBuilder() { }

    public EventTriggerBuilder<T> rule(TriggerRule rule) {
        this.rule = rule;
        return this;
    }

    public EventTriggerBuilder<T> address(String address) {
        this.address = address;
        return this;
    }

    @JsonProperty("eventTriggerPredicate")
    public EventTriggerBuilder<T> predicate(EventTriggerPredicate<T> predicate) {
        this.predicate = predicate;
        return this;
    }

    public EventTriggerBuilder<T> localOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public EventTrigger<T> build() {
        return new EventTriggerImpl<>(address, localOnly, predicate, rule);
    }

}
