package io.github.zero88.schedulerx.trigger;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;

final class EventTriggerImpl<T> implements EventTrigger<T> {

    private final TriggerRule rule;
    private final boolean localOnly;
    private final String address;
    private final EventTriggerPredicate<T> predicate;

    EventTriggerImpl(String address, boolean localOnly, EventTriggerPredicate<T> predicate, TriggerRule rule) {
        this.rule      = Optional.ofNullable(rule).orElse(TriggerRule.NOOP);
        this.localOnly = localOnly;
        this.address   = Objects.requireNonNull(
            Optional.ofNullable(address).filter(a -> !a.trim().isEmpty()).orElse(null),
            "The event address is required");
        this.predicate = Objects.requireNonNull(predicate, "The event trigger is required");
    }

    @Override
    public @NotNull TriggerRule rule() { return rule; }

    @Override
    public boolean isLocalOnly() { return localOnly; }

    @Override
    public @NotNull String getAddress() { return address; }

    @Override
    public @NotNull EventTriggerPredicate<T> getPredicate() { return predicate; }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EventTriggerImpl<?> that = (EventTriggerImpl<?>) o;
        if (localOnly != that.localOnly) { return false; }
        if (!address.equals(that.address)) { return false; }
        if (!predicate.equals(that.predicate)) { return false; }
        return rule().equals(that.rule());
    }

    @Override
    public int hashCode() {
        int result = (localOnly ? 1 : 0);
        result = 31 * result + address.hashCode();
        result = 31 * result + predicate.hashCode();
        result = 31 * result + rule().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "EventTrigger(address='" + address + '\'' + ", localOnly=" + localOnly + ", predicate='" + predicate +
               '\'' + ')';
    }

}
