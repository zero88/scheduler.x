package io.github.zero88.schedulerx.trigger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate;
import io.vertx.core.json.jackson.DatabindCodec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class EventTriggerTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = DatabindCodec.mapper();
    }

    @Test
    void test_serialize_deserialize() throws JsonProcessingException {
        final EventTrigger<Object> trigger = EventTrigger.builder()
                                                         .address("should.able.to.serialize")
                                                         .localOnly(true)
                                                         .predicate(EventTriggerPredicate.any())
                                                         .build();
        final String json = mapper.writeValueAsString(trigger);
        Assertions.assertEquals("{\"address\":\"should.able.to.serialize\",\"localOnly\":true," +
                                "\"eventTriggerPredicate\":{\"predicate\":\"io.github.zero88.schedulerx.trigger" +
                                ".predicate.AnyEventTriggerPredicate\"}}", json);
        final EventTrigger<Object> deserialized = mapper.readerFor(EventTrigger.class).readValue(json);
        Assertions.assertEquals(trigger, deserialized);
    }

}
