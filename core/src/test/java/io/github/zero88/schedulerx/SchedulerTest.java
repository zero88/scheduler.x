package io.github.zero88.schedulerx;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.github.zero88.schedulerx.trigger.EventTrigger;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.github.zero88.schedulerx.trigger.TriggerCondition;
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
        final SchedulingMonitor<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                   .setTestContext(testCtx)
                                                                   .setCompleted(completed)
                                                                   .build();
        final CronScheduler scheduler = CronScheduler.<Void, Void>builder()
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
        final SchedulingMonitor<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                   .setTestContext(ctx)
                                                                   .setSchedule(ensureExternalIdIsSet)
                                                                   .setEach(ensureExternalIdIsSet)
                                                                   .setCompleted(ensureExternalIdIsSet)
                                                                   .build();
        final JobData<Void> jobdata = declaredId == null ? JobData.empty() : JobData.empty(declaredId);
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(Duration.ofSeconds(1)).repeat(2).build();
        IntervalScheduler.<Void, Void>builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setJob(NoopJob.create())
                         .setJobData(jobdata)
                         .build()
                         .start();
    }

    @ParameterizedTest
    @MethodSource("provide_threads")
    void test_scheduler_should_run_job_in_dedicated_thread(WorkerThreadChecker checker, Vertx vertx,
                                                           VertxTestContext testContext) {
        final SchedulingMonitor<Object> asserter = SchedulingAsserter.builder()
                                                                     .setTestContext(testContext)
                                                                     .setEach(r -> Assertions.assertNull(r.error()))
                                                                     .build();
        final Job<Object, Object> job = (d, ec) -> checker.doAssert();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(Duration.ofSeconds(2)).repeat(2).build();
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setJob(job)
                         .setTimeoutPolicy(checker.timeoutPolicy())
                         .build()
                         .start(checker.createExecutor(vertx));
    }

    @ParameterizedTest
    @MethodSource("provide_threads")
    void test_scheduler_should_evaluate_trigger_in_dedicated_thread(WorkerThreadChecker checker, Vertx vertx,
                                                                    VertxTestContext testContext) {
        final EventTriggerPredicate<Object> predicate = EventTriggerPredicate.create((headers, body) -> {
            checker.doAssert("vert.x-eventloop-thread-0");
            return body;
        }, eventMessage -> {
            checker.doAssert();
            return true;
        });
        final SchedulingMonitor<Object> asserter = SchedulingAsserter.builder()
                                                                     .setTestContext(testContext)
                                                                     .setEach(r -> Assertions.assertNull(r.error()))
                                                                     .setMisfire(r -> Assertions.assertNull(r.error()))
                                                                     .build();
        final String address = "dedicated.thread";
        EventScheduler.builder()
                      .setVertx(vertx)
                      .setMonitor(asserter)
                      .setTrigger(EventTrigger.builder().address(address).predicate(predicate).build())
                      .setJob(NoopJob.create(1))
                      .setTimeoutPolicy(checker.timeoutPolicy())
                      .build()
                      .start(checker.createExecutor(vertx));
        vertx.eventBus().publish(address, "test");
    }

    @Test
    void test_scheduler_should_monitor_result_in_dedicated_thread(Vertx vertx, VertxTestContext testContext) {
        final WorkerThreadChecker c0 = WorkerThreadChecker.create(v -> null, "scheduler.x-monitor-thread-2s");
        final SchedulingMonitor<Object> asserter = SchedulingAsserter.builder()
                                                                     .setTestContext(testContext)
                                                                     .setSchedule(r -> c0.doAssert())
                                                                     .setEach(r -> c0.doAssert())
                                                                     .setCompleted(r -> c0.doAssert())
                                                                     .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(Duration.ofSeconds(2)).repeat(2).build();
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setJob(NoopJob.create())
                         .build()
                         .start();
    }

    @Test
    void test_scheduler_should_be_timeout_in_execution(Vertx vertx, VertxTestContext testContext) {
        final Duration timeout = Duration.ofSeconds(2);
        final Duration runningTime = Duration.ofSeconds(3);
        final Consumer<ExecutionResult<Object>> timeoutAsserter = result -> {
            Assertions.assertTrue(result.isError());
            Assertions.assertTrue(result.isTimeout());
            Assertions.assertEquals("Timeout after 2s", result.error().getMessage());
        };
        final SchedulingMonitor<Object> asserter = SchedulingAsserter.builder()
                                                                     .setTestContext(testContext)
                                                                     .setEach(timeoutAsserter)
                                                                     .build();
        final Job<Object, Object> job = (jobData, executionContext) -> TestUtils.block(runningTime, testContext);
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(IntervalTrigger.builder().interval(Duration.ofSeconds(5)).repeat(2).build())
                         .setJob(job)
                         .setTimeoutPolicy(TimeoutPolicy.create(timeout))
                         .build()
                         .start();
    }

    @Test
    void test_scheduler_should_be_timeout_in_evaluation(Vertx vertx, VertxTestContext testContext) {
        final Duration timeout = Duration.ofSeconds(1);
        final Duration runningTime = Duration.ofSeconds(3);
        final Consumer<ExecutionResult<Object>> timeoutAsserter = result -> {
            final TriggerCondition condition = result.triggerContext().condition();
            Assertions.assertTrue(result.isTimeout());
            Assertions.assertEquals("TriggerEvaluationTimeout", condition.reasonCode());
            Assertions.assertEquals("Timeout after 1s", condition.cause().getMessage());
            testContext.completeNow();
        };
        final SchedulingMonitor<Object> asserter = SchedulingAsserter.builder()
                                                                     .setTestContext(testContext)
                                                                     .setMisfire(timeoutAsserter)
                                                                     .build();
        final TriggerEvaluator evaluator = TriggerEvaluator.byBefore((trigger, triggerContext, externalId) -> {
            TestUtils.block(runningTime, testContext);
            return Future.succeededFuture(triggerContext);
        });
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(IntervalTrigger.builder().interval(Duration.ofSeconds(5)).build())
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
        final SchedulingMonitor<Object> asserter = SchedulingAsserter.builder()
                                                                     .setTestContext(testContext)
                                                                     .setCompleted(completed)
                                                                     .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(Duration.ofSeconds(1)).repeat(5).build();
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
        final SchedulingMonitor<Object> asserter = SchedulingAsserter.builder()
                                                                     .setTestContext(testContext)
                                                                     .setCompleted(onCompleted)
                                                                     .build();
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(Duration.ofSeconds(1)).build();
        final IntervalScheduler scheduler = IntervalScheduler.builder()
                                                             .setVertx(vertx)
                                                             .setMonitor(asserter)
                                                             .setTrigger(trigger)
                                                             .setJob(NoopJob.create())
                                                             .build();
        scheduler.start();
        TestUtils.block(Duration.ofSeconds(3), testContext);
        scheduler.cancel();
    }

    private static Stream<Arguments> provide_external_ids() {
        return Stream.of(arguments(null, Integer.class), arguments(3L, Long.class), arguments("test", String.class),
                         arguments(UUID.randomUUID(), UUID.class), arguments(JsonArray.of(1, 2, 3), JsonArray.class),
                         arguments(JsonObject.of("k1", "v1"), JsonObject.class));
    }

    private static Stream<Arguments> provide_threads() {
        final WorkerThreadChecker c0 = WorkerThreadChecker.create(vertx -> null, "scheduler.x-worker-thread-60s");
        final WorkerThreadChecker c1 = WorkerThreadChecker.create(TimeoutPolicy.create(Duration.ofSeconds(5)),
                                                                  vertx -> null, "scheduler.x-worker-thread-5s");
        final WorkerThreadChecker c2 = WorkerThreadChecker.create(v -> v.createSharedWorkerExecutor("custom.thread"),
                                                                  "custom.thread");
        return Stream.of(arguments(Named.named("default scheduler.x thread", c0)),
                         arguments(Named.named("scheduler.x thread by timeout policy", c1)),
                         arguments(Named.named("given dedicated thread", c2)));
    }

    interface WorkerThreadChecker {

        TimeoutPolicy timeoutPolicy();

        WorkerExecutor createExecutor(Vertx vertx);

        default void doAssert() { doAssert(null); }

        void doAssert(String expectedThreadName);

        static WorkerThreadChecker create(Function<Vertx, WorkerExecutor> executorProvider, String threadName) {
            return create(null, executorProvider, threadName);
        }

        static WorkerThreadChecker create(TimeoutPolicy timeoutPolicy, Function<Vertx, WorkerExecutor> executorProvider,
                                          String threadName) {

            return new WorkerThreadChecker() {
                @Override
                public TimeoutPolicy timeoutPolicy() { return timeoutPolicy; }

                @Override
                public WorkerExecutor createExecutor(Vertx vertx) { return executorProvider.apply(vertx); }

                public void doAssert(String expectedThread) {
                    String currentThreadName = Thread.currentThread().getName();
                    String expectedThreadName = Optional.ofNullable(expectedThread).orElse(threadName);
                    Assertions.assertTrue(currentThreadName.startsWith(expectedThreadName),
                                          "Thread name must starts with '" + expectedThreadName +
                                          "', current thread: '" + currentThreadName + "'");
                }
            };
        }

    }

}
