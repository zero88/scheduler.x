package io.github.zero88.schedulerx.trigger;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.NoopJob;
import io.github.zero88.schedulerx.SchedulingAsserter;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.TestUtils;
import io.github.zero88.schedulerx.trigger.TriggerCondition.ReasonCode;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class IntervalSchedulerTest {

    @RegisterExtension
    static RunTestOnContext rtoc = new RunTestOnContext();

    private static Stream<Object> provide_invalid_interval() {
        return Stream.of(arguments(IntervalTrigger.builder().interval(-1).build(), "Invalid interval value"),
                         arguments(IntervalTrigger.builder().repeat(0).interval(3).build(), "Invalid repeat value"),
                         arguments(IntervalTrigger.builder().interval(5).initialDelay(-1).build(),
                                   "Invalid initial delay value"));
    }

    @ParameterizedTest
    @MethodSource("provide_invalid_interval")
    @SuppressWarnings("java:S2699")
    void test_unable_schedule_job_due_to_invalid_config(IntervalTrigger trigger, String errorMsg, Vertx vertx,
                                                        VertxTestContext testContext) {
        SchedulingMonitor<Object> asserter = SchedulingAsserter.unableScheduleAsserter(testContext, errorMsg);
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setJob(NoopJob.create())
                         .build()
                         .start();
    }

    @Test
    void test_run_job_after_delay(Vertx vertx, VertxTestContext ctx) {
        final Consumer<ExecutionResult<Void>> onSchedule = result -> {
            Assertions.assertEquals(0, result.tick());
            Assertions.assertEquals(0, result.round());
        };
        final Consumer<ExecutionResult<Void>> onComplete = result -> {
            Assertions.assertEquals(2, result.round());
            Assertions.assertFalse(result.isError());
        };
        final SchedulingAsserter<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                    .setTestContext(ctx)
                                                                    .setSchedule(onSchedule)
                                                                    .setCompleted(onComplete)
                                                                    .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().initialDelay(5).interval(2).repeat(2).build();
        IntervalScheduler.<Void, Void>builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setJob(NoopJob.create())
                         .build()
                         .start();
    }

    @Test
    void test_run_blocking_job_till_the_end(Vertx vertx, VertxTestContext testContext) {
        final AtomicLong lastTickOnEach = new AtomicLong();
        final Consumer<ExecutionResult<Void>> onEach = result -> {
            lastTickOnEach.set(result.tick());
            if (result.round() == 1) {
                Assertions.assertEquals(result.tick(), result.round());
            } else {
                Assertions.assertTrue(result.tick() > result.round());
            }
        };
        final Consumer<ExecutionResult<Void>> onMisfire = result -> {
            Assertions.assertTrue(result.tick() > result.round());
            Assertions.assertTrue(result.tick() > lastTickOnEach.get());
            Assertions.assertEquals("JobIsRunning", result.triggerContext().condition().reasonCode());
        };
        final Consumer<ExecutionResult<Void>> onComplete = result -> {
            Assertions.assertEquals(3, result.round());
            Assertions.assertEquals(lastTickOnEach.get() + result.round(), result.tick());
            Assertions.assertFalse(result.isError());
        };
        final SchedulingAsserter<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                    .setTestContext(testContext)
                                                                    .setEach(onEach)
                                                                    .setMisfire(onMisfire)
                                                                    .setCompleted(onComplete)
                                                                    .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(1).repeat(3).build();
        final WorkerExecutor worker = vertx.createSharedWorkerExecutor("hello", 3, 500);
        IntervalScheduler.<Void, Void>builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setJob((jobData, ctx) -> TestUtils.block(Duration.ofSeconds(3), testContext))
                         .build()
                         .start(worker);
    }

    @Test
    void test_job_should_be_executed_in_interval_trigger(Vertx vertx, VertxTestContext context) {
        final Consumer<ExecutionResult<String>> onEach = result -> {
            if (result.round() < 3) {
                Assertions.assertTrue(result.isError());
                Assertions.assertNotNull(result.error());
                Assertions.assertInstanceOf(RuntimeException.class, result.error());
                Assertions.assertNull(result.data());
            }
            if (result.round() == 3) {
                Assertions.assertFalse(result.isError());
                Assertions.assertNull(result.error());
                Assertions.assertEquals("OK", result.data());
            }
            Assertions.assertNull(result.triggerContext().info());
        };
        final SchedulingAsserter<String> asserter = SchedulingAsserter.<String>builder()
                                                                      .setTestContext(context)
                                                                      .setEach(onEach)
                                                                      .build();
        final Job<Void, String> job = (jobData, ctx) -> {
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
                         .setJob(job)
                         .build()
                         .start();
    }

    @Test
    void test_scheduler_should_be_stopped_when_reach_to_target_round(Vertx vertx, VertxTestContext context) {
        final Consumer<ExecutionResult<String>> onCompleted = result -> {
            Assertions.assertTrue(result.triggerContext().isStopped());
            Assertions.assertEquals(ReasonCode.STOP_BY_CONFIG, result.triggerContext().condition().reasonCode());
        };
        final SchedulingAsserter<String> asserter = SchedulingAsserter.<String>builder()
                                                                      .setTestContext(context)
                                                                      .setCompleted(onCompleted)
                                                                      .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(1).repeat(3).build();
        IntervalScheduler.<Void, String>builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setJob(NoopJob.create())
                         .build()
                         .start();
    }

}
