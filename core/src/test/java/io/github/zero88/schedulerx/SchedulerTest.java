package io.github.zero88.schedulerx;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.trigger.CronScheduler;
import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.github.zero88.schedulerx.trigger.IntervalScheduler;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
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
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
        };
        final SchedulingAsserter<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                    .setTestContext(testCtx)
                                                                    .setCompleted(completed)
                                                                    .build();
        final CronScheduler<Void, Void> executor = CronScheduler.<Void, Void>builder()
                                                                .setVertx(vertx)
                                                                .setTrigger(trigger)
                                                                .setTask(NoopTask.create(2))
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

    @Test
    void test_scheduler_should_run_task_in_dedicated_thread(Vertx vertx, VertxTestContext testContext) {
        final String threadName = "HELLO";
        final WorkerExecutor worker = vertx.createSharedWorkerExecutor(threadName, 1);
        final SchedulingAsserter<Object> asserter = SchedulingAsserter.builder()
                                                                      .setTestContext(testContext)
                                                                      .build();
        final Task<Object, Object> task = (d, ec) -> Assertions.assertEquals(threadName + "-0",
                                                                             Thread.currentThread().getName());
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(2).repeat(1).build();
        IntervalScheduler.builder()
                         .setVertx(vertx)
                         .setMonitor(asserter)
                         .setTrigger(trigger)
                         .setTask(task)
                         .build()
                         .start(worker);
    }

    @ParameterizedTest
    @MethodSource("provide_external_ids")
    void test_scheduler_should_maintain_external_id_from_jobData_to_task_result(Object declaredId, Class<?> typeOfId,
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
                         .setTask(NoopTask.create())
                         .setJobData(jobdata)
                         .build()
                         .start();
    }

    @Test
    void test_scheduler_should_able_to_force_stop(Vertx vertx, VertxTestContext testContext) {
        final Consumer<ExecutionResult<Object>> completed = result -> {
            Assertions.assertNotNull(result.availableAt());
            Assertions.assertNotNull(result.completedAt());
            Assertions.assertEquals(3, result.round());
            Assertions.assertEquals(3, result.tick());
            Assertions.assertTrue(result.isCompleted());
            Assertions.assertFalse(result.isError());
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
                         .setTask(NoopTask.create(3))
                         .build()
                         .start();
    }

    private static Stream<Object> provide_external_ids() {
        return Stream.of(arguments(null, Integer.class), arguments(3L, Long.class), arguments("test", String.class),
                         arguments(UUID.randomUUID(), UUID.class), arguments(JsonArray.of(1, 2, 3), JsonArray.class),
                         arguments(JsonObject.of("k1", "v1"), JsonObject.class));
    }

}
