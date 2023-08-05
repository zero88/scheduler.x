package io.github.zero88.schedulerx.trigger;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.schedulerx.NoopTask;
import io.github.zero88.schedulerx.TaskExecutorAsserter;
import io.github.zero88.schedulerx.TaskResult;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class CronTriggerExecutorTest {

    @Test
    void test_unable_schedule_due_to_initial(Vertx vertx, VertxTestContext testContext) {
        final Checkpoint checkpoint = testContext.checkpoint(2);
        CronTriggerExecutor.builder()
                           .setVertx(vertx)
                           .setTrigger(CronTrigger.builder().expression("0/").build())
                           .setTask(NoopTask.create())
                           .setMonitor(TaskExecutorAsserter.unableScheduleAsserter(testContext, checkpoint))
                           .build()
                           .start();
    }

    @Test
    void test_run_task_by_cron(Vertx vertx, VertxTestContext testContext) {
        final Checkpoint checkpoint = testContext.checkpoint(3);
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
        CronTriggerExecutor.<Void, Void>builder()
                           .setVertx(vertx)
                           .setTrigger(CronTrigger.builder().expression("0/5 * * ? * * *").build())
                           .setTask((jobData, ctx) -> {
                               checkpoint.flag();
                               if (ctx.round() == 2) {
                                   ctx.forceStopExecution();
                               }
                           })
                           .setMonitor(TaskExecutorAsserter.<Void>builder()
                                                           .setTestContext(testContext)
                                                           .setSchedule(schedule)
                                                           .setCompleted(completed)
                                                           .build())
                           .build()
                           .start();
    }

}
