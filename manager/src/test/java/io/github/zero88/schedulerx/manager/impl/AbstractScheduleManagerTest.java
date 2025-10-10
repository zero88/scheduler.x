package io.github.zero88.schedulerx.manager.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.SchedulingMonitor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class AbstractScheduleManagerTest {

    private TestScheduleManager manager;
    private JsonObject testData;

    static Stream<Arguments> provide_valid_configs() {
        return Stream.of(
            Arguments.of("Simple interval trigger", new JsonObject()
                .put("trigger", new JsonObject()
                    .put("type", "interval")
                    .put("interval", 5))),

            Arguments.of("Interval trigger with duration", new JsonObject()
                .put("trigger", new JsonObject()
                    .put("type", "interval")
                    .put("interval", "PT10S")
                    .put("repeat", 5))),

            Arguments.of("Cron trigger", new JsonObject()
                .put("trigger", new JsonObject()
                    .put("type", "cron")
                    .put("expression", "0 0/2 0 ? * * *"))),

            Arguments.of("Complex interval with rule", new JsonObject()
                .put("trigger", new JsonObject()
                    .put("type", "interval")
                    .put("interval", "PT1H")
                    .put("initialDelay", "PT3S")
                    .put("rule", new JsonObject()
                        .put("until", "2023-10-20T10:10:00Z"))))
        );
    }

    @BeforeEach
    void setUp(Vertx vertx) {
        manager = new TestScheduleManager(vertx);
        testData = new JsonObject();
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        manager.shutdown()
            .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void test_setup_is_required_before_load(VertxTestContext testContext) {
        manager.load()
            .onComplete(testContext.failing(throwable -> {
                assertInstanceOf(IllegalStateException.class, throwable);
                assertTrue(throwable.getMessage().contains("Manager must be setup before loading"));
                testContext.completeNow();
            }));
    }

    @Test
    void test_setup_with_config(VertxTestContext testContext) {
        JsonObject config = new JsonObject().put("key", "value");

        assertDoesNotThrow(() -> manager.setup(vertx, config));
        assertTrue(manager.isSetup);
        testContext.completeNow();
    }

    @Test
    void test_setup_with_null_config(VertxTestContext testContext) {
        assertDoesNotThrow(() -> manager.setup(vertx, null));
        assertTrue(manager.isSetup);
        testContext.completeNow();
    }

    @Test
    void test_load_empty_data_should_succeed(VertxTestContext testContext) {
        manager.setup(vertx, new JsonObject());
        manager.setDataToReturn(new JsonObject());

        manager.load()
            .onComplete(testContext.succeeding(result -> {
                assertEquals(0, manager.getSchedulerCount());
                testContext.completeNow();
            }));
    }

    @Test
    void test_load_single_interval_scheduler(VertxTestContext testContext) {
        JsonObject config = new JsonObject()
            .put("trigger", new JsonObject()
                .put("type", "interval")
                .put("interval", 5)
                .put("repeat", 3));

        manager.setup(vertx, new JsonObject());
        manager.setDataToReturn(config);

        manager.load()
            .onComplete(testContext.succeeding(result -> {
                assertEquals(1, manager.getSchedulerCount());
                assertNotNull(manager.getScheduler("default_scheduler"));
                testContext.completeNow();
            }));
    }

    @Test
    void test_load_multiple_schedulers_as_array(VertxTestContext testContext) {
        JsonArray schedulers = new JsonArray()
            .add(new JsonObject()
                .put("trigger", new JsonObject()
                    .put("type", "interval")
                    .put("interval", 5)))
            .add(new JsonObject()
                .put("trigger", new JsonObject()
                    .put("type", "cron")
                    .put("expression", "0 0/2 0 ? * * *")));

        JsonObject config = new JsonObject().put("schedulers", schedulers);

        manager.setup(vertx, new JsonObject());
        manager.setDataToReturn(config);

        manager.load()
            .onComplete(testContext.succeeding(result -> {
                assertEquals(2, manager.getSchedulerCount());
                assertNotNull(manager.getScheduler("scheduler_0"));
                assertNotNull(manager.getScheduler("scheduler_1"));
                testContext.completeNow();
            }));
    }

    @Test
    void test_run_without_schedulers(VertxTestContext testContext) {
        manager.setup(vertx, new JsonObject());

        manager.run()
            .onComplete(testContext.succeeding(result -> {
                assertEquals(0, manager.getSchedulerCount());
                testContext.completeNow();
            }));
    }

    @Test
    void test_complete_lifecycle(VertxTestContext testContext) {
        JsonObject config = new JsonObject()
            .put("trigger", new JsonObject()
                .put("type", "interval")
                .put("interval", 5)
                .put("repeat", 2));

        manager.setup(vertx, new JsonObject());
        manager.setDataToReturn(config);

        manager.load()
            .compose(v -> {
                assertEquals(1, manager.getSchedulerCount());
                return manager.run();
            })
            .compose(v -> manager.shutdown())
            .onComplete(testContext.succeeding(result -> {
                assertEquals(0, manager.getSchedulerCount());
                testContext.completeNow();
            }));
    }

    @Test
    void test_reload_functionality(VertxTestContext testContext) {
        // Initial config
        JsonObject initialConfig = new JsonObject()
            .put("trigger", new JsonObject()
                .put("type", "interval")
                .put("interval", 5));

        manager.setup(vertx, new JsonObject());
        manager.setDataToReturn(initialConfig);

        manager.load()
            .compose(v -> {
                assertEquals(1, manager.getSchedulerCount());

                // Change data for reload
                JsonObject newConfig = new JsonObject()
                    .put("schedulers", new JsonArray()
                        .add(new JsonObject()
                            .put("trigger", new JsonObject()
                                .put("type", "cron")
                                .put("expression", "0 0/2 0 ? * * *")))
                        .add(new JsonObject()
                            .put("trigger", new JsonObject()
                                .put("type", "interval")
                                .put("interval", 10))));

                manager.setDataToReturn(newConfig);
                return manager.reload();
            })
            .onComplete(testContext.succeeding(result -> {
                assertEquals(2, manager.getSchedulerCount());
                testContext.completeNow();
            }));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provide_valid_configs")
    void test_load_various_valid_configurations(String description, JsonObject config, VertxTestContext testContext) {
        manager.setup(vertx, new JsonObject());
        manager.setDataToReturn(config);

        manager.load()
            .onComplete(testContext.succeeding(result -> {
                assertTrue(manager.getSchedulerCount() > 0, "Should have loaded at least one scheduler");
                testContext.completeNow();
            }));
    }

    @Test
    void test_invalid_trigger_type_should_fail(VertxTestContext testContext) {
        JsonObject config = new JsonObject()
            .put("trigger", new JsonObject()
                .put("type", "invalid_type"));

        manager.setup(vertx, new JsonObject());
        manager.setDataToReturn(config);

        manager.load()
            .onComplete(testContext.failing(throwable -> {
                assertInstanceOf(IllegalArgumentException.class, throwable);
                assertTrue(throwable.getMessage().contains("Unsupported trigger type"));
                testContext.completeNow();
            }));
    }


    // Test implementation of AbstractManager for testing purposes
    static class TestScheduleManager extends AbstractScheduleManager {
        private JsonObject dataToReturn;

        public TestScheduleManager(Vertx vertx) {
            super(vertx);
        }

        public TestScheduleManager(Vertx vertx, SchedulingMonitor<Object> monitor) {
            super(vertx, monitor);
        }

        public void setDataToReturn(JsonObject data) {
            this.dataToReturn = data;
        }

        @Override
        protected Future<JsonObject> loadData() {
            if (dataToReturn == null) {
                return Future.succeededFuture(new JsonObject());
            }
            return Future.succeededFuture(dataToReturn);
        }
    }
}
