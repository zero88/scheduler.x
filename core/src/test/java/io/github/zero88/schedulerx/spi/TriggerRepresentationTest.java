package io.github.zero88.schedulerx.spi;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.github.zero88.schedulerx.trigger.EventTrigger;
import io.github.zero88.schedulerx.trigger.EventTriggerPredicate;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.github.zero88.schedulerx.trigger.Trigger;

class TriggerRepresentationTest {

    @ParameterizedTest
    @MethodSource("provide_trigger")
    void test_trigger_representation(Trigger trigger, String expectedRepresentation) {
        Assertions.assertEquals(trigger.toString(), trigger.display());
        Assertions.assertEquals(trigger.display(), trigger.display("en"));
        Assertions.assertEquals(expectedRepresentation, trigger.display("en"));
    }

    private static Stream<Arguments> provide_trigger() {
        return Stream.of(arguments(IntervalTrigger.builder().interval(5).build(),
                                   "IntervalTrigger(initialDelay=0, initialDelayTimeUnit=SECONDS, interval=5, " +
                                   "intervalTimeUnit=SECONDS, repeat=-1)"),
                         arguments(EventTrigger.builder().address("a.b").predicate(EventTriggerPredicate.any()).build(),
                                   "EventTrigger(address='a.b', localOnly=false, predicate='Accept any event')"));
    }

    @Test
    void test_cron_trigger_with_custom_representation() {
        final CronTrigger trigger = CronTrigger.builder().expression("*/10 * * * *").build();
        Assertions.assertTrue(trigger.display().contains(trigger.toString()));
        Assertions.assertEquals(trigger.display(), trigger.display("en"));
        Assertions.assertEquals(trigger.display(), trigger.display("any"));
        Assertions.assertEquals("This is a custom representation: " + trigger, trigger.display());
        Assertions.assertEquals("Esta es una representación personalizada: " + trigger, trigger.display("es"));
    }

}
