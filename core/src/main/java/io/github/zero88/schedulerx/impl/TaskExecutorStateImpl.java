package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;

import org.jetbrains.annotations.NotNull;

public final class TaskExecutorStateImpl implements TaskExecutorStateInternal {

    private final AtomicReference<Instant> availableAt = new AtomicReference<>();
    private final AtomicLong tick = new AtomicLong(0);
    private final AtomicLong round = new AtomicLong(0);
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final AtomicBoolean executing = new AtomicBoolean(false);
    private final AtomicBoolean pending = new AtomicBoolean(true);
    private final AtomicReference<Entry<Long, Object>> data = new AtomicReference<>(new SimpleEntry<>(0L, null));
    private final AtomicReference<Entry<Long, Throwable>> error = new AtomicReference<>(new SimpleEntry<>(0L, null));
    private long timerId;

    @Override
    public Instant availableAt() {
        return availableAt.get();
    }

    @Override
    public long tick() {
        return tick.get();
    }

    @Override
    public long round() {
        return round.get();
    }

    @Override
    public boolean pending() {
        return pending.get();
    }

    @Override
    public boolean executing() {
        return executing.get();
    }

    @Override
    public boolean completed() {
        return completed.get();
    }

    @Override
    public Object lastData() {
        return Optional.ofNullable(data.get()).map(Entry::getValue).orElse(null);
    }

    @Override
    public Throwable lastError() {
        return Optional.ofNullable(error.get()).map(Entry::getValue).orElse(null);
    }

    @Override
    public long increaseTick() {
        return tick.incrementAndGet();
    }

    @Override
    public long increaseRound() {
        return round.incrementAndGet();
    }

    public long timerId() { return this.timerId; }

    @Override
    public @NotNull TaskExecutorStateInternal timerId(long timerId) {
        this.timerId = timerId;
        return this;
    }

    @Override
    public @NotNull TaskExecutorStateInternal markAvailable() {
        pending.set(false);
        availableAt.set(Instant.now());
        return this;
    }

    @Override
    public @NotNull TaskExecutorStateInternal markExecuting() {
        executing.set(true);
        return this;
    }

    @Override
    public @NotNull TaskExecutorStateInternal markIdle() {
        executing.set(false);
        return this;
    }

    @Override
    public @NotNull TaskExecutorStateInternal markCompleted() {
        completed.set(true);
        return this;
    }

    @Override
    public Object addData(long round, Object d) {
        return Optional.ofNullable(data.accumulateAndGet(new SimpleEntry<>(round, d), binaryOperator()))
                       .map(Entry::getValue)
                       .orElse(null);
    }

    @Override
    public Throwable addError(long round, Throwable err) {
        return Optional.ofNullable(error.accumulateAndGet(new SimpleEntry<>(round, err), binaryOperator()))
                       .map(Entry::getValue)
                       .orElse(null);
    }

    private <T> BinaryOperator<Entry<Long, T>> binaryOperator() {
        return BinaryOperator.maxBy(Comparator.comparingLong(Entry::getKey));
    }

}
