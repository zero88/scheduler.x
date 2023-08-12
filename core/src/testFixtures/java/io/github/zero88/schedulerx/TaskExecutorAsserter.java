package io.github.zero88.schedulerx;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import io.vertx.junit5.VertxTestContext;

/**
 * Represents for the executor monitor that able to do test assert.
 *
 * @param <OUTPUT> Type of Result data
 * @see TaskExecutorMonitor
 * @since 1.0.0
 */
@SuppressWarnings("java:S5960")
public final class TaskExecutorAsserter<OUTPUT> implements TaskExecutorMonitor<OUTPUT> {

    @NotNull
    private final VertxTestContext testContext;
    @NotNull
    private final TaskExecutorMonitor<OUTPUT> logMonitor;
    private final Consumer<TaskResult<OUTPUT>> unableSchedule;
    private final Consumer<TaskResult<OUTPUT>> schedule;
    private final Consumer<TaskResult<OUTPUT>> misfire;
    private final Consumer<TaskResult<OUTPUT>> each;
    private final Consumer<TaskResult<OUTPUT>> completed;

    TaskExecutorAsserter(@NotNull VertxTestContext testContext, @Nullable TaskExecutorMonitor<OUTPUT> logMonitor,
                         Consumer<TaskResult<OUTPUT>> unableSchedule, Consumer<TaskResult<OUTPUT>> schedule,
                         Consumer<TaskResult<OUTPUT>> misfire, Consumer<TaskResult<OUTPUT>> each,
                         Consumer<TaskResult<OUTPUT>> completed) {
        this.testContext    = Objects.requireNonNull(testContext, "Vertx Test context is required");
        this.logMonitor     = Optional.ofNullable(logMonitor).orElse(TaskExecutorLogMonitor.create());
        this.unableSchedule = unableSchedule;
        this.schedule       = schedule;
        this.misfire        = misfire;
        this.each           = each;
        this.completed      = completed;
    }

    @Override
    public void onUnableSchedule(@NotNull TaskResult<OUTPUT> result) {
        logMonitor.onUnableSchedule(result);
        verify(result, r -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertNotNull(result.unscheduledAt());
            Assertions.assertNull(result.availableAt());
            Assertions.assertNull(result.rescheduledAt());
            Assertions.assertNull(result.triggeredAt());
            Assertions.assertNull(result.executedAt());
            Assertions.assertNull(result.finishedAt());
            Assertions.assertNull(result.completedAt());
        });
        verify(result, unableSchedule);
    }

    @Override
    public void onSchedule(@NotNull TaskResult<OUTPUT> result) {
        logMonitor.onSchedule(result);
        verify(result, r -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertNull(result.unscheduledAt());
            Assertions.assertNull(result.triggeredAt());
            Assertions.assertNull(result.executedAt());
            Assertions.assertNull(result.finishedAt());
            Assertions.assertNull(result.completedAt());
            verify(result, schedule);
        });
    }

    @Override
    public void onMisfire(@NotNull TaskResult<OUTPUT> result) {
        logMonitor.onMisfire(result);
        verify(result, r -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertNotNull(result.triggeredAt());
            Assertions.assertNull(result.rescheduledAt());
            Assertions.assertNull(result.executedAt());
            Assertions.assertNull(result.finishedAt());
            Assertions.assertNull(result.completedAt());
            verify(result, misfire);
        });
    }

    @Override
    public void onEach(@NotNull TaskResult<OUTPUT> result) {
        logMonitor.onEach(result);
        verify(result, r -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertNotNull(result.triggeredAt());
            Assertions.assertNotNull(result.executedAt());
            Assertions.assertNotNull(result.finishedAt());
            Assertions.assertNull(result.rescheduledAt());
            verify(result, each);
        });
    }

    @Override
    public void onCompleted(@NotNull TaskResult<OUTPUT> result) {
        logMonitor.onCompleted(result);
        verify(result, r -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertNotNull(result.completedAt());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertNull(result.triggeredAt());
            Assertions.assertNull(result.executedAt());
            Assertions.assertNull(result.finishedAt());
            Assertions.assertNull(result.rescheduledAt());
            verify(result, completed);
            testContext.completeNow();
        });
    }

    private void verify(@NotNull TaskResult<OUTPUT> result, Consumer<TaskResult<OUTPUT>> verification) {
        try {
            if (Objects.nonNull(verification)) {
                testContext.verify(() -> verification.accept(result));
            }
        } catch (Exception ex) {
            testContext.failNow(ex);
        }
    }

    public static <OUT> TaskExecutorAsserterBuilder<OUT> builder() { return new TaskExecutorAsserterBuilder<>(); }

    public static <OUT> TaskExecutorMonitor<OUT> unableScheduleAsserter(@NotNull VertxTestContext testContext) {
        return unableScheduleAsserter(testContext, IllegalArgumentException.class, null);
    }

    public static <OUT> TaskExecutorMonitor<OUT> unableScheduleAsserter(@NotNull VertxTestContext testContext,
                                                                        @NotNull String errorMsg) {
        return unableScheduleAsserter(testContext, IllegalArgumentException.class, errorMsg);
    }

    /**
     * @param <OUT>       Type of output
     * @param testContext testContext
     * @param errorClazz  error class
     * @param errorMsg    error message
     * @return an asserter
     * @since 2.0.0
     */
    public static <OUT> TaskExecutorMonitor<OUT> unableScheduleAsserter(@NotNull VertxTestContext testContext,
                                                                        @NotNull Class<? extends Exception> errorClazz,
                                                                        @Nullable String errorMsg) {
        return TaskExecutorAsserter.<OUT>builder().setTestContext(testContext).setUnableSchedule(result -> {
            testContext.verify(() -> {
                Assertions.assertNull(result.availableAt());
                Assertions.assertNotNull(result.externalId());
                Assertions.assertNotNull(result.unscheduledAt());
                Assertions.assertNotNull(result.error());
                Assertions.assertInstanceOf(errorClazz, result.error());
                if (errorMsg != null) {
                    Assertions.assertEquals(errorMsg, result.error().getMessage());
                }
            });
            testContext.completeNow();
        }).build();
    }

}
