package io.github.zero88.schedulerx.manager.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;

@ExtendWith(VertxExtension.class)
class TriggerDeserializationTest {

    @Test
    void test_create_interval_trigger() {
        TestScheduleManager manager = new TestScheduleManager(Vertx.vertx());

        JsonObject triggerConfig = new JsonObject().put("type", "interval").put("interval", 60).put("repeat", 5);

        Trigger trigger = manager.createTrigger("interval", triggerConfig);

        assertNotNull(trigger);
        assertInstanceOf(IntervalTrigger.class, trigger);
        IntervalTrigger intervalTrigger = (IntervalTrigger) trigger;
        assertEquals(5, intervalTrigger.getRepeat());
        assertEquals(60, intervalTrigger.getInterval());
    }

    @Test
    void test_create_cron_trigger() {
        TestScheduleManager manager = new TestScheduleManager(Vertx.vertx());

        JsonObject triggerConfig = new JsonObject().put("type", "cron").put("expression", "0 0/5 * * * ? *");

        Trigger trigger = manager.createTrigger("cron", triggerConfig);

        assertNotNull(trigger);
        assertInstanceOf(CronTrigger.class, trigger);
        CronTrigger cronTrigger = (CronTrigger) trigger;
        assertEquals("0 0/5 * * * ? *", cronTrigger.getExpression());
    }

    @Test
    void test_invalid_trigger_config() {
        TestScheduleManager manager = new TestScheduleManager(Vertx.vertx());

        JsonObject invalidConfig = new JsonObject().put("type", "interval")
                                                   .put("interval", -10); // Invalid negative interval

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.createTrigger("interval", invalidConfig);
        });

        assertTrue(exception.getMessage().contains("Invalid trigger configuration"));
    }

    @Test
    void test_unsupported_trigger_type() {
        TestScheduleManager manager = new TestScheduleManager(Vertx.vertx());

        JsonObject config = new JsonObject().put("type", "unsupported");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.createTrigger("unsupported", config);
        });

        assertTrue(exception.getMessage().contains("Unsupported trigger type"));
    }

}
