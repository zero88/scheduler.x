package io.github.zero88.schedulerx.impl;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.github.zero88.schedulerx.trigger.TriggerContext;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;
import io.vertx.core.Future;

@Internal
public class DefaultTriggerEvaluator implements TriggerEvaluator {

    static TriggerEvaluator noop() {
        return new DefaultTriggerEvaluator();
    }

    private TriggerEvaluator next;

    @Override
    public final @NotNull Future<TriggerContext> beforeTrigger(@NotNull Trigger trigger,
                                                               @NotNull TriggerContext triggerContext,
                                                               @Nullable Object externalId) {
        return this.internalBeforeTrigger(trigger, triggerContext, externalId)
                   .flatMap(c -> next == null ? Future.succeededFuture(c) : next.beforeTrigger(trigger, c, externalId));
    }

    @Override
    public final @NotNull Future<TriggerContext> afterTrigger(@NotNull Trigger trigger,
                                                              @NotNull TriggerContext triggerContext,
                                                              @Nullable Object externalId, long round) {
        // @formatter:off
        return this.internalAfterTrigger(trigger, triggerContext, externalId, round )
                   .flatMap(c -> next == null ? Future.succeededFuture(c) : next.afterTrigger(trigger, c, externalId, round));
        // @formatter:on
    }

    @Override
    public final @NotNull TriggerEvaluator andThen(@Nullable TriggerEvaluator another) {
        this.next = another;
        return this;
    }

    protected Future<TriggerContext> internalBeforeTrigger(@NotNull Trigger trigger,
                                                           @NotNull TriggerContext triggerContext,
                                                           @Nullable Object externalId) {
        return Future.succeededFuture(triggerContext);
    }

    protected Future<TriggerContext> internalAfterTrigger(@NotNull Trigger trigger,
                                                          @NotNull TriggerContext triggerContext,
                                                          @Nullable Object externalId, long round) {
        return Future.succeededFuture(triggerContext);
    }

}
