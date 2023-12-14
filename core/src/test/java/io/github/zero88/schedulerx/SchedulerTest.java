package io.github.zero88.schedulerx;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.impl.AbstractTriggerEvaluator;
import io.github.zero88.schedulerx.trigger.CronScheduler;
import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.github.zero88.schedulerx.trigger.EventScheduler;
import io.github.zero88.schedulerx.trigger.EventTrigger;
import io.github.zero88.schedulerx.trigger.IntervalScheduler;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.github.zero88.schedulerx.trigger.TriggerContext;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class SchedulerTest {

    @Test
    void test_scheduler_should_start_only_once_time(Vertx vertx, VertxTestContext testCtx) throws InterruptedException {
        final int timeout = 10; // 2 times of 4 seconds and plus reserve time to start
        final CronTrigger trigger = CronTrigger.builder().expression("0/4 * * ? * * *").build();
        final Consumer<ExecutionResult<Void>> completed = result -> {
            Assertions.assertEquals(2, result.round());
            Assertions.assertFalse(result.isError());
        };
        final SchedulingAsserter<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                    .setTestContext(testCtx)
                                                                    .setCompleted(completed)
                                                                    .build();
        final CronScheduler<Void, Void> scheduler = CronScheduler.<Void, Void>builder()
                                                                 .setVertx(vertx)
                                                                 .setTrigger(trigger)
                                                                 .setJob(NoopJob.create(2))
                                                                 .setMonitor(asserter)
                                                                 .build();

        final int nbOfThreads = 5;
        final int nbOfFailed = nbOfThreads - 1;
        final List<Exception> store = TestUtils.simulateRunActionInParallel(testCtx, scheduler::start, nbOfThreads);

        Assertions.assertTrue(testCtx.awaitCompletion(timeout, TimeUnit.SECONDS));
        Assertions.assertEquals(nbOfFailed, store.size());
        for (int i = 0; i < nbOfFailed; i++) {
            Assertions.assertInstanceOf(IllegalStateException.class, store.get(i));
            Assertions.assertEquals("The executor is already started!", store.get(i).getMessage());
        }
    }

    @Test
    void test_scheduler_should_run_job_in_dedicated_thread(Vertx vertx, VertxTestContext testContext) {
        final String threadName = "HELLO";
        final WorkerExecutor worker = vertx.createSharedWorkerExecutor(threadName, 1);
        final SchedulingAsserter<Object> asserter = SchedulingAsserter.builder().setTestContext(testContext).build();
        final Job<Object, Object> job = (d, ec) -> Assertions.assertEquals(threadName + "-0",
                                                                           Thread.currentThread().getName());
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(2).repeat(1).build();
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setJob(job)
                         .build()
                         .start(worker);
    }

    @Test
    void test_scheduler_should_evaluate_trigger_in_dedicated_thread(Vertx vertx, VertxTestContext testContext) {
        final String threadName = "HEY";
        final WorkerExecutor worker = vertx.createSharedWorkerExecutor(threadName, 1);
        final Consumer<String> doAssert = (thread) -> Assertions.assertEquals(thread + "-0",
                                                                              Thread.currentThread().getName());
        final EventTriggerPredicate<Object> predicate = EventTriggerPredicate.create((headers, body) -> {
            doAssert.accept("vert.x-eventloop-thread");
            return body;
        }, eventMessage -> {
            doAssert.accept(threadName);
            return true;
        });
        final EventTrigger<Object> trigger = EventTrigger.builder()
                                                         .address("dedicated.thread")
                                                         .predicate(predicate)
                                                         .build();
        final SchedulingAsserter<Object> asserter = SchedulingAsserter.builder().setTestContext(testContext).build();
        EventScheduler.builder()
                      .setVertx(vertx)
                      .setMonitor(asserter)
                      .setTrigger(trigger)
                      .setJob(NoopJob.create(1))
                      .build()
                      .start(worker);
        vertx.eventBus().publish("dedicated.thread", "test");
    }

    @ParameterizedTest
    @MethodSource("provide_external_ids")
    void test_scheduler_should_maintain_external_id_from_jobData_to_job_result(Object declaredId, Class<?> typeOfId,
                                                                               Vertx vertx, VertxTestContext ctx) {
        final Consumer<ExecutionResult<Void>> ensureExternalIdIsSet = result -> {
            Assertions.assertNotNull(result.externalId());
            Assertions.assertInstanceOf(typeOfId, result.externalId());
            if (declaredId != null) {
                Assertions.assertSame(declaredId, result.externalId());
            }
        };
        final SchedulingAsserter<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                    .setTestContext(ctx)
                                                                    .setSchedule(ensureExternalIdIsSet)
                                                                    .setEach(ensureExternalIdIsSet)
                                                                    .setCompleted(ensureExternalIdIsSet)
                                                                    .build();
        final JobData<Void> jobdata = declaredId == null ? JobData.empty() : JobData.empty(declaredId);
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(1).repeat(2).build();
        IntervalScheduler.<Void, Void>builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setJob(NoopJob.create())
                         .setJobData(jobdata)
                         .build()
                         .start();
    }

    @Test
    void test_scheduler_should_timeout_in_execution(Vertx vertx, VertxTestContext testContext) {
        final Duration timeout = Duration.ofSeconds(2);
        final Duration runningTime = Duration.ofSeconds(3);
        final Consumer<ExecutionResult<Object>> timeoutAsserter = result -> {
            Assertions.assertTrue(result.isError());
            Assertions.assertTrue(result.isTimeout());
            Assertions.assertEquals("Timeout after 2s", result.error().getMessage());
        };
        final SchedulingAsserter<Object> asserter = SchedulingAsserter.builder()
                                                                      .setTestContext(testContext)
                                                                      .setEach(timeoutAsserter)
                                                                      .build();
        final Job<Object, Object> job = (jobData, executionContext) -> {
            TestUtils.block(runningTime, testContext);
            Assertions.assertTrue(Thread.currentThread().getName().startsWith("scheduler.x-worker-thread"));
        };
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(IntervalTrigger.builder().interval(5).repeat(1).build())
                         .setJob(job)
                         .setTimeoutPolicy(TimeoutPolicy.create(timeout))
                         .build()
                         .start();
    }

    @Test
    void test_scheduler_should_timeout_in_evaluation(Vertx vertx, VertxTestContext testContext) {
        final Duration timeout = Duration.ofSeconds(1);
        final Duration runningTime = Duration.ofSeconds(3);
        final Consumer<ExecutionResult<Object>> timeoutAsserter = result -> {
            Assertions.assertEquals("TriggerEvaluationTimeout", result.triggerContext().condition().reasonCode());
            Assertions.assertEquals("Timeout after 1s", result.triggerContext().condition().cause().getMessage());
            testContext.completeNow();
        };
        final SchedulingAsserter<Object> asserter = SchedulingAsserter.builder()
                                                                      .setTestContext(testContext)
                                                                      .setMisfire(timeoutAsserter)
                                                                      .build();
        final TriggerEvaluator evaluator = new AbstractTriggerEvaluator() {
            @Override
            protected Future<TriggerContext> internalCheck(@NotNull Trigger trigger,
                                                           @NotNull TriggerContext triggerContext,
                                                           @Nullable Object externalId) {
                TestUtils.block(runningTime, testContext);
                return Future.succeededFuture(triggerContext);
            }
        };
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(IntervalTrigger.builder().interval(5).build())
                         .setJob(NoopJob.create())
                         .setTimeoutPolicy(TimeoutPolicy.create(timeout, null))
                         .setTriggerEvaluator(evaluator)
                         .build()
                         .start();
    }

    @Test
    void test_scheduler_should_able_to_force_stop(Vertx vertx, VertxTestContext testContext) {
        final Consumer<ExecutionResult<Object>> completed = result -> {
            Assertions.assertEquals(3, result.round());
            Assertions.assertEquals(3, result.tick());
            Assertions.assertTrue(result.triggerContext().isStopped());
            Assertions.assertEquals("ForceStop", result.triggerContext().condition().reasonCode());
        };
        final SchedulingAsserter<Object> asserter = SchedulingAsserter.builder()
                                                                      .setTestContext(testContext)
                                                                      .setCompleted(completed)
                                                                      .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(1).repeat(5).build();
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setJob(NoopJob.create(3))
                         .build()
                         .start();
    }

    @Test
    void test_scheduler_should_able_to_cancel_manually(Vertx vertx, VertxTestContext testContext) {
        final Consumer<ExecutionResult<Object>> onCompleted = result -> {
            Assertions.assertTrue(result.triggerContext().isStopped());
            Assertions.assertEquals(result.tick(), result.triggerContext().tick());
            Assertions.assertEquals("TriggerIsCancelled", result.triggerContext().condition().reasonCode());
        };
        final SchedulingAsserter<Object> asserter = SchedulingAsserter.builder()
                                                                      .setTestContext(testContext)
                                                                      .setCompleted(onCompleted)
                                                                      .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(1).build();
        final IntervalScheduler<Object, Object> scheduler = IntervalScheduler.builder()
                                                                             .setVertx(vertx)
                                                                             .setMonitor(asserter)
                                                                             .setTrigger(trigger)
                                                                             .setJob(NoopJob.create())
                                                                             .build();
        scheduler.start();
        TestUtils.block(Duration.ofSeconds(3), testContext);
        scheduler.cancel();
    }

    private static Stream<Object> provide_external_ids() {
        return Stream.of(arguments(null, Integer.class), arguments(3L, Long.class), arguments("test", String.class),
                         arguments(UUID.randomUUID(), UUID.class), arguments(JsonArray.of(1, 2, 3), JsonArray.class),
                         arguments(JsonObject.of("k1", "v1"), JsonObject.class));
    }

}
