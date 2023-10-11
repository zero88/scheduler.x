package io.github.zero88.schedulerx.trigger;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.LocalTime;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate;
import io.github.zero88.schedulerx.trigger.rule.Timeframe;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SuppressWarnings("rawtypes")
class EventTriggerTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = DatabindCodec.mapper()
                              .findAndRegisterModules()
                              .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    static Stream<Arguments> validData() {
        final TriggerRule rule = TriggerRule.create(Collections.singletonList(Timeframe.of(LocalTime.of(2, 30), null)));
        // @formatter:off
        return Stream.of(
            arguments(EventTrigger.builder().address("s1.a2.b3").predicate(EventTriggerPredicate.any()).build(),
                      new JsonObject("{\"address\":\"s1.a2.b3\",\"eventTriggerPredicate\":{\"predicate\":\"io.github.zero88.schedulerx.trigger.predicate.AnyEventTriggerPredicate\"}}")),
            arguments(EventTrigger.builder().address("s1.a2.b3").localOnly(true).predicate(EventTriggerPredicate.any()).rule(rule).build(),
                      new JsonObject("{\"type\":\"event\",\"rule\":{\"timeframes\":[{\"from\":\"02:30:00\",\"type\":\"java.time.LocalTime\"}]}," +
                                     "\"address\":\"s1.a2.b3\",\"localOnly\":true," +
                                     "\"eventTriggerPredicate\":{\"predicate\":\"io.github.zero88.schedulerx.trigger.predicate.AnyEventTriggerPredicate\"}}")));
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("validData")
    void test_serialize_deserialize(EventTrigger trigger, JsonObject json) throws JsonProcessingException {
        final EventTrigger t3 = mapper.readValue(json.encode(), EventTrigger.class);
        Assertions.assertEquals(t3, trigger);
        Assertions.assertEquals(t3.toJson(), trigger.toJson());
        Assertions.assertEquals(t3.toJson().encode(), mapper.writeValueAsString(trigger));
    }

    static Stream<Arguments> invalidData() {
        return Stream.of(
            arguments(JsonObject.of("type", "event"), NullPointerException.class, "The event address is required"),
            arguments(JsonObject.of("address", null), NullPointerException.class, "The event address is required"),
            arguments(JsonObject.of("address", ""), NullPointerException.class, "The event address is required"),
            arguments(JsonObject.of("address", "  "), NullPointerException.class, "The event address is required"),
            arguments(JsonObject.of("address", "1.2.3"), NullPointerException.class, "The event trigger is required"));
    }

    @ParameterizedTest
    @MethodSource("invalidData")
    void test_deserialize_invalid_data(JsonObject json, Class<Exception> exClass, String errMsg) {
        Throwable throwable = Assertions.assertThrows(JsonProcessingException.class,
                                                      () -> mapper.readValue(json.encode(), EventTrigger.class));
        Assertions.assertInstanceOf(exClass, throwable.getCause());
        Assertions.assertEquals(errMsg, throwable.getCause().getMessage());
    }

}
