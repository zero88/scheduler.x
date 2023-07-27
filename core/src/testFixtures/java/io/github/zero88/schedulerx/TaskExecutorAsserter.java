package io.github.zero88.schedulerx;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;

/**
 * Represents for the executor monitor that able to do test assert.
 *
 * @see TaskExecutorMonitor
 * @since 1.0.0
 */
public final class TaskExecutorAsserter implements TaskExecutorMonitor {

    @NotNull
    private final VertxTestContext testContext;
    @NotNull
    private final TaskExecutorMonitor logMonitor;
    private final Consumer<TaskResult> unableSchedule;
    private final Consumer<TaskResult> schedule;
    private final Consumer<TaskResult> misfire;
    private final Consumer<TaskResult> each;
    private final Consumer<TaskResult> completed;

    TaskExecutorAsserter(@NotNull VertxTestContext testContext, @Nullable TaskExecutorMonitor logMonitor,
                         Consumer<TaskResult> unableSchedule, Consumer<TaskResult> schedule,
                         Consumer<TaskResult> misfire, Consumer<TaskResult> each, Consumer<TaskResult> completed) {
        this.testContext    = Objects.requireNonNull(testContext, "Vertx Test context is required");
        this.logMonitor     = Optional.ofNullable(logMonitor).orElse(TaskExecutorLogMonitor.LOG_MONITOR);
        this.unableSchedule = unableSchedule;
        this.schedule       = schedule;
        this.misfire        = misfire;
        this.each           = each;
        this.completed      = completed;
    }

    public static TaskExecutorAsserterBuilder builder() { return new TaskExecutorAsserterBuilder(); }

    @Override
    public void onUnableSchedule(@NotNull TaskResult result) {
        logMonitor.onUnableSchedule(result);
        verify(result, unableSchedule);
    }

    @Override
    public void onSchedule(@NotNull TaskResult result) {
        logMonitor.onSchedule(result);
        verify(result, schedule);
    }

    @Override
    public void onMisfire(@NotNull TaskResult result) {
        logMonitor.onMisfire(result);
        verify(result, misfire);
    }

    @Override
    public void onEach(@NotNull TaskResult result) {
        logMonitor.onEach(result);
        verify(result, each);
    }

    @Override
    public void onCompleted(@NotNull TaskResult result) {
        logMonitor.onCompleted(result);
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

    @SuppressWarnings("java:S5960")
    public static TaskExecutorMonitor unableScheduleAsserter(VertxTestContext testContext, Checkpoint checkpoint) {
        return TaskExecutorAsserter.builder().setTestContext(testContext).setUnableSchedule(result -> {
            checkpoint.flag();
            Assertions.assertNotNull(result.unscheduledAt());
            Assertions.assertNull(result.availableAt());
            Assertions.assertTrue(result.error() instanceof IllegalArgumentException);
            testContext.completeNow();
        }).build();
    }

    public static class TaskExecutorAsserterBuilder {

        private VertxTestContext testContext;
        private TaskExecutorMonitor logMonitor;
        private Consumer<TaskResult> unableSchedule;
        private Consumer<TaskResult> schedule;
        private Consumer<TaskResult> misfire;
        private Consumer<TaskResult> each;
        private Consumer<TaskResult> completed;

        /**
         * Set Vertx test context
         *
         * @param testContext test context
         * @return this for fluent API
         * @see VertxTestContext
         */
        public TaskExecutorAsserterBuilder setTestContext(@NotNull VertxTestContext testContext) {
            this.testContext = testContext;
            return this;
        }

        /**
         * Set log monitor
         *
         * @param logMonitor a log monitor
         * @return this for fluent API
         * @see TaskExecutorMonitor
         * @since 2.0.0
         */
        public TaskExecutorAsserterBuilder setLogMonitor(TaskExecutorMonitor logMonitor) {
            this.logMonitor = logMonitor;
            return this;
        }

        /**
         * Set a task result verification when unable to schedule task
         *
         * @param unableSchedule a verification when unable to schedule task
         * @return this for fluent API
         * @see TaskResult
         * @see TaskExecutorMonitor#onUnableSchedule(TaskResult)
         */
        public TaskExecutorAsserterBuilder setUnableSchedule(Consumer<TaskResult> unableSchedule) {
            this.unableSchedule = unableSchedule;
            return this;
        }

        /**
         * Set a task result verification when schedule task
         *
         * @param schedule a verification when schedule task
         * @return this for fluent API
         * @see TaskResult
         * @see TaskExecutorMonitor#onSchedule(TaskResult)
         */
        public TaskExecutorAsserterBuilder setSchedule(Consumer<TaskResult> schedule) {
            this.schedule = schedule;
            return this;
        }

        /**
         * Set a task result verification when misfire task
         *
         * @param misfire a verification when misfire task
         * @return this for fluent API
         * @see TaskResult
         * @see TaskExecutorMonitor#onMisfire(TaskResult)
         */
        public TaskExecutorAsserterBuilder setMisfire(Consumer<TaskResult> misfire) {
            this.misfire = misfire;
            return this;
        }

        /**
         * Set a task result verification when each round is finished
         *
         * @param each a verification when each round is finished of schedule
         * @return this for fluent API
         * @see TaskResult
         * @see TaskExecutorMonitor#onEach(TaskResult)
         */
        public TaskExecutorAsserterBuilder setEach(Consumer<TaskResult> each) {
            this.each = each;
            return this;
        }

        /**
         * Set a task result verification when execution is completed
         *
         * @param completed a verification when execution is completed
         * @return this for fluent API
         * @see TaskResult
         * @see TaskExecutorMonitor#onCompleted(TaskResult)
         */
        public TaskExecutorAsserterBuilder setCompleted(Consumer<TaskResult> completed) {
            this.completed = completed;
            return this;
        }

        /**
         * Build an asserter
         *
         * @return TaskExecutorAsserter
         */
        public TaskExecutorAsserter build() {
            return new TaskExecutorAsserter(testContext, logMonitor, unableSchedule, schedule, misfire, each,
                                            completed);
        }

    }

}
