package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.TriggerCondition;
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
     * Create trigger context in {@link TriggerStatus#INITIALIZED} state
     *
     * @param triggerType the trigger type
     */
    public static @NotNull TriggerContext init(@NotNull String triggerType) { return init(triggerType, null); }

    /**
     * Create trigger context in {@link TriggerStatus#INITIALIZED} state
     *
     * @param triggerType the trigger type
     * @param info        the trigger context info
     */
    public static @NotNull <T> TriggerContext init(@NotNull String triggerType, @Nullable T info) {
        final Instant triggerAt = Instant.now();
        final TriggerCondition condition = () -> TriggerStatus.INITIALIZED;
        return new TriggerContext() {
            @Override
            public @NotNull String type() { return triggerType; }

            public @NotNull Instant triggerAt() { return triggerAt; }

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
     * Transition trigger context to {@link TriggerStatus#SKIP} state.
     *
     * @param ctx    the current trigger context
     * @param reason the transition reason
     */
    public static @NotNull TriggerContext skip(@NotNull TriggerContext ctx, @NotNull String reason) {
        return transition(ctx, TriggerStatus.SKIP, reason, null);
    }

    /**
     * Transition trigger context to {@link TriggerStatus#SKIP} state.
     *
     * @param ctx    the current trigger context
     * @param reason the transition reason
     * @param cause  the transition cause
     */
    public static @NotNull TriggerContext skip(@NotNull TriggerContext ctx, @NotNull String reason,
                                               @NotNull Throwable cause) {
        return transition(ctx, TriggerStatus.SKIP, reason, cause);
    }

    /**
     * Transition trigger context to {@link TriggerStatus#STOP} state.
     *
     * @param ctx    the current trigger context
     * @param reason the transition reason
     */
    public static @NotNull TriggerContext stop(@NotNull TriggerContext ctx, @Nullable String reason) {
        return transition(ctx, TriggerStatus.STOP, reason, null);
    }

    /**
     * Create a trigger context in {@link TriggerStatus#STOP} state.
     *
     * @param triggerType the trigger type
     * @param reason      the stopped reason
     */
    public static @NotNull TriggerContext stop(String triggerType, String reason) {
        return noop(triggerType, createCondition(TriggerStatus.STOP, reason, null));
    }

    /**
     * Create a trigger context in {@link TriggerStatus#FAILED} state.
     *
     * @param triggerType the trigger type
     * @param reason      the transition reason
     * @param cause       the failed cause
     */
    public static TriggerContext failed(String triggerType, String reason, @Nullable Throwable cause) {
        return noop(triggerType, createCondition(TriggerStatus.FAILED, reason, cause));
    }

    static @NotNull TriggerContext transition(@NotNull TriggerContext ctx, @NotNull TriggerStatus status,
                                              @Nullable String reason, @Nullable Throwable cause) {
        final TriggerCondition condition = createCondition(status, reason, cause);
        return new TriggerContext() {

            @Override
            public @NotNull String type() { return ctx.type(); }

            @Override
            public @NotNull Instant triggerAt() { return Objects.requireNonNull(ctx.triggerAt()); }

            @Override
            public @NotNull TriggerCondition condition() { return condition; }

            @Override
            public @Nullable Object info() { return ctx.info(); }
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

    @NotNull
    static TriggerContext noop(String triggerType, TriggerCondition condition) {
        return new TriggerContext() {

            @Override
            public @NotNull String type() { return triggerType; }

            @Override
            public @Nullable Instant triggerAt() { return null; }

            @Override
            public @NotNull TriggerCondition condition() { return condition; }
        };
    }

}
