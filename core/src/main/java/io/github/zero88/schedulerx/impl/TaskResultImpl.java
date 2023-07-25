package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import io.github.zero88.schedulerx.TaskResult;

public final class TaskResultImpl implements TaskResult {

    private final Instant unscheduledAt;
    private final Instant rescheduledAt;
    private final Instant availableAt;
    private final Instant triggeredAt;
    private final Instant executedAt;
    private final Instant finishedAt;
    private final Instant completedAt;
    private final long tick;
    private final long round;
    private final boolean completed;
    private final Throwable error;
    private final Object data;

    TaskResultImpl(TaskResultBuilder builder) {
        this.unscheduledAt = builder.unscheduledAt;
        this.rescheduledAt = builder.rescheduledAt;
        this.availableAt   = builder.availableAt;
        this.triggeredAt   = builder.triggeredAt;
        this.executedAt    = builder.executedAt;
        this.finishedAt    = builder.finishedAt;
        this.completedAt   = builder.completedAt;
        this.tick          = builder.tick;
        this.round         = builder.round;
        this.completed     = builder.completed;
        this.error         = builder.error;
        this.data          = builder.data;
    }

    public Instant unscheduledAt()            { return this.unscheduledAt; }

    public Instant rescheduledAt()            { return this.rescheduledAt; }

    public Instant availableAt()              { return this.availableAt; }

    public Instant triggeredAt()              { return this.triggeredAt; }

    public Instant executedAt()               { return this.executedAt; }

    public Instant finishedAt()               { return this.finishedAt; }

    public Instant completedAt()              { return this.completedAt; }

    public long tick()                        { return this.tick; }

    public long round()                       { return this.round; }

    public boolean isCompleted()              { return this.completed; }

    public Throwable error()                  { return this.error; }

    public Object data()                      { return this.data; }

}
