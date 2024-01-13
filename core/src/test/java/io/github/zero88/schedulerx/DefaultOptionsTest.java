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
        Assertions.assertEquals(30, DefaultOptions.getInstance().triggerPreviewMaxCount);
        Assertions.assertEquals(Duration.ofSeconds(10), DefaultOptions.getInstance().triggerRuleMaxLeeway);

        Assertions.assertEquals(Duration.ofSeconds(2), DefaultOptions.getInstance().evaluationMaxTimeout);
        Assertions.assertEquals(Duration.ofSeconds(60), DefaultOptions.getInstance().executionMaxTimeout);
        Assertions.assertEquals("scheduler.x-worker-thread", DefaultOptions.getInstance().executionThreadPrefix);
        Assertions.assertEquals(5, DefaultOptions.getInstance().executionThreadPoolSize);

        Assertions.assertEquals(Duration.ofSeconds(2), DefaultOptions.getInstance().monitorMaxTimeout);
        Assertions.assertEquals("scheduler.x-monitor-thread", DefaultOptions.getInstance().monitorThreadPrefix);
        Assertions.assertEquals(3, DefaultOptions.getInstance().monitorThreadPoolSize);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_evaluation_max_timeout", value = "PT1S")
    void test_override_max_evaluation_timeout() {
        Assertions.assertEquals(Duration.ofSeconds(1), new DefaultOptions().evaluationMaxTimeout);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_execution_max_timeout", value = "PT10M")
    void test_override_max_execution_timeout() {
        Assertions.assertEquals(Duration.ofMinutes(10), new DefaultOptions().executionMaxTimeout);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_trigger_preview_max_count", value = "12")
    void test_override_max_preview_count() {
        Assertions.assertEquals(12, new DefaultOptions().triggerPreviewMaxCount);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_trigger_rule_max_leeway", value = "PT3S")
    void test_override_max_trigger_rule_leeway() {
        Assertions.assertEquals(Duration.ofSeconds(3), new DefaultOptions().triggerRuleMaxLeeway);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_execution_thread_prefix", value = "hello-there")
    void test_override_execution_thread_prefix() {
        Assertions.assertEquals("hello-there", new DefaultOptions().executionThreadPrefix);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_execution_thread_pool_size", value = "10")
    void test_override_execution_thread_pool_size() {
        Assertions.assertEquals(10, new DefaultOptions().executionThreadPoolSize);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_monitor_thread_prefix", value = "monitor-there")
    void test_override_monitor_thread_prefix() {
        Assertions.assertEquals("monitor-there", new DefaultOptions().monitorThreadPrefix);
    }

    @Test
    @SetSystemProperty(key = "schedulerx.default_monitor_thread_pool_size", value = "5")
    void test_override_monitor_thread_pool_size() {
        Assertions.assertEquals(5, new DefaultOptions().monitorThreadPoolSize);
    }

}
