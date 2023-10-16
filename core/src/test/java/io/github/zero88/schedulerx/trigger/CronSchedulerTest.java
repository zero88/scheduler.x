package io.github.zero88.schedulerx.trigger;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.NoopTask;
import io.github.zero88.schedulerx.SchedulingAsserter;
import io.github.zero88.schedulerx.Task;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class CronSchedulerTest {

    @Test
    @SuppressWarnings("java:S2699")
    void test_unable_schedule_due_to_wrong_expression(Vertx vertx, VertxTestContext testContext) {
        final CronTrigger trigger = CronTrigger.builder().expression("0/").build();
        CronScheduler.builder()
                     .setVertx(vertx)
                     .setTrigger(trigger)
                     .setTask(NoopTask.create())
                     .setMonitor(SchedulingAsserter.unableScheduleAsserter(testContext))
                     .build()
                     .start();
    }

    @Test
    void test_run_task_by_cron(Vertx vertx, VertxTestContext testContext) {
        final Consumer<ExecutionResult<String>> onSchedule = result -> {
            if (!result.isReschedule()) {
                Assertions.assertEquals(0, result.tick());
                Assertions.assertEquals(0, result.round());
            } else {
                Assertions.assertTrue(result.isReschedule());
            }
        };
        final Consumer<ExecutionResult<String>> onEach = result -> {
            if (result.round() < 3) {
                Assertions.assertTrue(result.isError());
                Assertions.assertNotNull(result.error());
                Assertions.assertTrue(result.error() instanceof RuntimeException);
                Assertions.assertNull(result.data());
            }
            if (result.round() == 3) {
                Assertions.assertFalse(result.isError());
                Assertions.assertNull(result.error());
                Assertions.assertEquals("OK", result.data());
            }
            Assertions.assertNull(result.triggerContext().info());
        };
        final Consumer<ExecutionResult<String>> onCompleted = result -> {
            Assertions.assertEquals(4, result.round());
            Assertions.assertFalse(result.isError());
        };
        final SchedulingAsserter<String> asserter = SchedulingAsserter.<String>builder()
                                                                      .setTestContext(testContext)
                                                                      .setSchedule(onSchedule)
                                                                      .setEach(onEach)
                                                                      .setCompleted(onCompleted)
                                                                      .build();
        final CronTrigger trigger = CronTrigger.builder().expression("0/2 * * ? * * *").build();
        final Task<Void, String> task = (jobData, ctx) -> {
            final long round = ctx.round();
            if (round == 1) {
                throw new RuntimeException("throw in execution");
            }
            if (round == 2) {
                ctx.fail(new RuntimeException("explicit set failed"));
            }
            if (round == 3) {
                ctx.complete("OK");
            }
            if (round == 4) {
                ctx.forceStopExecution();
            }
            if (round == 5) {
                throw new IllegalStateException("Must stop before");
            }
        };
        CronScheduler.<Void, String>builder()
                     .setVertx(vertx)
                     .setMonitor(asserter)
                     .setTrigger(trigger)
                     .setTask(task)
                     .build()
                     .start();
    }

}
