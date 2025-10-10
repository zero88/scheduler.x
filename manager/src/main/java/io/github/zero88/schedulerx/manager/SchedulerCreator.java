package io.github.zero88.schedulerx.manager;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.manager.ManagerExceptions.InitSchedulerException;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public interface SchedulerCreator {

    /**
     * Create scheduler from given data
     *
     * @param vertx vertx instance
     * @param data  schedule data in json object
     * @return the creation result
     * @throws InitSchedulerException if any error occurs during creation
     */
    Scheduler<? extends Trigger> create(@NotNull Vertx vertx, @NotNull JsonObject data) throws InitSchedulerException;

}
