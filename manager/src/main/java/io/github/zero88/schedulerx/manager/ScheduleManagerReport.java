package io.github.zero88.schedulerx.manager;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.manager.impl.ScheduleManagerReportImpl;
import io.github.zero88.schedulerx.trigger.Trigger;

public interface ScheduleManagerReport {

    static ScheduleManagerReport create() {
        return new ScheduleManagerReportImpl();
    }

    void addSuccess(Scheduler<? extends Trigger> scheduler);

    void addError(@NotNull Exception error);

}
