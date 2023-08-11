package io.github.zero88.schedulerx.trigger;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.schedulerx.NoopTask;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutorAsserter;
import io.github.zero88.schedulerx.TaskResult;
import io.github.zero88.schedulerx.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class CronTriggerExecutorTest {

    @Test
    void test_unable_schedule_due_to_wrong_expression(Vertx vertx, VertxTestContext testContext) {
        final Checkpoint checkpoint = testContext.checkpoint(2);
        final CronTrigger trigger = CronTrigger.builder().expression("0/").build();
        CronTriggerExecutor.builder()
                           .setVertx(vertx)
                           .setTrigger(trigger)
                           .setTask(NoopTask.create())
                           .setMonitor(TaskExecutorAsserter.unableScheduleAsserter(testContext, checkpoint))
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

    @Test
    void test_executor_should_start_only_once_time(Vertx vertx, VertxTestContext testCtx) throws InterruptedException {
        final int nbOfRound = 2;
        final int timeout = 10; // 2 times of 4 seconds and plus reserve time to start
        final Checkpoint checkpoint = testCtx.checkpoint(nbOfRound);
        final CronTrigger trigger = CronTrigger.builder().expression("0/4 * * ? * * *").build();
        final Task<Void, Void> task = (jobData, ctx) -> {
            checkpoint.flag();
            if (ctx.round() == nbOfRound) {
                ctx.forceStopExecution();
            }
        };
        final Consumer<TaskResult<Void>> completed = result -> {
            Assertions.assertEquals(2, result.round());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
        };
        final TaskExecutorAsserter<Void> asserter = TaskExecutorAsserter.<Void>builder()
                                                                        .setTestContext(testCtx)
                                                                        .setCompleted(completed)
                                                                        .build();
        final CronTriggerExecutor<Void, Void> executor = CronTriggerExecutor.<Void, Void>builder()
                                                                            .setVertx(vertx)
                                                                            .setTrigger(trigger)
                                                                            .setTask(task)
                                                                            .setMonitor(asserter)
                                                                            .build();

        final int nbOfThreads = 5;
        final int nbOfFailed = nbOfThreads - 1;
        final List<Exception> store = TestUtils.simulateRunActionInParallel(testCtx, executor::start, nbOfThreads);

        Assertions.assertTrue(testCtx.awaitCompletion(timeout, TimeUnit.SECONDS));
        Assertions.assertEquals(nbOfFailed, store.size());
        for (int i = 0; i < nbOfFailed; i++) {
            Assertions.assertInstanceOf(IllegalStateException.class, store.get(i));
            Assertions.assertEquals("The executor is already started!", store.get(i).getMessage());
        }
    }

}
