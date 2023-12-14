package io.github.zero88.schedulerx.impl;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.github.zero88.schedulerx.trigger.TriggerContext;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;
import io.vertx.core.Future;

@Internal
public abstract class AbstractTriggerEvaluator implements TriggerEvaluator {

    static TriggerEvaluator noop() {
        return new AbstractTriggerEvaluator() {
            @Override
            protected Future<TriggerContext> internalCheck(@NotNull Trigger trigger,
                                                           @NotNull TriggerContext triggerContext,
                                                           @Nullable Object externalId) {
                return Future.succeededFuture(triggerContext);
            }
        };
    }

    private TriggerEvaluator next;

    @Override
    public @NotNull Future<TriggerContext> beforeRun(@NotNull Trigger trigger, @NotNull TriggerContext triggerContext,
                                                     @Nullable Object externalId) {
        return this.internalCheck(trigger, triggerContext, externalId)
                   .flatMap(ctx -> next == null ? Future.succeededFuture(ctx) : next.beforeRun(trigger, ctx, externalId));
    }

    @Override
    public @NotNull TriggerEvaluator andThen(@Nullable TriggerEvaluator another) {
        this.next = another;
        return this;
    }

    protected abstract Future<TriggerContext> internalCheck(@NotNull Trigger trigger,
                                                            @NotNull TriggerContext triggerContext,
                                                            @Nullable Object externalId);

}
