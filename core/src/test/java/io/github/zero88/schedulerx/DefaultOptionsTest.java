package io.github.zero88.schedulerx;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junitpioneer.jupiter.SetSystemProperty;

@Execution(ExecutionMode.SAME_THREAD)
class DefaultOptionsTest {

    @Test
    void test_default_options() {
        Assertions.assertEquals(DefaultOptions.getInstance().maxEvaluationTimeout, Duration.ofSeconds(2));
        Assertions.assertEquals(DefaultOptions.getInstance().maxExecutionTimeout, Duration.ofSeconds(60));
        Assertions.assertEquals(DefaultOptions.getInstance().maxTriggerPreviewCount, 30);
        Assertions.assertEquals(DefaultOptions.getInstance().maxTriggerRuleLeeway, Duration.ofSeconds(10));
    }


    @Test
    @SetSystemProperty(key = "schedulerx.default_max_evaluation_timeout", value = "PT1S")
    void test_override_max_evaluation_timeout() {
        Assertions.assertEquals(new DefaultOptions().maxEvaluationTimeout, Duration.ofSeconds(1));
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_max_execution_timeout", value = "PT10M")
    void test_override_max_execution_timeout() {
        Assertions.assertEquals(new DefaultOptions().maxExecutionTimeout, Duration.ofMinutes(10));
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_max_trigger_preview_count", value = "12")
    void test_override_max_preview_count() {
        Assertions.assertEquals(new DefaultOptions().maxTriggerPreviewCount, 12);
    }


    @Test
    @SetSystemProperty(key = "schedulerx.default_max_trigger_rule_leeway", value = "PT3S")
    void test_override_max_trigger_rule_leeway() {
        Assertions.assertEquals(new DefaultOptions().maxTriggerRuleLeeway, Duration.ofSeconds(3));
    }

}
