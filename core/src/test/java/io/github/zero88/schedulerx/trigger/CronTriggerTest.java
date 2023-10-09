package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.zero88.schedulerx.trigger.rule.Timeframe;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;
import io.vertx.core.json.jackson.DatabindCodec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class CronTriggerTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() { mapper = DatabindCodec.mapper(); }

    @Test
    void test_compare() throws JsonProcessingException {
        final CronTrigger t1 = CronTrigger.builder()
                                                  .expression("0 0/2 0 ? * * *")
                                                  .timeZone(TimeZone.getTimeZone("EST"))
                                                  .build();
        final String data = "{\"expression\":\"0 0/2 0 ? * * *\",\"timeZone\":\"EST\"}";
        final CronTrigger t2 = mapper.readValue(data, CronTrigger.class);
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
        final CronExpression cronExpression = ((CronTriggerImpl) trigger).cronExpression;
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
        final String json = mapper.writeValueAsString(trigger);
        Assertions.assertEquals("{\"type\":\"cron\",\"rule\":{\"timeframes\":[],\"until\":null},\"expression\":\"0 0/2 0 ? * * *\",\"timeZone\":\"PST\"}", json);
    }

    @Test
    void test_deserialize() throws JsonProcessingException {
        final String data = "{\"expression\":\"0 0/3 0 ? * * *\",\"timeZone\":\"PST\"}";
        final CronTrigger trigger = mapper.readValue(data, CronTrigger.class);
        Assertions.assertEquals("0 0/3 0 ? * * *", trigger.getExpression());
        Assertions.assertEquals(TimeZone.getTimeZone("PST"), trigger.getTimeZone());
        Assertions.assertEquals("cron", trigger.type());
    }

    @Test
    void test_deserialize_invalid_timezone() throws JsonProcessingException {
        final String data = "{\"expression\":\"0 0/3 0 ? * * *\",\"timeZone\":\"XXX\"}";
        final CronTrigger trigger = mapper.readValue(data, CronTrigger.class);
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
        Assertions.assertEquals(startedAt.toInstant(), parameter.getStartedAt());
        Assertions.assertIterableEquals(expected, trigger.preview(parameter));
    }

    @Test
    void test_preview_trigger_with_rule() {
        final Instant startedAt = OffsetDateTime.parse("2023-10-01T23:00:00Z").toInstant();
        final List<OffsetDateTime> expected = Arrays.asList(OffsetDateTime.parse("2023-10-03T22:00:00Z"),
                                                            OffsetDateTime.parse("2023-10-04T22:00:00Z"));

        final CronTrigger trigger = CronTrigger.builder().expression("0 0 22 ? * * *").build();
        final TriggerRule rule = TriggerRule.create(Collections.singletonList(
                                                        Timeframe.of(Instant.parse("2023-10-03T00:00:00Z"),
                                                                     Instant.parse("2023-10-06T00:00:00Z"))),
                                                    Instant.parse("2023-10-05T00:00:00Z"));
        final PreviewParameter parameter = PreviewParameter.byDefault().setStartedAt(startedAt).setRule(rule);
        Assertions.assertIterableEquals(expected, trigger.preview(parameter));
    }

}
