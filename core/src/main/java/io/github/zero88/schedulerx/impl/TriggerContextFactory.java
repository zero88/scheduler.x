package io.github.zero88.schedulerx.impl;

import java.time.Instant;

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
     * Create trigger context in {@link TriggerStatus#SCHEDULED} state
     *
     * @param triggerType the trigger type
     */
    public static @NotNull TriggerContext scheduled(@NotNull String triggerType) {
        return create(triggerType, createCondition(TriggerStatus.SCHEDULED, ReasonCode.ON_SCHEDULE, null));
    }

    /**
     * Create trigger context in {@link TriggerStatus#KICKOFF} state
     *
     * @param triggerType the trigger type
     * @param tick        the tick
     */
    public static @NotNull TriggerTransitionContext kickoff(@NotNull String triggerType, long tick) {
        return kickoff(triggerType, tick, null);
    }

    /**
     * Create trigger context in {@link TriggerStatus#KICKOFF} state
     *
     * @param triggerType the trigger type
     * @param tick        the tick
     * @param info        the trigger context info
     */
    public static @NotNull <T> TriggerTransitionContext kickoff(@NotNull String triggerType, long tick,
                                                                @Nullable T info) {
        final Instant firedAt = Instant.now();
        final TriggerCondition condition = createCondition(TriggerStatus.KICKOFF, null, null);
        return new TriggerTransitionContext() {
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
    public static @NotNull TriggerTransitionContext ready(@NotNull TriggerTransitionContext ctx) {
        return transition(ctx, TriggerStatus.READY, null, null);
    }

    /**
     * Transition trigger context to {@link TriggerStatus#SKIPPED} state.
     *
     * @param ctx    the current trigger context
     * @param reason the transition reason
     */
    public static @NotNull TriggerTransitionContext skip(@NotNull TriggerTransitionContext ctx, @NotNull String reason) {
        return transition(ctx, TriggerStatus.SKIPPED, reason, null);
    }

    /**
     * Transition trigger context to {@link TriggerStatus#SKIPPED} state.
     *
     * @param ctx    the current trigger context
     * @param reason the transition reason
     * @param cause  the transition cause
     */
    public static @NotNull TriggerTransitionContext skip(@NotNull TriggerTransitionContext ctx, @NotNull String reason,
                                                         @NotNull Throwable cause) {
        return transition(ctx, TriggerStatus.SKIPPED, reason, cause);
    }

    /**
     * Transition trigger context to {@link TriggerStatus#STOPPED} state.
     *
     * @param ctx    the current trigger context
     * @param reason the transition reason
     */
    public static @NotNull TriggerTransitionContext stop(@NotNull TriggerTransitionContext ctx, @Nullable String reason) {
        return transition(ctx, TriggerStatus.STOPPED, reason, null);
    }

    /**
     * Create a trigger context in {@link TriggerStatus#STOPPED} state.
     *
     * @param triggerType the trigger type
     * @param reason      the stopped reason
     */
    public static @NotNull TriggerContext stop(String triggerType, String reason) {
        return create(triggerType, createCondition(TriggerStatus.STOPPED, reason, null));
    }

    /**
     * Create a trigger context in {@link TriggerStatus#ERROR} state.
     *
     * @param triggerType the trigger type
     * @param reason      the transition reason
     * @param cause       the failed cause
     */
    public static TriggerContext error(String triggerType, String reason, @Nullable Throwable cause) {
        return create(triggerType, createCondition(TriggerStatus.ERROR, reason, cause));
    }

    static @NotNull TriggerTransitionContext transition(@NotNull TriggerTransitionContext ctx,
                                                        @NotNull TriggerStatus status, @Nullable String reason,
                                                        @Nullable Throwable cause) {
        final TriggerCondition condition = createCondition(status, reason, cause);
        return new TriggerTransitionContext() {

            @Override
            public @NotNull String type() { return ctx.type(); }

            @Override
            public long tick() { return ctx.tick(); }

            @Override
            public @Nullable Instant firedAt() { return ctx.firedAt(); }

            @Override
            public @NotNull TriggerCondition condition() { return condition; }

            @Override
            public @Nullable Object info() { return ctx.info(); }
        };
    }

    static @NotNull TriggerContext create(String triggerType, TriggerCondition condition) {
        return new TriggerContext() {

            @Override
            public @NotNull String type() { return triggerType; }

            @Override
            public @NotNull TriggerCondition condition() { return condition; }
        };
    }

    static @NotNull TriggerContext convert(@NotNull TriggerContext internal) {
        return new TriggerContext() {
            @Override
            public @NotNull TriggerCondition condition() { return internal.condition(); }

            @Override
            public @NotNull String type() { return internal.type(); }

            @Override
            public @Nullable Object info() { return internal.info(); }
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
