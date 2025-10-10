package io.github.zero88.schedulerx.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.schedulerx.SchedulingAsserter;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class ScheduleManagerIntegrationTest {

    private JsonFileScheduleManager manager;
    private String testFilePath;
    private FileSystem fs;

    @BeforeEach
    void setUp(Vertx vertx) throws Exception {
        fs = vertx.fileSystem();

        // Create a temporary test file
        Path tempFile = Files.createTempFile("scheduler-integration-test", ".json");
        testFilePath = tempFile.toString();
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) throws Exception {
        if (manager != null) {
            manager.shutdown()
                   .compose(v -> fs.delete(testFilePath).recover(t -> null))
                   .onComplete(testContext.succeedingThenComplete());
        } else {
            testContext.completeNow();
        }
    }

    @Test
    void test_manager_with_real_scheduler_execution(Vertx vertx, VertxTestContext testContext)
        throws InterruptedException {
        final AtomicInteger executionCount = new AtomicInteger(0);
        final int expectedExecutions = 3;

        // Create a monitoring that counts executions using SchedulingAsserterBuilder
        SchedulingMonitor<Object> monitor = SchedulingAsserter.builder()
                                                              .setTestContext(testContext)
                                                              .disableAutoCompleteTest() // We'll complete
                                                              // manually when we reach the expected count
                                                              .setEach(result -> {
                                                                  int count = executionCount.incrementAndGet();
                                                                  if (count >= expectedExecutions) {
                                                                      testContext.completeNow();
                                                                  }
                                                              })
                                                              .build();

        manager = new JsonFileScheduleManager(vertx, monitor);

        // Create configuration for a fast interval scheduler
        JsonObject testData = new JsonObject().put("trigger", new JsonObject().put("type", "interval")
                                                                              .put("interval", 1) // 1 second
                                                                              .put("repeat", expectedExecutions));

        fs.writeFile(testFilePath, testData.toBuffer()).compose(v -> {
            manager.setup(vertx, new JsonObject().put("filePath", testFilePath));
            return manager.load();
        }).compose(v -> {
            assertEquals(1, manager.getSchedulerCount());
            return manager.run();
        }).onComplete(testContext.succeeding(result -> {
            // Execution counting is handled in the monitor
        }));

        // Wait for the scheduler to execute the expected number of times
        assertTrue(testContext.awaitCompletion(15, TimeUnit.SECONDS));
        assertTrue(executionCount.get() >= expectedExecutions);
    }

    @Test
    void test_manager_reload_functionality(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        final AtomicInteger phase1Count = new AtomicInteger(0);
        final AtomicInteger phase2Count = new AtomicInteger(0);
        final AtomicInteger totalCount = new AtomicInteger(0);

        SchedulingMonitor<Object> monitor = SchedulingAsserter.builder()
                                                              .setTestContext(testContext)
                                                              .disableAutoCompleteTest() // We'll complete manually
                                                              // when phase 2 reaches the expected count
                                                              .setEach(result -> {
                                                                  int total = totalCount.incrementAndGet();

                                                                  if (total <= 2) {
                                                                      phase1Count.incrementAndGet();
                                                                  } else {
                                                                      phase2Count.incrementAndGet();
                                                                      if (phase2Count.get() >= 2) {
                                                                          testContext.completeNow();
                                                                      }
                                                                  }
                                                              })
                                                              .build();

        manager = new JsonFileScheduleManager(vertx, monitor);

        // Phase 1: Load initial configuration
        JsonObject initialConfig = new JsonObject().put("trigger", new JsonObject().put("type", "interval")
                                                                                   .put("interval", 1)
                                                                                   .put("repeat", 2));

        fs.writeFile(testFilePath, initialConfig.toBuffer()).compose(v -> {
            manager.setup(vertx, new JsonObject().put("filePath", testFilePath));
            return manager.load();
        }).compose(v -> {
            assertEquals(1, manager.getSchedulerCount());
            return manager.run();
        }).compose(v -> {
            // Wait a bit for some executions, then reload
            return vertx.executeBlocking(promise -> {
                try {
                    Thread.sleep(3000); // Wait for 3 seconds
                    promise.complete();
                } catch (InterruptedException e) {
                    promise.fail(e);
                }
            });
        }).compose(v -> {
            // Phase 2: Reload with new configuration
            JsonObject newConfig = new JsonObject().put("trigger", new JsonObject().put("type", "interval")
                                                                                   .put("interval", 1)
                                                                                   .put("repeat", 3));

            return fs.writeFile(testFilePath, newConfig.toBuffer());
        }).compose(v -> manager.reload()).onComplete(testContext.succeeding(result -> {
            assertEquals(1, manager.getSchedulerCount());
        }));

        assertTrue(testContext.awaitCompletion(20, TimeUnit.SECONDS));
        assertTrue(phase1Count.get() >= 1, "Phase 1 should have at least 1 execution");
        assertTrue(phase2Count.get() >= 1, "Phase 2 should have at least 1 execution");
    }

    @Test
    void test_manager_with_complex_configuration(Vertx vertx, VertxTestContext testContext)
        throws InterruptedException {
        manager = new JsonFileScheduleManager(vertx);

        // Create a complex configuration with multiple schedulers
        JsonObject complexConfig = new JsonObject().put("schedulers", new JsonObject().put("fastScheduler",
                                                                                           new JsonObject().put(
                                                                                                               "trigger",
                                                                                                               new JsonObject().put(
                                                                                                                                   "type", "interval")
                                                                                                                               .put(
                                                                                                                                   "interval",
                                                                                                                                   2)
                                                                                                                               .put(
                                                                                                                                   "repeat",
                                                                                                                                   2))
                                                                                                           .put(
                                                                                                               "jobData",
                                                                                                               new JsonObject().put(
                                                                                                                   "externalId",
                                                                                                                   "fast-job")))
                                                                                      .put("cronScheduler",
                                                                                           new JsonObject().put(
                                                                                                               "trigger",
                                                                                                               new JsonObject().put(
                                                                                                                                   "type", "cron")
                                                                                                                               .put(
                                                                                                                                   "expression",
                                                                                                                                   "0/5 * * ? * * *")) // Every 5 seconds
                                                                                                           .put(
                                                                                                               "jobData",
                                                                                                               new JsonObject().put(
                                                                                                                   "externalId",
                                                                                                                   "cron-job"))));

        fs.writeFile(testFilePath, complexConfig.toBuffer()).compose(v -> {
            manager.setup(vertx, new JsonObject().put("filePath", testFilePath));
            return manager.load();
        }).onComplete(testContext.succeeding(result -> {
            assertEquals(2, manager.getSchedulerCount());
            assertNotNull(manager.getScheduler("fastScheduler"));
            assertNotNull(manager.getScheduler("cronScheduler"));
            testContext.completeNow();
        }));

        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    void test_manager_error_handling_with_invalid_config(Vertx vertx, VertxTestContext testContext)
        throws InterruptedException {
        manager = new JsonFileScheduleManager(vertx);

        // Configuration with invalid trigger data
        JsonObject invalidConfig = new JsonObject().put("schedulers", new JsonArray().add(
                                                                                         new JsonObject().put(
                                                                                             "trigger",
                                                                                             new JsonObject().put(
                                                                                                 "type", "interval")
                                                                                                                                         .put("interval", -1))) // Invalid negative interval
                                                                                     .add(
                                                                                         new JsonObject().put("trigger",
                                                                                                              new JsonObject().put(
                                                                                                                                  "type",
                                                                                                                                  "cron")
                                                                                                                              .put(
                                                                                                                                  "expression",
                                                                                                                                  "invalid-cron-expression"))));

        fs.writeFile(testFilePath, invalidConfig.toBuffer()).compose(v -> {
            manager.setup(vertx, new JsonObject().put("filePath", testFilePath));
            return manager.load();
        }).onComplete(testContext.failing(throwable -> {
            assertTrue(throwable instanceof IllegalArgumentException ||
                       throwable.getCause() instanceof IllegalArgumentException);
            assertEquals(0, manager.getSchedulerCount());
            testContext.completeNow();
        }));

        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    }

    @Test
    void test_full_lifecycle_with_file_operations(Vertx vertx, VertxTestContext testContext)
        throws InterruptedException {
        manager = new JsonFileScheduleManager(vertx);

        JsonObject initialData = new JsonObject().put("trigger", new JsonObject().put("type", "interval")
                                                                                 .put("interval", 5)
                                                                                 .put("repeat", 1));

        fs.writeFile(testFilePath, initialData.toBuffer()).compose(v -> {
            manager.setup(vertx, new JsonObject().put("filePath", testFilePath));
            return manager.load();
        }).compose(v -> {
            assertEquals(1, manager.getSchedulerCount());
            return manager.run();
        }).compose(v -> {
            // Save new data
            JsonObject newData = new JsonObject().put("schedulers", new JsonArray().add(
                new JsonObject().put("trigger", new JsonObject().put("type", "interval").put("interval", 3))));
            return manager.saveData(newData);
        }).compose(v -> manager.reload()).compose(v -> manager.shutdown()).onComplete(testContext.succeeding(result -> {
            assertEquals(0, manager.getSchedulerCount());
            testContext.completeNow();
        }));

        assertTrue(testContext.awaitCompletion(10, TimeUnit.SECONDS));
    }

}
