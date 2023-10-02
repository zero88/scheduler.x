package io.github.zero88.schedulerx.trigger.rule;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.trigger.rule.custom.SimpleDateTimeTimeframe;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

class TimeframeTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper().findAndRegisterModules()
                                   .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                                   .setSerializationInclusion(Include.NON_NULL);
    }

    private static Stream<Arguments> validValues() {
        // @formatter:off
        return Stream.of(arguments(Timeframe.of(LocalTime.of(2, 30), null),
                                   "{\"from\":\"02:30:00\",\"type\":\"java.time.LocalTime\"}"),
                         arguments(Timeframe.of(LocalTime.of(2, 30), LocalTime.of(4, 30)),
                                   "{\"from\":\"02:30:00\",\"to\":\"04:30:00\",\"type\":\"java.time.LocalTime\"}"),
                         arguments(Timeframe.of(LocalDate.of(2023, 9, 20), LocalDate.of(2023, 9, 22)),
                                   "{\"from\":\"2023-09-20\",\"to\":\"2023-09-22\",\"type\":\"java.time.LocalDate\"}"),
                         arguments(Timeframe.of(LocalDateTime.parse("2023-09-20T02:30:00"), LocalDateTime.parse("2023-09-22T03:30:50")),
                                   "{\"from\":\"2023-09-20T02:30:00\",\"to\":\"2023-09-22T03:30:50\",\"type\":\"java.time.LocalDateTime\"}"),
                         arguments(Timeframe.of(OffsetTime.parse("23:09:20+07:00"), OffsetTime.parse("23:55:20+07:00")),
                                   "{\"from\":\"23:09:20+07:00\",\"to\":\"23:55:20+07:00\",\"type\":\"java.time.OffsetTime\"}"),
                         arguments(Timeframe.of(OffsetDateTime.parse("2023-09-20T05:07:01+03:00"), OffsetDateTime.parse("2023-09-20T06:07:01+03:00")),
                                   "{\"from\":\"2023-09-20T05:07:01+03:00\",\"to\":\"2023-09-20T06:07:01+03:00\",\"type\":\"java.time.OffsetDateTime\"}"),
                         arguments(Timeframe.of(Instant.parse("2023-09-24T03:31:48Z"), Instant.parse("2023-09-26T06:31:48Z")),
                                   "{\"from\":\"2023-09-24T03:31:48Z\",\"to\":\"2023-09-26T06:31:48Z\",\"type\":\"java.time.Instant\"}"));
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("validValues")
    void serialize(Timeframe<?> timeframe, String expected) throws JsonProcessingException {
        Assertions.assertTrue(mapper.canSerialize(timeframe.getClass()));
        Assertions.assertEquals(expected, mapper.writeValueAsString(timeframe));
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> timeframe.set("from", "failed"));
    }

    @ParameterizedTest
    @MethodSource("validValues")
    void deserialize(Timeframe<?> expected, String input) throws JsonProcessingException {
        final Timeframe<?> o1 = mapper.readerFor(Timeframe.class).readValue(input);
        final Timeframe<?> o2 = mapper.readValue(input, Timeframe.class);
        Assertions.assertEquals(o1, o2);
        Assertions.assertEquals(expected, o1);
    }

    private static Stream<Arguments> invalidValues() {
        // @formatter:off
        return Stream.of(
            arguments(UnsupportedOperationException.class, "Unrecognized a timeframe with type[java.lang.String]",
                      "null", "null", null),
            arguments(UnsupportedOperationException.class, "Unrecognized a timeframe with type[x.y.z]",
                      "null", "null", "x.y.z"),
            arguments(IllegalArgumentException.class, "Unable to parse a timeframe. Cause: Timeframe type is required",
                      null, null, null),
            arguments(IllegalArgumentException.class, "Required at least one non-null value on either minimum or maximum value",
                      null, null, "java.time.LocalTime"),
            arguments(IllegalArgumentException.class, "Unable to parse a timeframe. Cause: Text 'xy' could not be parsed at index 0",
                      "xy", "ab", "java.time.LocalTime"),
            arguments(IllegalArgumentException.class, "Unable to parse a timeframe. Cause: Unsupported input type[java.time.LocalTime]",
                      LocalDate.of(2023, 9, 20), LocalTime.of(2, 30), null),
            arguments(IllegalArgumentException.class, "Unable to parse a timeframe. Cause: Unsupported input type[java.lang.Integer]",
                      12, 24, "java.time.LocalTime"),
            arguments(IllegalArgumentException.class, "'From' value must be before 'To' value",
                      LocalDate.of(2023, 9, 20), LocalDate.of(2023, 9, 18), null),
            arguments(IllegalArgumentException.class, "'From' value must be before 'To' value",
                      LocalDateTime.parse("2023-09-20T04:30:00"), LocalDateTime.parse("2023-09-20T03:30:50"), null),
            arguments(IllegalArgumentException.class, "'From' value must be before 'To' value",
                      OffsetDateTime.parse("2023-09-20T05:07:01+03:00"), OffsetDateTime.parse("2023-09-20T05:07:01+03:00"), null),
            arguments(IllegalArgumentException.class, "'From' value must be before 'To' value",
                      Instant.parse("2023-09-24T03:31:48Z"), Instant.parse("2023-09-22T03:31:48Z"), null));
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void expect_throws_if_invalid_value(Class<Throwable> exCls, String errorMsg, Object from, Object to, String type) {
        Executable executable = type == null ? () -> Timeframe.of(from, to) : () -> Timeframe.create(type, from, to);
        Throwable throwable = Assertions.assertThrows(exCls, executable);
        Assertions.assertEquals(errorMsg, throwable.getMessage());
    }

    @Test
    void serialize_deserialize_custom_Timeframe() throws JsonProcessingException {
        final String expected = "{\"from\":\"Fri, Sep 1, 2023 05:00:00\",\"to\":\"Sun, Oct 1, 2023 05:00:00\"," +
                                "\"timezone\":\"Europe/Paris\",\"type\":\"java.util.Date\"}";
        final Timeframe<?> timeframe = mapper.readValue(expected, Timeframe.class);
        Assertions.assertInstanceOf(SimpleDateTimeTimeframe.class, timeframe);
        Assertions.assertEquals(expected, mapper.writeValueAsString(timeframe));
    }

}
