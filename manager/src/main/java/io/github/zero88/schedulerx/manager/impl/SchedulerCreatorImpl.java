package io.github.zero88.schedulerx.manager.impl;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.CronScheduler;
import io.github.zero88.schedulerx.EventScheduler;
import io.github.zero88.schedulerx.IntervalScheduler;
import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.JobFactory;
import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.SchedulerBuilder;
import io.github.zero88.schedulerx.TimeoutPolicy;
import io.github.zero88.schedulerx.manager.ManagerExceptions.InitSchedulerException;
import io.github.zero88.schedulerx.manager.SchedulerCreator;
import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.github.zero88.schedulerx.trigger.EventTrigger;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class SchedulerCreatorImpl implements SchedulerCreator {

    @Override
    public Scheduler<? extends Trigger> create(@NotNull Vertx vertx, @NotNull JsonObject schedulerJson) throws InitSchedulerException {
        try {
            return createScheduler(vertx, schedulerJson);
        } catch (Exception e) {
            throw new InitSchedulerException(e);
        }
    }

    /**
     * Creates a scheduler from configuration data.
     *
     * @param vertx         vertx instance
     * @param schedulerJson the scheduler configuration
     * @return a Future containing the scheduler ID
     */
    protected Scheduler<? extends Trigger> createScheduler(Vertx vertx, JsonObject schedulerJson) {
        if (!schedulerJson.containsKey("trigger")) {
            throw new IllegalArgumentException("Trigger configuration is required");
        }

        Trigger trigger = createTrigger(schedulerJson.getJsonObject("trigger"));
        Job<Object, Object> job = JobFactory.getInstance().create(schedulerJson.getString("jobClass"));
        JobData<Object> jobData = createJobData(schedulerJson.getJsonObject("jobData"));
        TimeoutPolicy timeoutPolicy = TimeoutPolicy.create(schedulerJson.getJsonObject("timeoutPolicy"));
        //noinspection unchecked
        return createBuilderFromTrigger(trigger).setVertx(vertx)
                                                .setTrigger(trigger)
                                                .setJob(job)
                                                .setJobData(jobData)
                                                .setTimeoutPolicy(timeoutPolicy)
                                                //  .setMonitor(monitor)
                                                .build();
    }

    /**
     * Creates a trigger based on type and configuration.
     */
    protected Trigger createTrigger(JsonObject triggerConfig) {
        String type = triggerConfig.getString("type");
        try {
            return switch (type.toLowerCase()) {
                case "interval" -> triggerConfig.mapTo(IntervalTrigger.class);
                case "cron" -> triggerConfig.mapTo(CronTrigger.class);
                case "event" -> triggerConfig.mapTo(EventTrigger.class);
                default -> throw new IllegalArgumentException("Unsupported trigger type: " + type);
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid trigger configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Creates job data from configuration.
     */
    protected JobData<Object> createJobData(JsonObject jobDataConfig) {
        Object externalId = jobDataConfig.getValue("external_id");
        jobDataConfig.remove("external_id");
        return JobData.create(jobDataConfig, externalId);
    }

    @SuppressWarnings("rawtypes")
    private SchedulerBuilder createBuilderFromTrigger(Trigger trigger) {
        return switch (trigger.type().toLowerCase()) {
            case "interval" -> IntervalScheduler.builder();
            case "cron" -> CronScheduler.builder();
            case "event" -> EventScheduler.builder();
            default -> throw new IllegalArgumentException("Unsupported trigger type: " + trigger.type());
        };
    }

}
