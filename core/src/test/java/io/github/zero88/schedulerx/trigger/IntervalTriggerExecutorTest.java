package io.github.zero88.schedulerx.trigger;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.NoopTask;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutorAsserter;
import io.github.zero88.schedulerx.TaskExecutorMonitor;
import io.github.zero88.schedulerx.TaskResult;
import io.github.zero88.schedulerx.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class IntervalTriggerExecutorTest {

    private static Stream<Object> provide_invalid_interval() {
        return Stream.of(arguments(IntervalTrigger.builder().interval(-1).build(), "Invalid interval value"),
                         arguments(IntervalTrigger.builder().repeat(0).interval(3).build(), "Invalid repeat value"),
                         arguments(IntervalTrigger.builder().interval(5).initialDelay(-1).build(),
                                   "Invalid initial delay value"));
    }

    @ParameterizedTest
    @MethodSource("provide_invalid_interval")
    @SuppressWarnings("java:S2699")
    void test_run_task_unable_schedule_due_to_interval(IntervalTrigger trigger, String errorMsg, Vertx vertx,
                                                       VertxTestContext testContext) {
        TaskExecutorMonitor<Object> asserter = TaskExecutorAsserter.unableScheduleAsserter(testContext, errorMsg);
        IntervalTriggerExecutor.builder()
                               .setVertx(vertx)
                               .setMonitor(asserter)
                               .setTrigger(trigger)
                               .setTask(NoopTask.create())
                               .build()
                               .start();
    }

    @Test
    void test_run_task_after_delay(Vertx vertx, VertxTestContext ctx) {
        final Checkpoint checkpoint = ctx.checkpoint(2);
        final Consumer<TaskResult<Void>> s = result -> {
            checkpoint.flag();
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertEquals(0, result.tick());
            Assertions.assertEquals(0, result.round());
        };
        final Consumer<TaskResult<Void>> c = result -> {
            checkpoint.flag();
            Assertions.assertEquals(2, result.round());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
            ctx.completeNow();
        };
        final TaskExecutorAsserter<Void> asserter = TaskExecutorAsserter.<Void>builder()
                                                                        .setTestContext(ctx)
                                                                        .setSchedule(s)
                                                                        .setCompleted(c)
                                                                        .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().initialDelay(2).interval(2).repeat(2).build();
        IntervalTriggerExecutor.<Void, Void>builder()
                               .setVertx(vertx)
                               .setMonitor(asserter)
                               .setTrigger(trigger)
                               .setTask(NoopTask.create())
                               .build()
                               .start();
    }

    @Test
    void test_run_blocking_task_in_the_end(Vertx vertx, VertxTestContext testContext) {
        final Checkpoint checkpoint = testContext.checkpoint(3);
        final Consumer<TaskResult<Void>> c = result -> {
            checkpoint.flag();
            Assertions.assertEquals(3, result.round());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
        };
        final TaskExecutorAsserter<Void> asserter = TaskExecutorAsserter.<Void>builder()
                                                                        .setTestContext(testContext)
                                                                        .setCompleted(c)
                                                                        .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(2).repeat(3).build();
        IntervalTriggerExecutor.<Void, Void>builder()
                               .setVertx(vertx)
                               .setMonitor(asserter)
                               .setTrigger(trigger)
                               .setTask((jobData, ctx) -> {
                                   TestUtils.sleep(3000, testContext);
                                   checkpoint.flag();
                               })
                               .build()
                               .start();
    }

    @Test
    void test_task_should_be_executed_in_interval_trigger(Vertx vertx, VertxTestContext context) {
        final Task<Void, String> task = (jobData, ctx) -> {
            final long round = ctx.round();
            if (round == 1) {
                throw new RuntimeException("throw in execution");
            }
            if (round == 2) {
                ctx.fail(new IllegalArgumentException("explicit set failed"));
            }
            if (round == 3) {
                ctx.complete("OK");
            }
        };
        final Consumer<TaskResult<String>> e = result -> {
            if (result.round() == 1) {
                Assertions.assertTrue(result.isError());
                Assertions.assertNotNull(result.error());
                Assertions.assertTrue(result.error() instanceof RuntimeException);
                Assertions.assertNull(result.data());
            }
            if (result.round() == 2) {
                Assertions.assertTrue(result.isError());
                Assertions.assertNotNull(result.error());
                Assertions.assertTrue(result.error() instanceof IllegalArgumentException);
                Assertions.assertNull(result.data());
            }
            if (result.round() == 3) {
                Assertions.assertFalse(result.isError());
                Assertions.assertNull(result.error());
                Assertions.assertEquals("OK", result.data());
            }
        };
        final TaskExecutorAsserter<String> asserter = TaskExecutorAsserter.<String>builder()
                                                                          .setTestContext(context)
                                                                          .setEach(e)
                                                                          .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(2).repeat(3).build();
        IntervalTriggerExecutor.<Void, String>builder()
                               .setVertx(vertx)
                               .setMonitor(asserter)
                               .setTrigger(trigger)
                               .setTask(task)
                               .build()
                               .start();
    }

}
