package io.github.zero88.schedulerx.trigger.predicate;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.trigger.predicate.TestPredicateHolder.FailedEventTriggerExtensionPredicate;
import io.github.zero88.schedulerx.trigger.predicate.TestPredicateHolder.MockEventTriggerExtensionPredicate;
import io.github.zero88.schedulerx.trigger.predicate.TestPredicateHolder.MockExtraConverter;
import io.github.zero88.schedulerx.trigger.predicate.TestPredicateHolder.MockExtraFilter;
import io.github.zero88.schedulerx.trigger.predicate.TestPredicateHolder.MockForbiddenFilter;
import io.github.zero88.schedulerx.trigger.predicate.TestPredicateHolder.MockNullConverter;
import io.github.zero88.schedulerx.trigger.predicate.TestPredicateHolder.MockUnableDeserializeEventTrigger;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("rawtypes")
class EventTriggerPredicateTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = DatabindCodec.mapper();
    }

    static Stream<Arguments> validData() {
        // @formatter:off
        return Stream.of(arguments(EventTriggerPredicate.any(),
                                   JsonObject.of("predicate", AnyEventTriggerPredicate.class.getName())),
                         arguments(EventTriggerPredicate.ignoreType(new MockForbiddenFilter()),
                                   JsonObject.of("msgConverter", AllowAnyMessageBodyType.class.getName(), "msgFilter",
                                                 MockForbiddenFilter.class.getName())),
                         arguments(EventTriggerPredicate.create(new MockForbiddenFilter()),
                                   JsonObject.of("msgConverter", AutoCastMessageBody.class.getName(), "msgFilter",
                                                 MockForbiddenFilter.class.getName())),
                         arguments(EventTriggerPredicate.create(new MockNullConverter(), new MockForbiddenFilter()),
                                   JsonObject.of("msgConverter", MockNullConverter.class.getName(), "msgFilter",
                                                 MockForbiddenFilter.class.getName())),
                         arguments(EventTriggerPredicate.create(new MockExtraConverter().load(Collections.singletonMap("k1", "v1")),
                                                                new MockExtraFilter().load(Collections.singletonMap("k2", "v2"))),
                                   JsonObject.of("msgConverter", MockExtraConverter.class.getName(),
                                                 "msgFilter", MockExtraFilter.class.getName(),
                                                 "msgConverterExtra", JsonObject.of("k1", "v1"),
                                                 "msgFilterExtra", JsonObject.of("k2", "v2"))));
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("validData")
    void test_able_to_serialize_deserialize_builtin(EventTriggerPredicate predicate, JsonObject expected)
        throws JsonProcessingException {
        String json = expected.encode();
        Assertions.assertEquals(json, mapper.writeValueAsString(predicate));
        EventTriggerPredicate deserialized = mapper.readerFor(EventTriggerPredicate.class).readValue(json);
        Assertions.assertEquals(deserialized, predicate);
    }

    static Stream<Arguments> notAbleToDeserialize() {
        // @formatter:off
        return Stream.of(arguments(JsonObject.of(), "'msgConverter' is required"),
                         arguments(JsonObject.of("msgConverter", null), "'msgConverter' is required"),
                         arguments(JsonObject.of("msgConverter", MockNullConverter.class.getName(), "msgFilter", "  "), "'msgFilter' is required"),
                         arguments(JsonObject.of("predicate", null, "msgConverter", MockNullConverter.class.getName(), "msgFilter", MockForbiddenFilter.class.getName()),
                                   "'predicate' is required"),
                         arguments(JsonObject.of("msgConverter", "anyConverter"),
                                   "Invalid an argument class definition[anyConverter], expecting a subclass of [io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate$MessageConverter]"),
                         arguments(JsonObject.of("msgConverter", MockNullConverter.class.getName(), "msgFilter", "anyFilter"),
                                   "Invalid an argument class definition[anyFilter], expecting a subclass of [io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate$MessageFilter]"),
                         arguments(JsonObject.of("predicate", MockUnableDeserializeEventTrigger.class.getName(), "msgConverter", MockNullConverter.class.getName(), "msgFilter", MockForbiddenFilter.class.getName()),
                                   "Invalid an argument class definition[io.github.zero88.schedulerx.trigger.predicate.TestPredicateHolder$MockUnableDeserializeEventTrigger], " +
                                   "expecting a subclass of [io.github.zero88.schedulerx.trigger.predicate.EventTriggerExtensionPredicate]"),
                         arguments(JsonObject.of("predicate", FailedEventTriggerExtensionPredicate.class.getName(), "msgConverter", MockNullConverter.class.getName(), "msgFilter", MockForbiddenFilter.class.getName()),
                                   "Failed to setup class io.github.zero88.schedulerx.trigger.predicate.TestPredicateHolder$FailedEventTriggerExtensionPredicate"));
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("notAbleToDeserialize")
    void test_not_able_to_deserialize(JsonObject json, String cause) {
        JsonMappingException ex = Assertions.assertThrows(JsonMappingException.class,
                                                          () -> mapper.readerFor(EventTriggerPredicate.class)
                                                                      .readValue(json.encode()));
        Assertions.assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        Assertions.assertEquals(cause, ex.getCause().getMessage());
    }

    @Test
    void test_able_to_deserialize_extension_predicate() throws JsonProcessingException {
        JsonObject input = JsonObject.of("predicate", MockEventTriggerExtensionPredicate.class.getName(),
                                         "predicateExtra", JsonObject.of("k", "v"), "msgConverter",
                                         MockNullConverter.class.getName(), "msgFilter",
                                         MockForbiddenFilter.class.getName());
        Object deserialized = mapper.readerFor(EventTriggerPredicate.class).readValue(input.encode());
        Assertions.assertInstanceOf(MockEventTriggerExtensionPredicate.class, deserialized);
        JsonObject extra = ((MockEventTriggerExtensionPredicate) deserialized).extra();
        Assertions.assertInstanceOf(MockNullConverter.class, extra.getValue("msgConverter"));
        Assertions.assertInstanceOf(MockForbiddenFilter.class, extra.getValue("msgFilter"));
        Assertions.assertEquals(JsonObject.of("k", "v"), extra.getValue("predicateExtra"));
    }

    static Stream<EventTriggerPredicate> unsupportedSerialize() {
        return Stream.of(EventTriggerPredicate.create(o -> false),
                         EventTriggerPredicate.create(new MockNullConverter(), o -> false));
    }

    @ParameterizedTest
    @MethodSource("unsupportedSerialize")
    void test_anonymous(EventTriggerPredicate predicate) {
        UnsupportedOperationException ex = Assertions.assertThrows(UnsupportedOperationException.class,
                                                                   predicate::toJson);
        Assertions.assertEquals("Unable to serialize anonymous class or lambda", ex.getMessage());
    }

}
