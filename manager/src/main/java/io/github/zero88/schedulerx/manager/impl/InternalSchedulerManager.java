package io.github.zero88.schedulerx.manager.impl;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.manager.ScheduleManager;
import io.github.zero88.schedulerx.manager.ScheduleManagerReport;
import io.github.zero88.schedulerx.manager.SchedulerCreator;
import io.github.zero88.schedulerx.manager.SchedulingLoader;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public interface InternalSchedulerManager<T extends SchedulingLoader> extends ScheduleManager {

    @Override
    default Future<ScheduleManagerReport> run() {
        ScheduleManagerReport report = ScheduleManagerReport.create();
        SchedulerCreator creator = creator();
        return loader().load().map(loader -> {
            JsonObject data;
            while ((data = loader.next()) != null) {
                try {
                    final Scheduler<? extends Trigger> scheduler = creator.create(vertx(), data);
                    // TODO: start by worker
                    scheduler.start();
                    report.addSuccess(scheduler);
                } catch (Exception e) {
                    report.addError(e);
                }
            }
            return report;
        });
    }

    @NotNull SchedulerCreator creator();

    @NotNull T loader();

    @NotNull Vertx vertx();

}
