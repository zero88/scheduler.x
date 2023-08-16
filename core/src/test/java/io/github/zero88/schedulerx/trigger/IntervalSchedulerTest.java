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
import io.github.zero88.schedulerx.SchedulingAsserter;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class IntervalSchedulerTest {

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
        SchedulingMonitor<Object> asserter = SchedulingAsserter.unableScheduleAsserter(testContext, errorMsg);
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setTask(NoopTask.create())
                         .build()
                         .start();
    }

    @Test
    void test_run_task_after_delay(Vertx vertx, VertxTestContext ctx) {
        final Consumer<ExecutionResult<Void>> onSchedule = result -> {
            Assertions.assertEquals(0, result.tick());
            Assertions.assertEquals(0, result.round());
        };
        final Consumer<ExecutionResult<Void>> onComplete = result -> {
            Assertions.assertEquals(2, result.round());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
        };
        final SchedulingAsserter<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                    .setTestContext(ctx)
                                                                    .setSchedule(onSchedule)
                                                                    .setCompleted(onComplete)
                                                                    .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().initialDelay(2).interval(2).repeat(2).build();
        IntervalScheduler.<Void, Void>builder()
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
        final Consumer<ExecutionResult<Void>> onComplete = result -> {
            checkpoint.flag();
            Assertions.assertEquals(3, result.round());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
        };
        final SchedulingAsserter<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                    .setTestContext(testContext)
                                                                    .setCompleted(onComplete)
                                                                    .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(2).repeat(3).build();
        IntervalScheduler.<Void, Void>builder()
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
        };
        final SchedulingAsserter<String> asserter = SchedulingAsserter.<String>builder()
                                                                      .setTestContext(context)
                                                                      .setEach(onEach)
                                                                      .build();
        final Task<Void, String> task = (jobData, ctx) -> {
            final long round = ctx.round();
            if (round == 1) {
                throw new IllegalArgumentException("throw in execution");
            }
            if (round == 2) {
                ctx.fail(new IllegalArgumentException("explicit set failed"));
            }
            if (round == 3) {
                ctx.complete("OK");
            }
        };
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(2).repeat(3).build();
        IntervalScheduler.<Void, String>builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setTask(task)
                         .build()
                         .start();
    }

}