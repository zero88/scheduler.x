package io.github.zero88.schedulerx;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import io.vertx.junit5.VertxTestContext;

/**
 * Represents for the scheduling monitor that able to do test assert.
 *
 * @param <OUT> Type of Result data
 * @apiNote This interface is renamed from {@code TaskExecutorAsserter} since {@code 2.0.0}
 * @see SchedulingMonitor
 * @since 2.0.0
 */
@SuppressWarnings("java:S5960")
public final class SchedulingAsserter<OUT> implements SchedulingMonitor<OUT> {

    @NotNull
    private final VertxTestContext testContext;
    @NotNull
    private final SchedulingMonitor<OUT> logMonitor;
    private final Consumer<ExecutionResult<OUT>> unableSchedule;
    private final Consumer<ExecutionResult<OUT>> schedule;
    private final Consumer<ExecutionResult<OUT>> misfire;
    private final Consumer<ExecutionResult<OUT>> each;
    private final Consumer<ExecutionResult<OUT>> completed;

    SchedulingAsserter(@NotNull VertxTestContext testContext, @Nullable SchedulingMonitor<OUT> logMonitor,
                       Consumer<ExecutionResult<OUT>> unableSchedule, Consumer<ExecutionResult<OUT>> schedule,
                       Consumer<ExecutionResult<OUT>> misfire, Consumer<ExecutionResult<OUT>> each,
                       Consumer<ExecutionResult<OUT>> completed) {
        this.testContext    = Objects.requireNonNull(testContext, "Vertx Test context is required");
        this.logMonitor     = Optional.ofNullable(logMonitor).orElse(SchedulingLogMonitor.create());
        this.unableSchedule = unableSchedule;
        this.schedule       = schedule;
        this.misfire        = misfire;
        this.each           = each;
        this.completed      = completed;
    }

    @Override
    public void onUnableSchedule(@NotNull ExecutionResult<OUT> result) {
        logMonitor.onUnableSchedule(result);
        verify(result, r -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertNotNull(result.unscheduledAt());
            Assertions.assertNotNull(result.triggerContext());
            Assertions.assertTrue(result.triggerContext().isError());
            Assertions.assertEquals(-1, result.tick());
            Assertions.assertEquals(-1, result.round());
            Assertions.assertNull(result.availableAt());
            Assertions.assertNull(result.rescheduledAt());
            Assertions.assertNull(result.triggeredAt());
            Assertions.assertNull(result.executedAt());
            Assertions.assertNull(result.finishedAt());
            Assertions.assertNull(result.completedAt());
            verify(result, unableSchedule);
        });
    }

    @Override
    public void onSchedule(@NotNull ExecutionResult<OUT> result) {
        logMonitor.onSchedule(result);
        verify(result, r -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertNotNull(result.triggerContext());
            Assertions.assertTrue(result.triggerContext().isScheduled());
            Assertions.assertNull(result.unscheduledAt());
            Assertions.assertNull(result.triggeredAt());
            Assertions.assertNull(result.executedAt());
            Assertions.assertNull(result.finishedAt());
            Assertions.assertNull(result.completedAt());
            verify(result, schedule);
        });
    }

    @Override
    public void onMisfire(@NotNull ExecutionResult<OUT> result) {
        logMonitor.onMisfire(result);
        verify(result, r -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertNotNull(result.firedAt());
            Assertions.assertNotNull(result.triggerContext());
            Assertions.assertNotNull(result.triggerContext().condition());
            Assertions.assertTrue(result.triggerContext().isSkipped());
            Assertions.assertNotNull(result.finishedAt());
            Assertions.assertNull(result.rescheduledAt());
            Assertions.assertNull(result.triggeredAt());
            Assertions.assertNull(result.executedAt());
            Assertions.assertNull(result.completedAt());
            verify(result, misfire);
        });
    }

    @Override
    public void onEach(@NotNull ExecutionResult<OUT> result) {
        logMonitor.onEach(result);
        verify(result, r -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertNotNull(result.firedAt());
            Assertions.assertNotNull(result.triggeredAt());
            Assertions.assertNotNull(result.triggerContext());
            Assertions.assertTrue(result.triggerContext().isExecuted());
            Assertions.assertNotNull(result.executedAt());
            Assertions.assertNotNull(result.finishedAt());
            Assertions.assertNull(result.rescheduledAt());
            verify(result, each);
        });
    }

    @Override
    public void onCompleted(@NotNull ExecutionResult<OUT> result) {
        logMonitor.onCompleted(result);
        verify(result, r -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertNotNull(result.completedAt());
            Assertions.assertNotNull(result.triggerContext());
            Assertions.assertTrue(result.triggerContext().isStopped());
            Assertions.assertNull(result.triggeredAt());
            Assertions.assertNull(result.executedAt());
            Assertions.assertNull(result.finishedAt());
            Assertions.assertNull(result.rescheduledAt());
            verify(result, completed);
            testContext.completeNow();
        });
    }

    private void verify(@NotNull ExecutionResult<OUT> result, Consumer<ExecutionResult<OUT>> verification) {
        try {
            if (Objects.nonNull(verification)) {
                testContext.verify(() -> verification.accept(result));
            }
        } catch (Exception ex) {
            testContext.failNow(ex);
        }
    }

    public static <OUT> SchedulingAsserterBuilder<OUT> builder() { return new SchedulingAsserterBuilder<>(); }

    public static <OUT> SchedulingMonitor<OUT> unableScheduleAsserter(@NotNull VertxTestContext testContext) {
        return unableScheduleAsserter(testContext, IllegalArgumentException.class, null);
    }

    public static <OUT> SchedulingMonitor<OUT> unableScheduleAsserter(@NotNull VertxTestContext testContext,
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
    public static <OUT> SchedulingMonitor<OUT> unableScheduleAsserter(@NotNull VertxTestContext testContext,
                                                                      @NotNull Class<? extends Exception> errorClazz,
                                                                      @Nullable String errorMsg) {
        return SchedulingAsserter.<OUT>builder().setTestContext(testContext).setUnableSchedule(result -> {
            testContext.verify(() -> {
                final Throwable cause = result.triggerContext().condition().cause();
                Assertions.assertNotNull(cause);
                Assertions.assertInstanceOf(errorClazz, cause);
                if (errorMsg != null) {
                    Assertions.assertEquals(errorMsg, cause.getMessage());
                }
            });
            testContext.completeNow();
        }).build();
    }

}
