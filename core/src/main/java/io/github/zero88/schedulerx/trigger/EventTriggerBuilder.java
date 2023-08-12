package io.github.zero88.schedulerx.trigger;

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

    public EventTriggerBuilder<T> setAddress(String address) {
        this.address = address;
        return this;
    }

    public EventTriggerBuilder<T> setPredicate(EventTriggerPredicate<T> predicate) {
        this.predicate = predicate;
        return this;
    }

    public EventTriggerBuilder<T> setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public EventTrigger<T> build() {
        return new EventTrigger<>(address, localOnly, predicate);
    }

}
