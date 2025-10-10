package io.github.zero88.schedulerx.manager;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * The interface for managing schedulers in the scheduler.x framework.
 * This interface defines methods for setting up the datasource for schedulers, then start.
 *
 * @since 2.0.0
 */
public interface ScheduleManager {

    /**
     * Initializes the manager, this method should be called before any other methods.
     *
     * @return this for fluent API
     */
    ScheduleManager setup(@NotNull Vertx vertx, @NotNull JsonObject config);

    /**
     * Loading available schedulers from datasource then start them up.
     *
     * @return a Future indicating completion
     */
    Future<ScheduleManagerReport> run();

    /**
     * Cancels all the schedulers in the manager.
     *
     * @return a Future indicating completion
     */
    Future<Void> shutdown();

}
