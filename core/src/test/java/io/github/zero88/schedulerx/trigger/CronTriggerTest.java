package io.github.zero88.schedulerx.trigger;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.trigger.rule.Timeframe;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

class CronTriggerTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = DatabindCodec.mapper()
                              .findAndRegisterModules()
                              .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    static Stream<Arguments> validData() {
        final TriggerRule rule = TriggerRule.builder()
                                            .until(Instant.parse("2023-10-20T10:10:00Z"))
                                            .timeframe(Timeframe.of(Instant.parse("2023-10-10T10:10:00Z"), null))
                                            .build();
        final JsonObject ruleJson = JsonObject.of("until", "2023-10-20T10:10:00Z", "timeframes", JsonArray.of(
            JsonObject.of("type", "java.time.Instant", "from", "2023-10-10T10:10:00Z")));
        // @formatter:off
        return Stream.of(arguments(CronTrigger.builder().expression("0 0/2 0 ? * * *").build(),
                                   JsonObject.of("expression", "0 0/2 0 ? * * *", "timeZone", "GMT")),
                         arguments(CronTrigger.builder().expression("0 0/2 0 ? * * *").build(),
                                   JsonObject.of("type", "cron", "expression", "0 0/2 0 ? * * *", "timeZone", "GMT")),
                         arguments(CronTrigger.builder().expression("0 0/2 0 ? * * *").timeZone(TimeZone.getTimeZone("EST")).build(),
                                   JsonObject.of("type", "cron", "expression", "0 0/2 0 ? * * *", "timeZone", "EST")),
                         arguments(CronTrigger.builder().expression("0 0/2 0 ? * * *").rule(rule).build(),
                                   JsonObject.of("expression", "0 0/2 0 ? * * *", "timeZone", "GMT", "rule", ruleJson)));
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("validData")
    void test_serialize_deserialize(CronTrigger trigger, JsonObject json) throws JsonProcessingException {
        final CronTrigger t2 = mapper.readValue(json.encode(), CronTrigger.class);
        Assertions.assertEquals(t2, trigger);
        Assertions.assertEquals(t2.toJson(), trigger.toJson());
        Assertions.assertEquals(t2.toJson().encode(), mapper.writeValueAsString(trigger));
    }

    static Stream<Arguments> invalidExpression() {
        return Stream.of(arguments(""), arguments("a 0/2 0 ? * * *"), arguments(JsonObject.of("expression", "*")));
    }

    @ParameterizedTest
    @MethodSource("invalidExpression")
    void test_invalid_expression(Object exprOrJson) throws JsonProcessingException {
        CronTrigger trigger;
        if (exprOrJson instanceof String) {
            trigger = CronTrigger.builder().expression((String) exprOrJson).build();
        } else {
            trigger = mapper.readValue(((JsonObject) exprOrJson).encode(), CronTrigger.class);
        }
        Assertions.assertThrows(IllegalArgumentException.class, trigger::validate);
    }

    @Test
    void test_should_throw_NPE_when_null_expression() {
        String exMsg = "Cron expression is required";
        NullPointerException npe = Assertions.assertThrows(NullPointerException.class,
                                                           () -> CronTrigger.builder().expression(null).build());
        Assertions.assertEquals(exMsg, npe.getMessage());

        JsonProcessingException jsonEx = Assertions.assertThrows(JsonProcessingException.class, () -> mapper.readValue(
            JsonObject.of("expression", null).encode(), CronTrigger.class));
        Assertions.assertInstanceOf(NullPointerException.class, jsonEx.getCause());
        Assertions.assertEquals(exMsg, jsonEx.getCause().getMessage());
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
        final TriggerRule rule = TriggerRule.builder()
                                            .until(Instant.parse("2023-10-05T00:00:00Z"))
                                            .timeframe(Timeframe.of(Instant.parse("2023-10-03T00:00:00Z"),
                                                                    Instant.parse("2023-10-06T00:00:00Z")))
                                            .build();
        final PreviewParameter parameter = PreviewParameter.byDefault().setStartedAt(startedAt).setRule(rule);
        Assertions.assertIterableEquals(expected, trigger.preview(parameter));
    }

}
