package io.github.zero88.vertx.scheduler;

import java.util.Objects;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;

import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;

import lombok.Builder;
import lombok.NonNull;

@Builder
public class TaskExecutorAsserter implements TaskExecutorLogMonitor {

    @NonNull
    private final VertxTestContext testContext;
    private final Consumer<TaskResult> unableSchedule;
    private final Consumer<TaskResult> schedule;
    private final Consumer<TaskResult> misfire;
    private final Consumer<TaskResult> each;
    private final Consumer<TaskResult> completed;

    @Override
    public void onUnableSchedule(@NonNull TaskResult result) {
        TaskExecutorLogMonitor.super.onUnableSchedule(result);
        verify(result, unableSchedule);
    }

    @Override
    public void onSchedule(@NonNull TaskResult result) {
        TaskExecutorLogMonitor.super.onSchedule(result);
        verify(result, schedule);
    }

    @Override
    public void onMisfire(@NonNull TaskResult result) {
        TaskExecutorLogMonitor.super.onMisfire(result);
        verify(result, misfire);
    }

    @Override
    public void onEach(@NonNull TaskResult result) {
        TaskExecutorLogMonitor.super.onEach(result);
        verify(result, each);
    }

    @Override
    public void onCompleted(@NonNull TaskResult result) {
        TaskExecutorLogMonitor.super.onCompleted(result);
        verify(result, r -> {
            completed.accept(r);
            testContext.completeNow();
        });
    }

    private void verify(@NonNull TaskResult result, Consumer<TaskResult> verification) {
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

}
