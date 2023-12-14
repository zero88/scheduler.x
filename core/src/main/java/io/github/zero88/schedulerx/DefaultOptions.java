package io.github.zero88.schedulerx;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import io.github.zero88.schedulerx.impl.Utils;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.VertxOptions;

/**
 * Instances of this class are used to configure the default options for {@link Trigger}, and {@link Scheduler}
 * instances.
 *
 * @since 2.0.0
 */
public final class DefaultOptions {

    public static final String DEFAULT_MAX_EXECUTION_TIMEOUT = "schedulerx.default_max_execution_timeout";
    public static final String DEFAULT_MAX_EVALUATION_TIMEOUT = "schedulerx.default_max_evaluation_timeout";
    public static final String DEFAULT_MAX_TRIGGER_RULE_LEEWAY = "schedulerx.default_max_trigger_rule_leeway";
    public static final String DEFAULT_MAX_TRIGGER_PREVIEW_COUNT = "schedulerx.default_max_trigger_preview_count";
    public static final String DEFAULT_WORKER_THREAD_PREFIX = "schedulerx.default_worker_thread_prefix";
    public static final String DEFAULT_WORKER_THREAD_POOL_SIZE = "schedulerx.default_worker_thread_pool_size";


    private static class Holder {

        private static final DefaultOptions INSTANCE = new DefaultOptions();

    }

    public static DefaultOptions getInstance() {
        return DefaultOptions.Holder.INSTANCE;
    }

    /**
     * Declares the default max execution timeout. Defaults is {@link VertxOptions#DEFAULT_MAX_WORKER_EXECUTE_TIME}
     *
     * @apiNote It can be overridden by system property with key {@link #DEFAULT_MAX_EXECUTION_TIMEOUT}
     */
    public final Duration maxExecutionTimeout;
    /**
     * Declares the default max trigger evaluation timeout. Defaults is
     * {@link VertxOptions#DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME}
     *
     * @apiNote It can be overridden by system property with key {@link #DEFAULT_MAX_EVALUATION_TIMEOUT}
     */
    public final Duration maxEvaluationTimeout;
    /**
     * Declares the default max trigger rule leeway time. Defaults is {@code 10 seconds}.
     *
     * @apiNote It can be overridden by system property with key {@link #DEFAULT_MAX_TRIGGER_RULE_LEEWAY}
     */
    public final Duration maxTriggerRuleLeeway;
    /**
     * Declares the default max number of the trigger preview items. Defaults is {@code 30}.
     *
     * @apiNote It can be overridden by system property with key {@link #DEFAULT_MAX_TRIGGER_PREVIEW_COUNT}
     */
    public final int maxTriggerPreviewCount;

    /**
     * Declares the default worker thread name prefix. Defaults is {@code scheduler.x-worker-thread}.
     *
     * @apiNote It can be overridden by system property with key {@link #DEFAULT_WORKER_THREAD_PREFIX}
     */
    public final String workerThreadPrefix;

    /**
     * Declares the default worker thread pool size. Defaults is {@code 3}.
     *
     * @apiNote It can be overridden by system property with key {@link #DEFAULT_WORKER_THREAD_POOL_SIZE}
     */
    public final int workerThreadPoolSize;

    DefaultOptions() {
        this.maxExecutionTimeout    = loadMaxExecutionTimeout();
        this.maxEvaluationTimeout   = loadMaxEvaluationTimeout();
        this.maxTriggerRuleLeeway   = loadTriggerRuleLeeway();
        this.maxTriggerPreviewCount = loadTriggerPreviewCount();
        this.workerThreadPrefix     = loadWorkerThreadPrefix();
        this.workerThreadPoolSize   = loadWorkerThreadPoolSize();
    }

    private static Duration loadMaxExecutionTimeout() {
        try {
            return Duration.parse(System.getProperty(DEFAULT_MAX_EXECUTION_TIMEOUT));
        } catch (DateTimeParseException | NullPointerException ex) {
            return Duration.of(VertxOptions.DEFAULT_MAX_WORKER_EXECUTE_TIME,
                               Utils.toChronoUnit(VertxOptions.DEFAULT_MAX_WORKER_EXECUTE_TIME_UNIT));
        }
    }

    private static Duration loadMaxEvaluationTimeout() {
        try {
            return Duration.parse(System.getProperty(DEFAULT_MAX_EVALUATION_TIMEOUT));
        } catch (DateTimeParseException | NullPointerException ex) {
            return Duration.of(VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME,
                               Utils.toChronoUnit(VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME_UNIT));
        }
    }

    private static Duration loadTriggerRuleLeeway() {
        try {
            return Duration.parse(System.getProperty(DEFAULT_MAX_TRIGGER_RULE_LEEWAY));
        } catch (DateTimeParseException | NullPointerException ex) {
            return Duration.ofSeconds(10);
        }
    }

    private static int loadTriggerPreviewCount() {
        try {
            return Integer.parseInt(System.getProperty(DEFAULT_MAX_TRIGGER_PREVIEW_COUNT));
        } catch (NumberFormatException | NullPointerException ex) {
            return 30;
        }
    }

    private static String loadWorkerThreadPrefix() {
        return System.getProperty(DEFAULT_WORKER_THREAD_PREFIX, "scheduler.x-worker-thread");
    }

    private static int loadWorkerThreadPoolSize() {
        try {
            return Integer.parseInt(System.getProperty(DEFAULT_WORKER_THREAD_POOL_SIZE));
        } catch (NumberFormatException | NullPointerException ex) {
            return 3;
        }
    }

}
