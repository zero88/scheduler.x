package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import io.github.zero88.schedulerx.TaskResult;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Builder
@Accessors(fluent = true)
public class TaskResultImpl implements TaskResult {

    private final Instant unscheduledAt;
    private final Instant rescheduledAt;
    private final Instant availableAt;
    private final Instant triggeredAt;
    private final Instant executedAt;
    private final Instant finishedAt;
    private final Instant completedAt;
    private final long tick;
    private final long round;
    @Accessors
    private final boolean completed;
    private final Throwable error;
    private final Object data;

}
