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
        Assertions.assertEquals(Duration.ofSeconds(2), DefaultOptions.getInstance().maxEvaluationTimeout);
        Assertions.assertEquals(Duration.ofSeconds(60), DefaultOptions.getInstance().maxExecutionTimeout);
        Assertions.assertEquals(30, DefaultOptions.getInstance().maxTriggerPreviewCount);
        Assertions.assertEquals(Duration.ofSeconds(10), DefaultOptions.getInstance().maxTriggerRuleLeeway);
        Assertions.assertEquals("scheduler.x-worker-thread", DefaultOptions.getInstance().workerThreadPrefix);
        Assertions.assertEquals(3, DefaultOptions.getInstance().workerThreadPoolSize);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_max_evaluation_timeout", value = "PT1S")
    void test_override_max_evaluation_timeout() {
        Assertions.assertEquals(Duration.ofSeconds(1), new DefaultOptions().maxEvaluationTimeout);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_max_execution_timeout", value = "PT10M")
    void test_override_max_execution_timeout() {
        Assertions.assertEquals(Duration.ofMinutes(10), new DefaultOptions().maxExecutionTimeout);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_max_trigger_preview_count", value = "12")
    void test_override_max_preview_count() {
        Assertions.assertEquals(12, new DefaultOptions().maxTriggerPreviewCount);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_max_trigger_rule_leeway", value = "PT3S")
    void test_override_max_trigger_rule_leeway() {
        Assertions.assertEquals(Duration.ofSeconds(3), new DefaultOptions().maxTriggerRuleLeeway);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_worker_thread_prefix", value = "hello-there")
    void test_override_worker_thread_prefix() {
        Assertions.assertEquals("hello-there", new DefaultOptions().workerThreadPrefix);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_worker_thread_pool_size", value = "10")
    void test_override_worker_thread_pool_size() {
        Assertions.assertEquals(10, new DefaultOptions().workerThreadPoolSize);
    }

}
