package io.github.zero88.vertx.scheduler.trigger;

import java.time.Instant;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class CronTriggerTest {

    @Test
    void test_invalid_trigger() {
        final CronTrigger trigger = CronTrigger.builder().expression("a 0/2 0 ? * * *").build();
        Assertions.assertThrows(IllegalArgumentException.class, trigger::getCronExpression);
    }

    @Test
    void test_trigger() {
        final CronTrigger trigger = CronTrigger.builder().expression("0 0/2 0 ? * * *").build();
        final CronExpression cronExpression = trigger.getCronExpression();
        final Instant parse = Instant.parse("2021-02-25T00:00:00Z");

        Assertions.assertEquals("GMT", trigger.getTimeZone().getID());
        Assertions.assertEquals(cronExpression.getTimeZone(), trigger.getTimeZone());
        Assertions.assertEquals(2 * 60 * 1000, trigger.nextTriggerAfter(parse));
    }

    @Test
    void test_serialize() throws JsonProcessingException {
        final TimeZone tz = TimeZone.getTimeZone("PST");
        final CronTrigger trigger = CronTrigger.builder().expression("0 0/2 0 ? * * *").timeZone(tz).build();
        final String json = new ObjectMapper().writeValueAsString(trigger);
        Assertions.assertEquals("{\"expression\":\"0 0/2 0 ? * * *\",\"timeZone\":\"PST\"}", json);
    }

    @Test
    void test_deserialize() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String data = "{\"expression\":\"0 0/3 0 ? * * *\",\"timeZone\":\"PST\"}";
        final CronTrigger trigger = objectMapper.readValue(data, CronTrigger.class);
        Assertions.assertEquals("0 0/3 0 ? * * *", trigger.getExpression());
        Assertions.assertEquals(TimeZone.getTimeZone("PST"), trigger.getTimeZone());
    }

    @Test
    void test_deserialize_invalid_timezone() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String data = "{\"expression\":\"0 0/3 0 ? * * *\",\"timeZone\":\"XXX\"}";
        final CronTrigger trigger = objectMapper.readValue(data, CronTrigger.class);
        Assertions.assertEquals("0 0/3 0 ? * * *", trigger.getExpression());
        Assertions.assertEquals(TimeZone.getTimeZone("GMT"), trigger.getTimeZone());
    }

}
