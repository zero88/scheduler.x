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
                                   .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                                   .setSerializationInclusion(Include.NON_NULL);
    }

    @Test
    void serialize_deserialize() throws JsonProcessingException {
        final String expected =
            "{\"timeframes\":[{\"from\":\"2023-09-24\",\"to\":\"2023-09-26\",\"type\":\"java.time.LocalDate\"}," +
            "{\"from\":\"09:39:33.514\",\"to\":\"11:39:33.514\",\"type\":\"java.time.LocalTime\"}]," +
            "\"until\":\"2023-09-24T03:31:48Z\",\"leeway\":0.0}";
        final Timeframe<?> tf1 = Timeframe.of(LocalDate.parse("2023-09-24"), LocalDate.parse("2023-09-26"));
        final Timeframe<?> tf2 = Timeframe.of(LocalTime.parse("09:39:33.514"), LocalTime.parse("11:39:33.514"));
        final Instant until = Instant.parse("2023-09-24T03:31:48Z");
        final TriggerRule triggerRule = TriggerRule.create(Arrays.asList(tf1, tf2), until);
        final String data = mapper.writeValueAsString(triggerRule);
        final TriggerRule fromJson = mapper.readerFor(TriggerRule.class).readValue(data);
        Assertions.assertEquals(expected, data);
        Assertions.assertEquals(triggerRule, fromJson);
    }

    private static Stream<Arguments> untilTestData() {
        final Instant until = Instant.parse("2023-09-24T03:31:48Z");
        return Stream.of(arguments(until, FiredAtArgument.of(Instant.parse("2023-09-22T00:00:48Z"), false)),
                         arguments(until, FiredAtArgument.of(Instant.parse("2023-09-24T00:00:48Z"), false)),
                         arguments(until, FiredAtArgument.of(Instant.parse("2023-09-24T03:31:48Z"), false)),
                         arguments(until,
                                   FiredAtArgument.of(Duration.ofSeconds(3), Instant.parse("2023-09-24T03:31:50Z"),
                                                      false)),
                         arguments(until, FiredAtArgument.of(Instant.parse("2023-09-24T04:31:48Z"), true)),
                         arguments(until, FiredAtArgument.of(Instant.parse("2023-09-25T03:31:48Z"), true)),
                         arguments(until,
                                   FiredAtArgument.of(Duration.ofSeconds(3), Instant.parse("2023-09-24T03:31:52Z"),
                                                      true)));
    }

    @ParameterizedTest
    @MethodSource("untilTestData")
    void test_until(Instant until, FiredAtArgument arg) {
        Assertions.assertEquals(arg.expected, TriggerRule.create(until, arg.leeway).isExceeded(arg.firedAt));
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
        final String data =
            "{\"timeframes\":[{\"from\":\"2023-09-24\",\"to\":\"2023-09-26\",\"type\":\"java.time.LocalDate\"}," +
            "{\"from\":\"09:39:33.514\",\"to\":\"11:39:33.514\",\"type\":\"java.time.LocalTime\"}]," +
            "\"until\":\"2023-09-24T03:31:48Z\",\"leeway\":" + leeway + "}";
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
    void test_TimeFrames_statisfy(ZoneId zoneId, List<Timeframe> timeframes, FiredAtArgument arg) {
        final TimeZone systemTimeZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
            Assertions.assertEquals(arg.expected, TriggerRule.create(timeframes, arg.leeway).satisfy(arg.firedAt));
        } finally {
            TimeZone.setDefault(systemTimeZone);
        }
    }

}
