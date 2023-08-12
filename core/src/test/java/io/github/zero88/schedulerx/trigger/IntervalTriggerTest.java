package io.github.zero88.schedulerx.trigger;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

class IntervalTriggerTest {

    @Test
    void test_compare() throws JsonProcessingException {
        final IntervalTrigger trigger1 = IntervalTrigger.builder()
                                                        .initialDelay(30)
                                                        .interval(5)
                                                        .intervalTimeUnit(TimeUnit.SECONDS)
                                                        .repeat(10)
                                                        .build();
        final String data = "{\"initialDelayTimeUnit\":\"SECONDS\",\"initialDelay\":30,\"repeat\":10," +
                            "\"intervalTimeUnit\":\"SECONDS\",\"interval\":5}";
        final IntervalTrigger trigger2 = new ObjectMapper().readValue(data, IntervalTrigger.class);
        Assertions.assertEquals(trigger2, trigger1);
    }

    @Test
    void test_serialize() throws JsonProcessingException {
        final IntervalTrigger trigger = IntervalTrigger.builder()
                                                       .initialDelay(1)
                                                       .interval(10)
                                                       .intervalTimeUnit(TimeUnit.DAYS)
                                                       .repeat(3)
                                                       .build();
        Assertions.assertFalse(trigger.noDelay());
        Assertions.assertTrue(trigger.noRepeatIndefinitely());
        final String json = new ObjectMapper().writeValueAsString(trigger);
        Assertions.assertEquals("{\"repeat\":3,\"initialDelay\":1,\"initialDelayTimeUnit\":\"SECONDS\"," +
                                "\"interval\":10,\"intervalTimeUnit\":\"DAYS\"}", json);
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
        Assertions.assertEquals(-1, trigger.getRepeat());
        Assertions.assertEquals("interval", trigger.type());
        Assertions.assertFalse(trigger.noRepeatIndefinitely());
        Assertions.assertTrue(trigger.noDelay());
    }

    @ParameterizedTest
    @CsvSource(value = {
        "{\"repeat\":0}|Invalid repeat value", "{\"interval\":10,\"initialDelay\":-1}|Invalid initial delay value",
        "{\"interval\":-1}|Invalid interval value", "{\"initialDelayTimeUnit\":\"SECONDS\"}|Invalid interval value",
    }, delimiter = '|')
    void test_deserialize_invalid_value(String input, String expected) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final IntervalTrigger intervalTrigger = objectMapper.readValue(input, IntervalTrigger.class);
        Throwable cause = Assertions.assertThrows(IllegalArgumentException.class, intervalTrigger::validate);
        Assertions.assertEquals(expected, cause.getMessage());
    }

    @ParameterizedTest
    @CsvSource({ "{\"initialDelayTimeUnit\":\"SECONDS1\"}" })
    void test_deserialize_invalid_format(String input) {
        final ObjectMapper objectMapper = new ObjectMapper();
        Assertions.assertThrows(InvalidFormatException.class,
                                () -> objectMapper.readValue(input, IntervalTrigger.class));
    }

    @Test
    void test_preview_trigger() {
        final OffsetDateTime startedAt = OffsetDateTime.parse("2023-07-30T18:01+07:00");
        final List<OffsetDateTime> expected = Arrays.asList(OffsetDateTime.parse("2023-07-30T18:11:10+07:00"),
                                                            OffsetDateTime.parse("2023-07-30T18:21:10+07:00"),
                                                            OffsetDateTime.parse("2023-07-30T18:31:10+07:00"));

        final IntervalTrigger trigger = IntervalTrigger.builder()
                                                       .initialDelay(10)
                                                       .interval(10)
                                                       .intervalTimeUnit(TimeUnit.MINUTES)
                                                       .repeat(3)
                                                       .build();
        final PreviewParameter parameter = PreviewParameter.byDefault()
                                                           .setStartedAt(startedAt.toInstant())
                                                           .setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        final List<OffsetDateTime> result = trigger.preview(parameter);
        Assertions.assertEquals(3, result.size());
        Assertions.assertIterableEquals(expected, result);
    }

}
