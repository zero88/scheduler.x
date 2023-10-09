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

    EventTriggerImpl(@NotNull String address, boolean localOnly, @NotNull EventTriggerPredicate<T> predicate,
                     TriggerRule rule) {
        this.rule      = rule;
        this.localOnly = localOnly;
        this.address   = Objects.requireNonNull(address);
        this.predicate = Objects.requireNonNull(predicate);
    }

    @Override
    public @NotNull TriggerRule rule() { return Optional.ofNullable(rule).orElseGet(EventTrigger.super::rule); }

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
        if (localOnly != that.localOnly)
            return false;
        if (!address.equals(that.address))
            return false;
        return predicate.equals(that.predicate);
    }

    @Override
    public int hashCode() {
        int result = (localOnly ? 1 : 0);
        result = 31 * result + address.hashCode();
        result = 31 * result + predicate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "EventTrigger(address='" + address + '\'' + ", localOnly=" + localOnly + ", predicate='" + predicate +
               '\'' + ')';
    }

}
