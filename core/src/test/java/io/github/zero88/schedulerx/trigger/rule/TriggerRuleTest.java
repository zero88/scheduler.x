package io.github.zero88.schedulerx.trigger.rule;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.cartesian.ArgumentSets;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SuppressWarnings("rawtypes")
class TriggerRuleTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper().findAndRegisterModules()
                                   .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                   .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                                   .setSerializationInclusion(Include.NON_NULL);
    }

    @Test
    void serialize_deserialize() throws JsonProcessingException {
        final String expected =
            "{\"timeframes\":[{\"from\":\"2023-09-24\",\"to\":\"2023-09-26\",\"type\":\"java.time.LocalDate\"}," +
            "{\"from\":\"09:39:33.514\",\"to\":\"11:39:33.514\",\"type\":\"java.time.LocalTime\"}]," +
            "\"beginTime\":\"2023-09-01T00:00:00Z\",\"until\":\"2023-09-24T03:31:48Z\",\"leeway\":\"PT5S\"}";
        final Timeframe<?> tf1 = Timeframe.of(LocalDate.parse("2023-09-24"), LocalDate.parse("2023-09-26"));
        final Timeframe<?> tf2 = Timeframe.of(LocalTime.parse("09:39:33.514"), LocalTime.parse("11:39:33.514"));
        final Instant beginTime = Instant.parse("2023-09-01T00:00:00Z");
        final Instant until = Instant.parse("2023-09-24T03:31:48Z");
        final TriggerRule triggerRule = TriggerRule.builder()
                                                   .beginTime(beginTime)
                                                   .timeframes(tf1, tf2)
                                                   .until(until)
                                                   .leeway(Duration.ofSeconds(5))
                                                   .build();
        final String data = mapper.writeValueAsString(triggerRule);
        final TriggerRule fromJson = mapper.readerFor(TriggerRule.class).readValue(data);
        Assertions.assertEquals(expected, data);
        Assertions.assertEquals(triggerRule, fromJson);
    }

    @ParameterizedTest
    @MethodSource("beginUntilData")
    void test_begin_must_be_before_until(Instant beginTime, Instant until, boolean isValid) {
        try {
            TriggerRule.builder().beginTime(beginTime).until(until).build();
        } catch (Exception ex) {
            if (!isValid) {
                Assertions.assertInstanceOf(IllegalArgumentException.class, ex);
                Assertions.assertEquals("The 'begin time' must be before the 'until time'", ex.getMessage());
            }
        }
    }

    private static Stream<Arguments> beginUntilData() {
        //@formatter:off
        return Stream.of(
            arguments(null, null, true),
            arguments(null, Instant.parse("2023-09-24T01:00:00Z"), true),
            arguments(Instant.parse("2023-09-24T01:00:00Z"), null, true),
            arguments(Instant.parse("2023-09-24T01:00:00Z"), Instant.parse("2023-09-24T02:00:00Z"), true),
            arguments(Instant.parse("2023-09-24T02:00:00Z"), Instant.parse("2023-09-24T01:00:00Z"), false),
            arguments(Instant.parse("2023-09-24T01:00:00Z"), Instant.parse("2023-09-24T01:00:00Z"), false)
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("untilTestData")
    void test_until(Instant until, FiredAtArgument arg) {
        TriggerRule rule = TriggerRule.builder().until(until).leeway(arg.leeway).build();
        Assertions.assertEquals(arg.expected, rule.isExceeded(arg.firedAt));
    }

    private static Stream<Arguments> untilTestData() {
        final Instant until = Instant.parse("2023-09-24T03:31:48Z");
        //@formatter:off
        return Stream.of(arguments(until, FiredAtArgument.of(Instant.parse("2023-09-22T00:00:48Z"), false)),
                         arguments(until, FiredAtArgument.of(Instant.parse("2023-09-24T00:00:48Z"), false)),
                         arguments(until, FiredAtArgument.of(Instant.parse("2023-09-24T03:31:48Z"), false)),
                         arguments(until, FiredAtArgument.of(Instant.parse("2023-09-24T04:31:48Z"), true)),
                         arguments(until, FiredAtArgument.of(Instant.parse("2023-09-25T03:31:48Z"), true)),
                         arguments(until, FiredAtArgument.of(Duration.ofSeconds(3), Instant.parse("2023-09-24T03:31:50Z"), false)),
                         arguments(until, FiredAtArgument.of(Duration.ofSeconds(3), Instant.parse("2023-09-24T03:31:52Z"), true)));
        //@formatter:on
    }

    private static Stream<Arguments> beginTestData() {
        final Instant beginTime = Instant.parse("2023-09-24T10:00:00Z");
        final Duration leeway = Duration.ofSeconds(5);
        //@formatter:off
        return Stream.of(
            arguments(null, Duration.ZERO, FiredAtArgument.of(beginTime, false)),
            arguments(beginTime, Duration.parse("PT59M50S"), FiredAtArgument.of(Instant.parse("2023-09-24T09:00:00Z"), true)),
            arguments(beginTime, Duration.parse("PT1S"), FiredAtArgument.of(Instant.parse("2023-09-24T09:59:49Z"), true)),
            arguments(beginTime, Duration.ZERO, FiredAtArgument.of(Instant.parse("2023-09-24T09:59:50Z"), true)),
            arguments(beginTime, Duration.parse("PT6S"), FiredAtArgument.of(leeway, Instant.parse("2023-09-24T09:59:49Z"), true)),
            arguments(beginTime, Duration.ZERO, FiredAtArgument.of(leeway, Instant.parse("2023-09-24T09:59:55Z"), true)),
            arguments(beginTime, Duration.ZERO, FiredAtArgument.of(Instant.parse("2023-09-24T10:00:00Z"), false)),
            arguments(beginTime, Duration.ZERO, FiredAtArgument.of(Instant.parse("2023-09-24T10:01:00Z"), false))
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("beginTestData")
    void test_calculate_the_register_time(Instant beginTime, Duration durationToRegister, FiredAtArgument arg) {
        TriggerRule rule = TriggerRule.builder().beginTime(beginTime).leeway(arg.leeway).build();
        Assertions.assertEquals(arg.expected, rule.isPending(arg.firedAt),
                                "the fired-at time should be before the begin time");
        Assertions.assertEquals(durationToRegister, rule.calculateRegisterTime(arg.firedAt));
    }

    private static Stream<Arguments> leewayTestData() {
        return Stream.of(arguments(null, Duration.ZERO), arguments(0.0, Duration.ZERO), arguments(0, Duration.ZERO),
                         arguments("-PT1H", Duration.ZERO), arguments("PT5S", Duration.ofSeconds(5)),
                         arguments("PT1M", Duration.ofSeconds(10)));
    }

    @ParameterizedTest
    @MethodSource("leewayTestData")
    void test_leeway_serialize_deserialize(Object leeway, Duration expected) throws JsonProcessingException {
        if (leeway instanceof String) {
            leeway = "\"" + leeway + "\"";
        }
        final String data = "{\"timeframes\":[],\"until\":\"2023-09-24T03:31:48Z\",\"leeway\":" + leeway + "}";
        final TriggerRule rule = mapper.readerFor(TriggerRule.class).readValue(data);
        Assertions.assertEquals(expected, rule.leeway());
    }

    private static @NotNull List<Timeframe<OffsetTime>> offsetTimeRanges() {
        return Arrays.asList(Timeframe.of(OffsetTime.of(LocalTime.parse("09:00:00"), ZoneOffset.UTC),
                                          OffsetTime.of(LocalTime.parse("11:30:00"), ZoneOffset.UTC)),
                             Timeframe.of(OffsetTime.of(LocalTime.parse("23:00:00"), ZoneOffset.ofHours(-1)),
                                          OffsetTime.of(LocalTime.parse("02:30:00"), ZoneOffset.ofHours(-1))));
    }

    private static @NotNull List<Timeframe<LocalDate>> localDateRanges() {
        return Arrays.asList(Timeframe.to(LocalDate.parse("2023-09-20")),
                             Timeframe.of(LocalDate.parse("2023-09-22"), LocalDate.parse("2023-09-23")));
    }

    private static ArgumentSets timeframesTestData() {
        return ArgumentSets.argumentsForFirstParameter(ZoneOffset.ofHours(-1))
                           .argumentsForNextParameter(localDateRanges(), offsetTimeRanges())
                           .argumentsForNextParameter(FiredAtArgument.of(Instant.parse("2023-09-18T01:00:00Z"), true),
                                                      FiredAtArgument.of(Instant.parse("2023-09-22T03:29:59Z"), true),
                                                      FiredAtArgument.of(Instant.parse("2023-09-22T09:00:00Z"), true),
                                                      FiredAtArgument.of(Instant.parse("2023-09-22T11:29:59Z"), true),
                                                      FiredAtArgument.of(Instant.parse("2023-09-23T00:00:01Z"), true),
                                                      FiredAtArgument.of(Duration.ofSeconds(7),
                                                                         Instant.parse("2023-09-22T03:30:06Z"), true),
                                                      FiredAtArgument.of(Instant.parse("2023-09-20T04:00:00Z"), false),
                                                      FiredAtArgument.of(Instant.parse("2023-09-21T08:59:59Z"), false),
                                                      FiredAtArgument.of(Instant.parse("2023-09-23T11:30:00Z"), false),
                                                      FiredAtArgument.of(Instant.parse("2023-09-24T22:00:00Z"), false),
                                                      FiredAtArgument.of(Duration.ofSeconds(7),
                                                                         Instant.parse("2023-09-25T03:30:08Z"), false));
    }

    @CartesianTest
    @CartesianTest.MethodFactory("timeframesTestData")
    void test_TimeFrames_satisfy(ZoneId zoneId, List<Timeframe> timeframes, FiredAtArgument arg) {
        final TimeZone systemTimeZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
            TriggerRule rule = TriggerRule.builder().timeframes(timeframes).leeway(arg.leeway).build();
            Assertions.assertEquals(arg.expected, rule.satisfy(arg.firedAt));
        } finally {
            TimeZone.setDefault(systemTimeZone);
        }
    }

}
