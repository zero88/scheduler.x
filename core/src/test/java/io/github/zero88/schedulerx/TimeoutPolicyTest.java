package io.github.zero88.schedulerx;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class TimeoutPolicyTest {

    @Test
    void test_default() {
        final TimeoutPolicy timeoutPolicy = TimeoutPolicy.byDefault();
        Assertions.assertEquals(Duration.ofSeconds(2), timeoutPolicy.evaluationTimeout());
        Assertions.assertEquals(Duration.ofSeconds(60), timeoutPolicy.executionTimeout());
    }

    @Test
    void test_serialize_deserialize() throws JsonProcessingException {
        ObjectMapper mapper = TestUtils.defaultMapper();
        final String expected = "{\"evaluationTimeout\":\"PT1S\",\"executionTimeout\":\"PT30S\"}";
        final TimeoutPolicy timeoutPolicy = TimeoutPolicy.create(Duration.ofSeconds(1), Duration.ofSeconds(30));
        Assertions.assertEquals(expected, mapper.writeValueAsString(timeoutPolicy));
        final TimeoutPolicy deserialized = mapper.readerFor(TimeoutPolicy.class).readValue(expected);
        Assertions.assertEquals(timeoutPolicy.evaluationTimeout(), deserialized.evaluationTimeout());
        Assertions.assertEquals(timeoutPolicy.executionTimeout(), deserialized.executionTimeout());
    }

    @Test
    void test_abnormal_value() {
        final TimeoutPolicy t1 = TimeoutPolicy.create(Duration.ZERO, Duration.ofSeconds(100));
        Assertions.assertEquals(Duration.ofSeconds(2), t1.evaluationTimeout());
        Assertions.assertEquals(Duration.ofSeconds(60), t1.executionTimeout());
    }
}
