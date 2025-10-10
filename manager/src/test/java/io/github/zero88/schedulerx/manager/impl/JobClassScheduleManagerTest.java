package io.github.zero88.schedulerx.manager.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.schedulerx.ExecutionContext;
import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class JobClassScheduleManagerTest {

    private TestScheduleManager manager;
    private AtomicReference<String> lastJobResult;

    @BeforeEach
    void setUp(Vertx vertx) {
        lastJobResult = new AtomicReference<>();

        // Create a monitor that will capture the job result
        SchedulingMonitor<Object> monitor = new SchedulingMonitor<Object>() {
            @Override
            public void onEach(ExecutionResult<Object> result) {
                if (result.data() != null) {
                    lastJobResult.set(result.data().toString());
                }
            }
        };

        manager = new TestScheduleManager(vertx, monitor);
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        manager.shutdown().onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void test_create_job_from_job_class(VertxTestContext testContext) {
        // Create a configuration with a jobClass specification
        JsonObject config = new JsonObject()
            .put("trigger", new JsonObject()
                .put("type", "interval")
                .put("interval", 1) // 1 second interval
                .put("repeat", 1))  // Just run once
            .put("job", new JsonObject()
                .put("jobClass", "io.github.zero88.schedulerx.manager.impl.TestCustomJob"))
            .put("jobData", new JsonObject()
                .put("external_id", "test-job-1")
                .put("message", "Hello from test"));

        manager.setup(vertx, new JsonObject());
        manager.setDataToReturn(config);

        // Load and run the scheduler
        manager.load()
              .compose(v -> manager.run())
              .onComplete(testContext.succeeding(result -> {
                  // Verify a scheduler was created with our jobClass
                  assertEquals(1, manager.getSchedulerCount());
                  assertNotNull(manager.getScheduler("test-job-1"));

                  // Wait a bit for the job to execute
                  vertx.setTimer(2000, id -> {
                      // Verify our custom job executed properly
                      String resultMsg = lastJobResult.get();
                      assertNotNull(resultMsg);
                      assertTrue(resultMsg.contains("Hello from test"));
                      testContext.completeNow();
                  });
              }));
    }
}

/**
 * Test custom job with a default constructor
 */
class TestCustomJob implements Job<Object, String> {
    @Override
    public void execute(JobData<Object> jobData, ExecutionContext<String> executionContext) {
        // Access config values from jobData instead of constructor
        String message = "Default message";
        if (jobData.get() instanceof JsonObject jobDataJson) {
            message = jobDataJson.getString("message", message);
        }
        executionContext.complete("TestCustomJob executed with message: " + message);
    }
}
