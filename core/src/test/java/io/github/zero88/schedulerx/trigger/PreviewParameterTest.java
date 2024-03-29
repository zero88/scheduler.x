package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class PreviewParameterTest {

    @Test
    void test_byDefault() {
        final PreviewParameter parameter = PreviewParameter.byDefault();
        Assertions.assertEquals(10, parameter.getTimes());
        Assertions.assertNull(parameter.getStartedAt());
        Assertions.assertNull(parameter.getTimeZone());
        Assertions.assertNull(parameter.getRule());
    }

    @Test
    void test_byConstructor() {
        final PreviewParameter parameter = new PreviewParameter();
        Assertions.assertEquals(1, parameter.getTimes());
        Assertions.assertNull(parameter.getStartedAt());
        Assertions.assertNull(parameter.getTimeZone());
        Assertions.assertNull(parameter.getRule());
    }

    @ParameterizedTest
    @CsvSource({ "31,30", "-1,1", "0,1", "10,10", "15,15" })
    void test_min_max_times(int input, int expected) {
        Assertions.assertEquals(expected, PreviewParameter.byDefault().setTimes(input).getTimes());
        Assertions.assertEquals(expected, new PreviewParameter().setTimes(input).getTimes());
    }

    @ParameterizedTest
    @MethodSource("provide_timezone")
    void test_provide_timezone(Object input) {
        final Instant now = Instant.now();
        PreviewParameter p1;
        if (input instanceof ZoneId) {
            p1 = PreviewParameter.byDefault().setTimeZone((ZoneId) input);
        } else {
            p1 = PreviewParameter.byDefault().setTimeZone((TimeZone) input);
        }
        Assertions.assertNotNull(p1.getTimeZone());
        Assertions.assertEquals("+07:00", p1.getTimeZone().getRules().getOffset(now).getId());
    }

    private static Stream<Object> provide_timezone() {
        return Stream.of(ZoneId.of("UTC+7"), ZoneOffset.ofHours(7), TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

}
