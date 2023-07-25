package io.github.zero88.schedulerx;

import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;

public class TaskExecutorAsserter implements TaskExecutorLogMonitor {

    @NotNull
    private final VertxTestContext testContext;
    private final Consumer<TaskResult> unableSchedule;
    private final Consumer<TaskResult> schedule;
    private final Consumer<TaskResult> misfire;
    private final Consumer<TaskResult> each;
    private final Consumer<TaskResult> completed;

    TaskExecutorAsserter(@NotNull VertxTestContext testContext, Consumer<TaskResult> unableSchedule,
                         Consumer<TaskResult> schedule, Consumer<TaskResult> misfire, Consumer<TaskResult> each,
                         Consumer<TaskResult> completed) {
        this.testContext    = Objects.requireNonNull(testContext, "Vertx Test context is required");
        this.unableSchedule = unableSchedule;
        this.schedule       = schedule;
        this.misfire        = misfire;
        this.each           = each;
        this.completed      = completed;
    }

    public static TaskExecutorAsserterBuilder builder() { return new TaskExecutorAsserterBuilder(); }

    @Override
    public void onUnableSchedule(@NotNull TaskResult result) {
        TaskExecutorLogMonitor.super.onUnableSchedule(result);
        verify(result, unableSchedule);
    }

    @Override
    public void onSchedule(@NotNull TaskResult result) {
        TaskExecutorLogMonitor.super.onSchedule(result);
        verify(result, schedule);
    }

    @Override
    public void onMisfire(@NotNull TaskResult result) {
        TaskExecutorLogMonitor.super.onMisfire(result);
        verify(result, misfire);
    }

    @Override
    public void onEach(@NotNull TaskResult result) {
        TaskExecutorLogMonitor.super.onEach(result);
        verify(result, each);
    }

    @Override
    public void onCompleted(@NotNull TaskResult result) {
        TaskExecutorLogMonitor.super.onCompleted(result);
        verify(result, r -> {
            completed.accept(r);
            testContext.completeNow();
        });
    }

    private void verify(@NotNull TaskResult result, Consumer<TaskResult> verification) {
        try {
            if (Objects.nonNull(verification)) {
                testContext.verify(() -> verification.accept(result));
            }
        } catch (Exception ex) {
            testContext.failNow(ex);
        }
    }

    static TaskExecutorMonitor unableScheduleAsserter(VertxTestContext testContext, Checkpoint checkpoint) {
        return TaskExecutorAsserter.builder().testContext(testContext).unableSchedule(result -> {
            checkpoint.flag();
            Assertions.assertNotNull(result.unscheduledAt());
            Assertions.assertNull(result.availableAt());
            Assertions.assertTrue(result.error() instanceof IllegalArgumentException);
            testContext.completeNow();
        }).build();
    }

    public static class TaskExecutorAsserterBuilder {

        private VertxTestContext testContext;
        private Consumer<TaskResult> unableSchedule;
        private Consumer<TaskResult> schedule;
        private Consumer<TaskResult> misfire;
        private Consumer<TaskResult> each;
        private Consumer<TaskResult> completed;

        public TaskExecutorAsserterBuilder testContext(@NotNull VertxTestContext testContext) {
            this.testContext = testContext;
            return this;
        }

        public TaskExecutorAsserterBuilder unableSchedule(Consumer<TaskResult> unableSchedule) {
            this.unableSchedule = unableSchedule;
            return this;
        }

        public TaskExecutorAsserterBuilder schedule(Consumer<TaskResult> schedule) {
            this.schedule = schedule;
            return this;
        }

        public TaskExecutorAsserterBuilder misfire(Consumer<TaskResult> misfire) {
            this.misfire = misfire;
            return this;
        }

        public TaskExecutorAsserterBuilder each(Consumer<TaskResult> each) {
            this.each = each;
            return this;
        }

        public TaskExecutorAsserterBuilder completed(Consumer<TaskResult> completed) {
            this.completed = completed;
            return this;
        }

        public TaskExecutorAsserter build() {
            return new TaskExecutorAsserter(this.testContext, this.unableSchedule, this.schedule, this.misfire,
                                            this.each, this.completed);
        }

    }

}
