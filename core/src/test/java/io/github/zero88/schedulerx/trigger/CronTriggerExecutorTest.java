package io.github.zero88.schedulerx.trigger;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.schedulerx.NoopTask;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutorAsserter;
import io.github.zero88.schedulerx.TaskResult;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class CronTriggerExecutorTest {

    @Test
    @SuppressWarnings("java:S2699")
    void test_unable_schedule_due_to_wrong_expression(Vertx vertx, VertxTestContext testContext) {
        final CronTrigger trigger = CronTrigger.builder().expression("0/").build();
        CronTriggerExecutor.builder()
                           .setVertx(vertx)
                           .setTrigger(trigger)
                           .setTask(NoopTask.create())
                           .setMonitor(TaskExecutorAsserter.unableScheduleAsserter(testContext))
                           .build()
                           .start();
    }

    @Test
    void test_run_task_by_cron(Vertx vertx, VertxTestContext testContext) {
        final int nbOfRound = 2;
        final Checkpoint checkpoint = testContext.checkpoint(nbOfRound);
        final CronTrigger trigger = CronTrigger.builder().expression("0/3 * * ? * * *").build();
        final Task<Void, Void> task = (jobData, ctx) -> {
            checkpoint.flag();
            if (ctx.round() == nbOfRound) {
                ctx.forceStopExecution();
            }
        };
        final Consumer<TaskResult<Void>> schedule = result -> {
            if (!result.isReschedule()) {
                Assertions.assertNotNull(result.availableAt());
                Assertions.assertEquals(0, result.tick());
                Assertions.assertEquals(0, result.round());
            } else {
                Assertions.assertNotNull(result.availableAt());
                Assertions.assertTrue(result.isReschedule());
            }
        };
        final Consumer<TaskResult<Void>> completed = result -> {
            Assertions.assertEquals(2, result.round());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
        };
        final TaskExecutorAsserter<Void> asserter = TaskExecutorAsserter.<Void>builder()
                                                                        .setTestContext(testContext)
                                                                        .setSchedule(schedule)
                                                                        .setCompleted(completed)
                                                                        .build();
        CronTriggerExecutor.<Void, Void>builder()
                           .setVertx(vertx)
                           .setMonitor(asserter)
                           .setTrigger(trigger)
                           .setTask(task)
                           .build()
                           .start();
    }

}
