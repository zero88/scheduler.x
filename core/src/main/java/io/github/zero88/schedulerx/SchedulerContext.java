package io.github.zero88.schedulerx;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Vertx;

@Internal
interface SchedulerContext<TRIGGER extends Trigger, OUT> {

    /**
     * Vertx
     *
     * @return vertx
     */
    @NotNull Vertx vertx();

    /**
     * The trigger
     *
     * @return trigger
     */
    @NotNull TRIGGER trigger();

    /**
     * Defines a scheduling monitor
     *
     * @return scheduling monitor
     * @see SchedulingMonitor
     */
    @NotNull SchedulingMonitor<OUT> monitor();

}
