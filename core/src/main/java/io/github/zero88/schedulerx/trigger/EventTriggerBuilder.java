package io.github.zero88.schedulerx.trigger;

import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents a builder that constructs {@link EventTrigger}
 *
 * @since 2.0.0
 */
@JsonPOJOBuilder(withPrefix = "")
public final class EventTriggerBuilder<T> {

    private String address;
    private boolean localOnly = false;
    private EventTriggerPredicate<T> predicate;

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
        return new EventTrigger<>(address, localOnly, predicate);
    }

}
