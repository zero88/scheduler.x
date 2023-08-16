package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class CronTriggerTest {

    @Test
    void test_compare() throws JsonProcessingException {
        final CronTrigger t1 = CronTrigger.builder()
                                          .expression("0 0/2 0 ? * * *")
                                          .timeZone(TimeZone.getTimeZone("EST"))
                                          .build();
        final String data = "{\"expression\":\"0 0/2 0 ? * * *\",\"timeZone\":\"EST\"}";
        final CronTrigger t2 = new ObjectMapper().readValue(data, CronTrigger.class);
        Assertions.assertEquals(t2, t1);
    }

    @Test
    void test_invalid_trigger() {
        final CronTrigger trigger = CronTrigger.builder().expression("a 0/2 0 ? * * *").build();
        Assertions.assertThrows(IllegalArgumentException.class, trigger::validate);
    }

    @Test
    void test_trigger() {
        final CronTrigger trigger = CronTrigger.builder().expression("0 0/2 0 ? * * *").build().validate();
        final CronExpression cronExpression = trigger.getCronExpression();
        final Instant parse = Instant.parse("2021-02-25T00:00:00Z");

        Assertions.assertEquals("GMT", trigger.getTimeZone().getID());
        Assertions.assertEquals(cronExpression.getTimeZone(), trigger.getTimeZone());
        Assertions.assertEquals(2 * 60 * 1000, trigger.nextTriggerAfter(parse));
        Assertions.assertThrows(NullPointerException.class, () -> trigger.nextTriggerAfter(null));
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
        Assertions.assertEquals("cron", trigger.type());
    }

    @Test
    void test_deserialize_invalid_timezone() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String data = "{\"expression\":\"0 0/3 0 ? * * *\",\"timeZone\":\"XXX\"}";
        final CronTrigger trigger = objectMapper.readValue(data, CronTrigger.class);
        Assertions.assertEquals("0 0/3 0 ? * * *", trigger.getExpression());
        Assertions.assertEquals(TimeZone.getTimeZone("GMT"), trigger.getTimeZone());
        Assertions.assertEquals("cron", trigger.type());
    }

    @Test
    void test_preview_trigger_by_default() {
        final CronTrigger trigger = CronTrigger.builder().expression("0 0/5 * * * ?").build();
        final List<OffsetDateTime> result = trigger.preview();
        Assertions.assertEquals(10, result.size());
        Assertions.assertEquals(0, result.stream().filter(Objects::isNull).count());
    }

    @Test
    void test_preview_trigger() {
        final TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        final OffsetDateTime startedAt = OffsetDateTime.parse("2023-07-30T18:01+07:00");
        final List<OffsetDateTime> expected = Arrays.asList(OffsetDateTime.parse("2023-07-30T11:05Z"),
                                                            OffsetDateTime.parse("2023-07-30T11:10Z"),
                                                            OffsetDateTime.parse("2023-07-30T11:15Z"),
                                                            OffsetDateTime.parse("2023-07-30T11:20Z"),
                                                            OffsetDateTime.parse("2023-07-30T11:25Z"));

        final CronTrigger trigger = CronTrigger.builder().expression("0 0/5 * * * ?").timeZone(timeZone).build();
        final PreviewParameter parameter = new PreviewParameter().setStartedAt(startedAt.toInstant())
                                                                 .setTimes(5)
                                                                 .setTimeZone(ZoneOffset.UTC);
        final List<OffsetDateTime> result = trigger.preview(parameter);
        Assertions.assertEquals(5, result.size());
        Assertions.assertIterableEquals(expected, result);
        Assertions.assertEquals(startedAt.toInstant(), parameter.getStartedAt());
    }

}
