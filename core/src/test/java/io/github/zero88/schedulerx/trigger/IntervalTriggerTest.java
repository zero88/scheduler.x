package io.github.zero88.schedulerx.trigger;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.trigger.rule.Timeframe;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;

class IntervalTriggerTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = DatabindCodec.mapper()
                              .findAndRegisterModules()
                              .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    static Stream<Arguments> validData() {
        final TriggerRule ruleWithoutBeginTime = TriggerRule.builder()
                                                            .until(Instant.parse("2023-10-20T10:10:00Z"))
                                                            .build();
        final JsonObject ruleWithoutBeginTimeJson = JsonObject.of("until", "2023-10-20T10:10:00Z");
        final TriggerRule ruleWithBeginTime = TriggerRule.builder()
                                                         .beginTime(Instant.parse("2023-10-18T10:10:00Z"))
                                                         .until(Instant.parse("2023-10-20T10:10:00Z"))
                                                         .build();
        final JsonObject ruleWithBeginTimeJson = JsonObject.of("beginTime", "2023-10-18T10:10:00Z", "until",
                                                               "2023-10-20T10:10:00Z");
        // @formatter:off
        return Stream.of(
            arguments(IntervalTrigger.builder().interval(10).build(),
                      new JsonObject("{\"interval\":10}")),
            arguments(IntervalTrigger.builder().interval(20).build(),
                      new JsonObject("{\"type\":\"interval\",\"repeat\":-1,\"initialDelay\":0,\"initialDelayTimeUnit\":\"SECONDS\",\"interval\":20,\"intervalTimeUnit\":\"SECONDS\"}")),
            arguments(IntervalTrigger.builder().initialDelay(30).interval(5).repeat(10).build(),
                      new JsonObject("{\"repeat\":10,\"initialDelay\":30,\"interval\":5}")),
            arguments(IntervalTrigger.builder().initialDelay(1).initialDelayTimeUnit(TimeUnit.HOURS).interval(10).intervalTimeUnit(TimeUnit.MINUTES).repeat(3).build(),
                      new JsonObject("{\"repeat\":3,\"initialDelay\":1,\"initialDelayTimeUnit\":\"HOURS\",\"interval\":10,\"intervalTimeUnit\":\"MINUTES\"}")),
            arguments(IntervalTrigger.builder().initialDelay(Duration.ofSeconds(1)).interval(Duration.ofSeconds(60)).repeat(15).build(),
                      JsonObject.of("type", "interval", "repeat", 15, "initialDelay", "PT1S","interval", "PT60S")),
            arguments(IntervalTrigger.builder().interval(Duration.ofHours(1)).initialDelay(Duration.ofSeconds(3)).rule(ruleWithoutBeginTime).build(),
                      JsonObject.of("interval", "PT1H", "initialDelay", "PT3S", "rule", ruleWithoutBeginTimeJson)),
            arguments(IntervalTrigger.builder().interval(Duration.ofHours(1)).initialDelay(Duration.ofSeconds(3)).rule(ruleWithBeginTime).build(),
                      JsonObject.of("interval", "PT1H", "rule", ruleWithBeginTimeJson)));
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("validData")
    void test_serialize_deserialize(IntervalTrigger trigger, JsonObject json) throws JsonProcessingException {
        final IntervalTrigger t1 = mapper.readValue(json.encode(), IntervalTrigger.class);
        Assertions.assertEquals(t1, trigger);
        Assertions.assertEquals(t1.toJson(), trigger.toJson());
        Assertions.assertEquals(t1.toJson().encode(), mapper.writeValueAsString(trigger));
        Assertions.assertEquals(json.mapTo(IntervalTrigger.class), trigger);
    }

    static Stream<Arguments> invalidData() {
        return Stream.of(arguments(JsonObject.of("repeat", 0), IllegalArgumentException.class, "Invalid repeat value"),
                         arguments(JsonObject.of("repeat", -10), IllegalArgumentException.class,
                                   "Invalid repeat value"),
                         arguments(JsonObject.of("interval", -1), IllegalArgumentException.class,
                                   "Invalid interval value"),
                         arguments(JsonObject.of("initialDelayTimeUnit", "SECONDS"), IllegalArgumentException.class,
                                   "Invalid interval value"),
                         arguments(JsonObject.of("interval", 10, "initialDelay", -1), IllegalArgumentException.class,
                                   "Invalid initial delay value"));
    }

    @ParameterizedTest
    @MethodSource("invalidData")
    void test_deserialize_invalid_value(JsonObject input, Class<Exception> exCls, String expected)
        throws JsonProcessingException {
        final IntervalTrigger intervalTrigger = mapper.readValue(input.encode(), IntervalTrigger.class);
        Throwable cause = Assertions.assertThrows(exCls, intervalTrigger::validate);
        Assertions.assertEquals(expected, cause.getMessage());
    }

    @ParameterizedTest
    @CsvSource({ "{\"initialDelayTimeUnit\":\"SECONDS1\"}" })
    void test_deserialize_invalid_format(String input) {
        final Throwable t = Assertions.assertThrows(ValueInstantiationException.class,
                                                    () -> mapper.readValue(input, IntervalTrigger.class));
        Assertions.assertInstanceOf(IllegalArgumentException.class, t.getCause());
    }

    @Test
    void test_preview_trigger() {
        final OffsetDateTime startedAt = OffsetDateTime.parse("2023-07-30T18:01+07:00");
        final List<OffsetDateTime> expected = Arrays.asList(OffsetDateTime.parse("2023-07-30T18:11:10+07:00"),
                                                            OffsetDateTime.parse("2023-07-30T18:21:10+07:00"),
                                                            OffsetDateTime.parse("2023-07-30T18:31:10+07:00"));

        final IntervalTrigger trigger = IntervalTrigger.builder()
                                                       .initialDelay(Duration.ofSeconds(10))
                                                       .interval(Duration.ofMinutes(10))
                                                       .repeat(3)
                                                       .build();
        final PreviewParameter parameter = PreviewParameter.byDefault()
                                                           .setStartedAt(startedAt.toInstant())
                                                           .setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        Assertions.assertIterableEquals(expected, trigger.preview(parameter));
    }

    @Test
    void test_preview_trigger_with_rule() {
        final OffsetDateTime startedAt = OffsetDateTime.parse("2023-07-30T18:01+07:00");
        final List<OffsetDateTime> expected = Arrays.asList(OffsetDateTime.parse("2023-07-30T19:01:00+07:00"),
                                                            OffsetDateTime.parse("2023-07-30T19:31:00+07:00"),
                                                            OffsetDateTime.parse("2023-07-30T20:01:00+07:00"),
                                                            OffsetDateTime.parse("2023-07-30T20:31:00+07:00"));

        final IntervalTrigger trigger = IntervalTrigger.builder().interval(Duration.ofMinutes(30)).build();
        final TriggerRule rule = TriggerRule.builder()
                                            .timeframe(Timeframe.of(OffsetDateTime.parse("2023-07-30T19:00:00+07:00"),
                                                                    OffsetDateTime.parse("2023-07-31T00:00:00+07:00")))
                                            .until(OffsetDateTime.parse("2023-07-30T21:00:00+07:00").toInstant())
                                            .build();
        final PreviewParameter parameter = PreviewParameter.byDefault()
                                                           .setStartedAt(startedAt.toInstant())
                                                           .setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
                                                           .setRule(rule);
        Assertions.assertIterableEquals(expected, trigger.preview(parameter));
    }

}
