package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.TriggerCondition;
import io.github.zero88.schedulerx.trigger.TriggerCondition.ReasonCode;
import io.github.zero88.schedulerx.trigger.TriggerCondition.TriggerStatus;
import io.github.zero88.schedulerx.trigger.TriggerContext;

/**
 * The factory to create trigger context.
 *
 * @since 2.0.0
 */
@Internal
public final class TriggerContextFactory {

    private TriggerContextFactory() { }

    /**
     * Create trigger context in {@link TriggerStatus#SCHEDULED} state with {@link ReasonCode#ON_SCHEDULE} reason
     *
     * @param triggerType the trigger type
     */
    public static @NotNull TriggerContext scheduled(@NotNull String triggerType) {
        return create(triggerType, 0, createCondition(TriggerStatus.SCHEDULED, ReasonCode.ON_SCHEDULE, null));
    }

    /**
     * Create trigger context in {@link TriggerStatus#SCHEDULED} state with {@link ReasonCode#ON_RESCHEDULE} reason
     *
     * @param triggerType the trigger type
     * @param tick        the tick at the rescheduled time
     */
    public static @NotNull TriggerContext rescheduled(@NotNull String triggerType, long tick) {
        return create(triggerType, tick, createCondition(TriggerStatus.SCHEDULED, ReasonCode.ON_RESCHEDULE, null));
    }

    /**
     * Create a trigger context in {@link TriggerStatus#ERROR} state.
     *
     * @param triggerType the trigger type
     * @param reason      the transition reason
     * @param cause       the failed cause
     */
    public static TriggerContext error(String triggerType, String reason, @Nullable Throwable cause) {
        return create(triggerType, -1, createCondition(TriggerStatus.ERROR, reason, cause));
    }

    /**
     * Create a trigger context in {@link TriggerStatus#STOPPED} state with {@link ReasonCode#ON_CANCEL} reason.
     *
     * @param triggerType the trigger type
     * @param tick        the current tick
     */
    public static @NotNull TriggerContext cancel(String triggerType, long tick) {
        return create(triggerType, tick, createCondition(TriggerStatus.STOPPED, ReasonCode.ON_CANCEL, null));
    }

    /**
     * Create trigger context in {@link TriggerStatus#KICKOFF} state
     *
     * @param triggerType the trigger type
     * @param tick        the tick
     */
    public static @NotNull TriggerContext kickoff(@NotNull String triggerType, long tick) {
        return kickoff(triggerType, tick, null);
    }

    /**
     * Create trigger context in {@link TriggerStatus#KICKOFF} state
     *
     * @param triggerType the trigger type
     * @param tick        the tick
     * @param info        the trigger context info
     */
    public static @NotNull <T> TriggerContext kickoff(@NotNull String triggerType, long tick, @Nullable T info) {
        final Instant firedAt = Instant.now();
        final TriggerCondition condition = createCondition(TriggerStatus.KICKOFF, null, null);
        return new TriggerContext() {
            @Override
            public @NotNull String type() { return triggerType; }

            @Override
            public long tick() { return tick; }

            public @NotNull Instant firedAt() { return firedAt; }

            @Override
            public @NotNull TriggerCondition condition() { return condition; }

            @Override
            public T info() { return info; }
        };
    }

    /**
     * Transition trigger context to {@link TriggerStatus#READY} state.
     *
     * @param ctx the current trigger context
     */
    public static @NotNull TriggerContext ready(@NotNull TriggerContext ctx) {
        return transition(ctx, TriggerStatus.READY, null, null);
    }

    /**
     * Transition trigger context to {@link TriggerStatus#EXECUTED} state.
     *
     * @param ctx the current trigger context
     */
    public static @NotNull TriggerContext executed(@NotNull TriggerContext ctx) {
        return transition(ctx, TriggerStatus.EXECUTED, null, null);
    }

    /**
     * Transition trigger context to {@link TriggerStatus#SKIPPED} state.
     *
     * @param ctx    the current trigger context
     * @param reason the transition reason
     */
    public static @NotNull TriggerContext skip(@NotNull TriggerContext ctx, @NotNull String reason) {
        return transition(ctx, TriggerStatus.SKIPPED, reason, null);
    }

    /**
     * Transition trigger context to {@link TriggerStatus#SKIPPED} state.
     *
     * @param ctx    the current trigger context
     * @param reason the transition reason
     * @param cause  the transition cause
     */
    public static @NotNull TriggerContext skip(@NotNull TriggerContext ctx, @NotNull String reason,
                                               @NotNull Throwable cause) {
        return transition(ctx, TriggerStatus.SKIPPED, reason, cause);
    }

    /**
     * Transition trigger context to {@link TriggerStatus#STOPPED} state.
     *
     * @param ctx    the current trigger context
     * @param reason the transition reason
     */
    public static @NotNull TriggerContext stop(@NotNull TriggerContext ctx, @Nullable String reason) {
        return transition(ctx, TriggerStatus.STOPPED, reason, null);
    }

    static @NotNull TriggerContext transition(@NotNull TriggerContext ctx, @NotNull TriggerStatus status,
                                              @Nullable String reason, @Nullable Throwable cause) {
        final Instant firedAt = Objects.requireNonNull(ctx.firedAt(),
                                                       "A fired at time is required in trigger transition");
        final TriggerCondition condition = createCondition(status, reason, cause);
        return new TriggerContext() {

            @Override
            public @NotNull String type() { return ctx.type(); }

            @Override
            public long tick() { return ctx.tick(); }

            @Override
            public @NotNull Instant firedAt() { return firedAt; }

            @Override
            public @NotNull TriggerCondition condition() { return condition; }

            @Override
            public @Nullable Object info() { return ctx.info(); }
        };
    }

    static @NotNull TriggerContext create(String triggerType, long tick, TriggerCondition condition) {
        return new TriggerContext() {
            @Override
            public long tick() { return tick; }

            @Override
            public @Nullable Instant firedAt() { return null; }

            @Override
            public @NotNull String type() { return triggerType; }

            @Override
            public @NotNull TriggerCondition condition() { return condition; }
        };
    }

    static @NotNull TriggerCondition createCondition(@NotNull TriggerStatus status, @Nullable String reason,
                                                     @Nullable Throwable cause) {
        return new TriggerCondition() {
            @Override
            public @NotNull TriggerStatus status() { return status; }

            @Override
            public @Nullable String reasonCode() { return reason; }

            @Override
            public @Nullable Throwable cause() { return cause; }
        };
    }

}
