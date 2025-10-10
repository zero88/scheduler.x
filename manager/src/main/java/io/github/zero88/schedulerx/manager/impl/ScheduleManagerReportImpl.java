package io.github.zero88.schedulerx.manager.impl;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.manager.ScheduleManagerReport;
import io.github.zero88.schedulerx.trigger.Trigger;

public class ScheduleManagerReportImpl implements ScheduleManagerReport {

    @Override
    public void addSuccess(Scheduler<? extends Trigger> scheduler) {

    }

    @Override
    public void addError(@NotNull Exception error) {

    }

}
