package io.github.zero88.schedulerx.trigger;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.eventbus.EventBus;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Represents for inspecting settings specific to a CronTrigger.
 * <p/>
 * An event trigger is a schedule that runs on receiving a specific event from a {@code Vert.x event-bus} distributed
 * messaging system, in short it acts as a consumer in
 *
 * @param <T> Type of event message
 * @since 2.0.0
 */
@JsonDeserialize(builder = EventTriggerBuilder.class)
public final class EventTrigger<T> implements Trigger {

    public static <T> EventTriggerBuilder<T> builder() { return new EventTriggerBuilder<>(); }

    private final boolean localOnly;
    private final @NotNull String address;
    private final @NotNull EventTriggerPredicate<T> predicate;

    EventTrigger(@NotNull String address, boolean localOnly, @NotNull EventTriggerPredicate<T> predicate) {
        this.localOnly = localOnly;
        this.address   = Objects.requireNonNull(address);
        this.predicate = Objects.requireNonNull(predicate);
    }

    /**
     * Declares a flag to check whether the event trigger listens to the event from the cluster or only local.
     *
     * @see EventBus#localConsumer(String)
     */
    public boolean isLocalOnly() { return localOnly; }

    /**
     * Declares the event-bus specific address
     */
    public @NotNull String getAddress() { return address; }

    /**
     * Declares the predicate to filter the incoming event
     *
     * @see EventTriggerPredicate
     */
    public @NotNull EventTriggerPredicate<T> getPredicate() { return predicate; }

    @Override
    public @NotNull String type() { return "event"; }

    @Override
    public @NotNull EventTrigger<T> validate() { return this; }

    @Override
    public @NotNull List<OffsetDateTime> preview(@NotNull PreviewParameter parameter) { return new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EventTrigger<?> that = (EventTrigger<?>) o;
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
        return "EventTrigger{" + "localOnly=" + localOnly + ", address='" + address + '\'' + ", predicate=" +
               predicate + '}';
    }

}
