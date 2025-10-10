package io.github.zero88.schedulerx.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.manager.ScheduleManager;
import io.github.zero88.schedulerx.manager.SchedulingLoader;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Abstract base implementation of the Manager interface that provides common functionality
 * for managing schedulers. Concrete implementations should extend this class and implement
 * the abstract methods to provide specific data loading strategies.
 */
public abstract class AbstractScheduleManager<T extends SchedulingLoader> implements InternalSchedulerManager<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractScheduleManager.class);
    protected final ConcurrentMap<String, Scheduler<?>> schedulers = new ConcurrentHashMap<>();
    protected ObjectMapper mapper;
    protected Vertx vertx;
    protected JsonObject config;
    protected boolean isSetup = false;

    @Override
    public ScheduleManager setup(@NotNull Vertx vertx, @NotNull JsonObject config) {
        this.vertx   = Objects.requireNonNull(vertx, "Vertx instance is required");
        this.config  = Objects.requireNonNull(config, "Config is required");
        this.mapper  = DatabindCodec.mapper()
                                    .findAndRegisterModules()
                                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                                             SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        this.isSetup = true;
        LOGGER.info("Manager setup completed with config: " + this.config.encodePrettily());
        return this;
    }

    @Override
    public Future<Void> shutdown() {
        LOGGER.info("Shutting down " + schedulers.size() + " scheduler(s)...");

        List<Future<Void>> stopFutures = new ArrayList<>();

        for (Scheduler<?> scheduler : schedulers.values()) {
            Promise<Void> stopPromise = Promise.promise();
            try {
                scheduler.cancel();
                stopPromise.complete();
            } catch (Exception e) {
                LOGGER.warn("Error stopping scheduler", e);
                stopPromise.fail(e);
            }
            stopFutures.add(stopPromise.future());
        }

        if (stopFutures.isEmpty()) {
            return Future.succeededFuture();
        }

        return Future.all(stopFutures).<Void>mapEmpty().onSuccess(v -> {
            schedulers.clear();
            LOGGER.info("All schedulers shutdown completed");
        });
    }

    /**
     * Gets the current scheduler count.
     *
     * @return the number of active schedulers
     */
    public int getSchedulerCount() {
        return schedulers.size();
    }

    /**
     * Gets a scheduler by ID.
     *
     * @param schedulerId the scheduler ID
     * @return the scheduler or null if not found
     */
    public Scheduler<?> getScheduler(String schedulerId) {
        return schedulers.get(schedulerId);
    }

    /**
     * Reloads the manager by shutting down existing schedulers and loading new ones.
     *
     * @return a Future indicating completion
     */
    public Future<Void> reload() {
        LOGGER.info("Reloading scheduler configurations...");
        return shutdown().flatMap(v -> load()).flatMap(v -> run());
    }

    @Override
    public Future<Void> load() {
        if (!isSetup) {
            return Future.failedFuture(new IllegalStateException("Manager must be setup before loading"));
        }

        return loadData().compose(this::createSchedulersFromData)
                         .onSuccess(v -> LOGGER.info("Successfully loaded schedulers"))
                         .onFailure(t -> LOGGER.error("Failed to load schedulers", t));
    }


    /**
     * Load data from the specific source (file, database, etc.).
     * This method should be implemented by concrete classes.
     *
     * @return a Future containing the loaded data as JsonObject
     */
    protected abstract Future<JsonObject> loadData();

    /**
     * Creates schedulers from the loaded data.
     *
     * @param data the loaded data
     * @return a Future indicating completion
     */
    protected Future<Void> createSchedulersFromData(JsonObject data) {
        Promise<Void> promise = Promise.promise();

        try {
            if (data == null || data.isEmpty()) {
                LOGGER.warn("No scheduler data provided");
                promise.complete();
                return promise.future();
            }

            List<Future<String>> loadFutures = new ArrayList<>();

            // Handle both single scheduler config and array of configs
            if (data.containsKey("schedulers") && data.getValue("schedulers") instanceof JsonArray) {
                JsonArray schedulerConfigs = data.getJsonArray("schedulers");
                for (int i = 0; i < schedulerConfigs.size(); i++) {
                    JsonObject config = schedulerConfigs.getJsonObject(i);
                    loadFutures.add(createSchedulerFromConfig(config, "scheduler_" + i));
                }
            } else if (data.containsKey("trigger")) {
                // Single scheduler configuration
                loadFutures.add(createSchedulerFromConfig(data, "default_scheduler"));
            } else if (data.getValue("schedulers") instanceof JsonObject) {
                // Map of named schedulers
                JsonObject namedSchedulers = data.getJsonObject("schedulers");
                for (String name : namedSchedulers.fieldNames()) {
                    JsonObject config = namedSchedulers.getJsonObject(name);
                    // Use name as a fallback scheduler ID, but if config has external_id in jobData, it will take 
                    // precedence
                    loadFutures.add(createSchedulerFromConfig(config, name));
                }
            }

            if (loadFutures.isEmpty()) {
                LOGGER.warn("No valid scheduler configurations found in data");
                promise.complete();
                return promise.future();
            }

            Future.all(loadFutures).onComplete(ar -> {
                if (ar.succeeded()) {
                    LOGGER.info("Successfully created " + loadFutures.size() + " scheduler(s)");
                    promise.complete();
                } else {
                    LOGGER.error("Failed to create schedulers", ar.cause());
                    promise.fail(ar.cause());
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error during scheduler creation", e);
            promise.fail(e);
        }

        return promise.future();
    }
}
