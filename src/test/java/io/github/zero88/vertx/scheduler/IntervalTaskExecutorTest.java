package io.github.zero88.vertx.scheduler;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.vertx.scheduler.impl.IntervalTaskExecutor;
import io.github.zero88.vertx.scheduler.trigger.IntervalTrigger;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class IntervalTaskExecutorTest {

    static void sleep(int millis, VertxTestContext testContext) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            testContext.failNow(e);
        }
    }

    @Test
    void test_run_task_unable_schedule_due_to_interval(Vertx vertx, VertxTestContext testContext) {
        final Checkpoint checkpoint = testContext.checkpoint(2);
        IntervalTaskExecutor.builder()
                            .vertx(vertx)
                            .trigger(IntervalTrigger.builder().interval(-1).build())
                            .task((jobData, ctx) -> {})
                            .monitor(TaskExecutorAsserter.unableScheduleAsserter(testContext, checkpoint))
                            .build()
                            .start();
    }

    @Test
    void test_run_task_unable_schedule_due_to_initial(Vertx vertx, VertxTestContext testContext) {
        final Checkpoint checkpoint = testContext.checkpoint(2);
        IntervalTaskExecutor.builder()
                            .vertx(vertx)
                            .trigger(IntervalTrigger.builder().initialDelay(-1).build())
                            .task((jobData, ctx) -> {})
                            .monitor(TaskExecutorAsserter.unableScheduleAsserter(testContext, checkpoint))
                            .build()
                            .start();
    }

    @Test
    void test_run_task_after_delay(Vertx vertx, VertxTestContext ctx) {
        final Checkpoint checkpoint = ctx.checkpoint(2);
        final WorkerExecutor worker = vertx.createSharedWorkerExecutor("TEST_PERIODIC", 3);
        final Consumer<TaskResult> s = result -> {
            checkpoint.flag();
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertEquals(0, result.tick());
            Assertions.assertEquals(0, result.round());
        };
        final Consumer<TaskResult> c = result -> {
            checkpoint.flag();
            Assertions.assertEquals(2, result.round());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
            ctx.completeNow();
        };
        IntervalTaskExecutor.builder()
                            .vertx(vertx)
                            .trigger(IntervalTrigger.builder().initialDelay(2).interval(2).repeat(2).build())
                            .task((jobData, context) -> {})
                            .monitor(TaskExecutorAsserter.builder().testContext(ctx).schedule(s).completed(c).build())
                            .build()
                            .start(worker);
    }

    @Test
    void test_run_blocking_task_in_the_end(Vertx vertx, VertxTestContext testContext) {
        final Checkpoint checkpoint = testContext.checkpoint(3);
        final WorkerExecutor worker = vertx.createSharedWorkerExecutor("TEST_PERIODIC", 3);
        final Consumer<TaskResult> c = result -> {
            checkpoint.flag();
            Assertions.assertEquals(3, result.round());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
        };
        IntervalTaskExecutor.builder()
                            .vertx(vertx)
                            .trigger(IntervalTrigger.builder().interval(2).repeat(3).build())
                            .task((jobData, ctx) -> {
                                sleep(4000, testContext);
                                checkpoint.flag();
                            })
                            .monitor(TaskExecutorAsserter.builder().testContext(testContext).completed(c).build())
                            .build()
                            .start(worker);
    }

    @Test
    void test_cancel_task_in_condition(Vertx vertx, VertxTestContext context) {
        final Checkpoint checkpoint = context.checkpoint(5);
        final Task task = (jobData, ctx) -> {
            checkpoint.flag();
            final long round = ctx.round();
            if (round == 2) {
                throw new RuntimeException("xx");
            }
            if (round == 4) {
                throw new IllegalArgumentException("yy");
            }
            if (round == 5) {
                ctx.forceStopExecution();
            }
        };
        final Consumer<TaskResult> e = result -> {
            if (result.round() == 2) {
                Assertions.assertTrue(result.isError());
                Assertions.assertNotNull(result.error());
                Assertions.assertTrue(result.error() instanceof RuntimeException);
            }
            if (result.round() == 4) {
                Assertions.assertTrue(result.isError());
                Assertions.assertNotNull(result.error());
                Assertions.assertTrue(result.error() instanceof IllegalArgumentException);
            }
        };
        final Consumer<TaskResult> c = result -> {
            Assertions.assertEquals(5, result.round());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
        };
        IntervalTaskExecutor.builder()
                            .vertx(vertx)
                            .trigger(IntervalTrigger.builder().interval(1).repeat(10).build())
                            .task(task)
                            .monitor(TaskExecutorAsserter.builder().testContext(context).each(e).completed(c).build())
                            .build()
                            .start();
    }

}
