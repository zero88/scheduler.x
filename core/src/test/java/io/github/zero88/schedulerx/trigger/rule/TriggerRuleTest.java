package io.github.zero88.schedulerx.trigger.rule;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
        return Stream.of(
            arguments(Instant.parse("2023-09-24T03:31:48Z"), null, Instant.parse("2023-09-22T00:00:48Z"), false),
            arguments(Instant.parse("2023-09-24T03:31:48Z"), null, Instant.parse("2023-09-24T00:00:48Z"), false),
            arguments(Instant.parse("2023-09-24T03:31:48Z"), null, Instant.parse("2023-09-24T03:31:48Z"), false),
            arguments(Instant.parse("2023-09-24T03:31:48Z"), Duration.ofSeconds(3), Instant.parse("2023-09-24T03:31:50Z"), false),
            arguments(Instant.parse("2023-09-24T03:31:48Z"), null, Instant.parse("2023-09-24T04:31:48Z"), true),
            arguments(Instant.parse("2023-09-24T03:31:48Z"), null, Instant.parse("2023-09-25T03:31:48Z"), true),
            arguments(Instant.parse("2023-09-24T03:31:48Z"), Duration.ofSeconds(3), Instant.parse("2023-09-24T03:31:52Z"), true));
    }

    @ParameterizedTest
    @MethodSource("untilTestData")
    void test_until(Instant until, Duration leeway, Instant firedAt, boolean isExceeded) {
        Assertions.assertEquals(isExceeded, TriggerRule.create(null, until, leeway).isExceeded(firedAt));
    }

    private static Stream<Arguments> leewayTestData() {
        return Stream.of(arguments(0.0, Duration.ZERO), arguments(0, Duration.ZERO), arguments("-PT1H", Duration.ZERO),
                         arguments("PT10S", Duration.ofSeconds(10)), arguments("PT1M", Duration.ofSeconds(30)));
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

    private static Stream<Arguments> testdata_OffsetTimeFrame() {
        return Stream.of(arguments(Instant.parse("2023-09-22T04:00:48Z"), false),
                         arguments(Instant.parse("2023-09-22T09:39:33Z"), false),
                         arguments(Instant.parse("2023-09-22T10:00:48Z"), true),
                         arguments(Instant.parse("2023-09-22T11:39:33Z"), false),
                         arguments(Instant.parse("2023-09-22T22:00:48Z"), false),
                         arguments(Instant.parse("2023-09-22T23:00:48Z"), true),
                         arguments(Instant.parse("2023-09-22T00:00:00Z"), true),
                         arguments(Instant.parse("2023-09-22T01:00:00Z"), true));
    }

    @ParameterizedTest
    @MethodSource("testdata_OffsetTimeFrame")
    void test_OffsetTimeFrame_statisfy(Instant firedAt, boolean isSatisfied) {
        final List<Timeframe> timeframes = Arrays.asList(
            Timeframe.of(OffsetTime.of(LocalTime.parse("09:39:33"), ZoneOffset.UTC),
                         OffsetTime.of(LocalTime.parse("11:39:33"), ZoneOffset.UTC)),
            Timeframe.of(OffsetTime.of(LocalTime.parse("22:39:33"), ZoneOffset.UTC),
                         OffsetTime.of(LocalTime.parse("02:39:33"), ZoneOffset.UTC)));
        Assertions.assertEquals(isSatisfied, TriggerRule.create(timeframes).satisfy(firedAt));
    }

}
