package io.github.zero88.vertx.scheduler.trigger;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class IntervalTriggerTest {

    @Test
    void test_serialize() throws JsonProcessingException {
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(10).intervalTimeUnit(TimeUnit.DAYS).build();
        final String json = new ObjectMapper().writeValueAsString(trigger);
        Assertions.assertEquals("{\"initialDelayTimeUnit\":\"SECONDS\",\"initialDelay\":0,\"repeat\":-1," +
                                "\"intervalTimeUnit\":\"DAYS\",\"interval\":10}", json);
    }

    @Test
    void test_deserialize() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String data = "{\"initialDelayTimeUnit\":\"SECONDS\",\"initialDelay\":0,\"repeat\":-1," +
                            "\"intervalTimeUnit\":\"DAYS\",\"interval\":10}";
        final IntervalTrigger trigger = objectMapper.readValue(data, IntervalTrigger.class);
        Assertions.assertEquals(10, trigger.getInterval());
        Assertions.assertEquals(TimeUnit.DAYS, trigger.getIntervalTimeUnit());
        Assertions.assertEquals(0, trigger.getInitialDelay());
        Assertions.assertEquals(TimeUnit.SECONDS, trigger.getInitialDelayTimeUnit());
    }

    @Test
    void test_deserialize_invalid() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String data = "{\"initialDelayTimeUnit\":\"SECONDS\",\"initialDelay\":-1,\"repeat\":0," +
                            "\"intervalTimeUnit\":\"DAYS\",\"interval\":-1}";
        final IntervalTrigger trigger = objectMapper.readValue(data, IntervalTrigger.class);
        Assertions.assertThrows(IllegalArgumentException.class, trigger::getInterval, "Invalid interval value");
        Assertions.assertThrows(IllegalArgumentException.class, trigger::getRepeat, "Invalid repeat value");
        Assertions.assertThrows(IllegalArgumentException.class, trigger::getInitialDelay, "Invalid initial delay value");
    }

}
