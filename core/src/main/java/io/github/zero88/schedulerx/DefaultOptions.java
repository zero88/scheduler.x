package io.github.zero88.schedulerx;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

import io.github.zero88.schedulerx.impl.Utils;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.VertxOptions;

/**
 * The class instance is used to configure the default {@link Trigger} options, and {@link Job} options.
 *
 * @since 2.0.0
 */
public final class DefaultOptions {

    public static final String PROP_EVALUATION_MAX_TIMEOUT = "schedulerx.default_evaluation_max_timeout";
    public static final String PROP_EXECUTION_MAX_TIMEOUT = "schedulerx.default_execution_max_timeout";
    public static final String PROP_EXECUTION_THREAD_PREFIX = "schedulerx.default_execution_thread_prefix";
    public static final String PROP_EXECUTION_THREAD_POOL_SIZE = "schedulerx.default_execution_thread_pool_size";

    public static final String PROP_MONITOR_MAX_TIMEOUT = "schedulerx.default_monitor_max_timeout";
    public static final String PROP_MONITOR_THREAD_PREFIX = "schedulerx.default_monitor_thread_prefix";
    public static final String PROP_MONITOR_THREAD_POOL_SIZE = "schedulerx.default_monitor_thread_pool_size";

    public static final String PROP_TRIGGER_RULE_PROP_MAX_LEEWAY = "schedulerx.default_trigger_rule_max_leeway";
    public static final String PROP_TRIGGER_PREVIEW_MAX_COUNT = "schedulerx.default_trigger_preview_max_count";


    private static class Holder {

        private static final DefaultOptions INSTANCE = new DefaultOptions();

    }

    public static DefaultOptions getInstance() {
        return DefaultOptions.Holder.INSTANCE;
    }

    /**
     * Declares the default max execution timeout. Defaults is {@link VertxOptions#DEFAULT_MAX_WORKER_EXECUTE_TIME}
     *
     * @apiNote It can be overridden by system property with key {@value #PROP_EXECUTION_MAX_TIMEOUT}
     */
    public final Duration executionMaxTimeout;

    /**
     * Declares the default max trigger evaluation timeout. Defaults is
     * {@link VertxOptions#DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME}
     *
     * @apiNote It can be overridden by system property with key {@value #PROP_EVALUATION_MAX_TIMEOUT}
     */
    public final Duration evaluationMaxTimeout;

    /**
     * Declares the default max trigger rule leeway time. Defaults is {@code 10 seconds}.
     *
     * @apiNote It can be overridden by system property with key {@value #PROP_TRIGGER_RULE_PROP_MAX_LEEWAY}
     */
    public final Duration triggerRuleMaxLeeway;

    /**
     * Declares the default max number of the trigger preview items. Defaults is {@code 30}.
     *
     * @apiNote It can be overridden by system property with key {@value #PROP_TRIGGER_PREVIEW_MAX_COUNT}
     */
    public final int triggerPreviewMaxCount;

    /**
     * Declares the default worker thread name prefix for the execution operation. Defaults is
     * {@code scheduler.x-worker-thread}.
     *
     * @apiNote It can be overridden by system property with key {@value #PROP_EXECUTION_THREAD_PREFIX}
     */
    public final String executionThreadPrefix;

    /**
     * Declares the default worker thread pool size for the execution operation. Defaults is {@code 5}.
     *
     * @apiNote It can be overridden by system property with key {@value #PROP_EXECUTION_THREAD_POOL_SIZE}
     */
    public final int executionThreadPoolSize;

    /**
     * Declares the default max scheduling monitor timeout. Defaults is
     * {@link VertxOptions#DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME}
     *
     * @apiNote It can be overridden by system property with key {@value #PROP_MONITOR_MAX_TIMEOUT}
     */
    public final Duration monitorMaxTimeout;

    /**
     * Declares the default worker thread name prefix for the monitor operation. Defaults is
     * {@code scheduler.x-monitor-thread}.
     *
     * @apiNote It can be overridden by system property with key {@value #PROP_MONITOR_THREAD_PREFIX}
     */
    public final String monitorThreadPrefix;

    /**
     * Declares the default worker thread pool size for the monitor operation. Defaults is {@code 3}.
     *
     * @apiNote It can be overridden by system property with key {@value #PROP_MONITOR_THREAD_POOL_SIZE}
     */
    public final int monitorThreadPoolSize;

    DefaultOptions() {
        this.triggerRuleMaxLeeway   = loadDuration(PROP_TRIGGER_RULE_PROP_MAX_LEEWAY, 10, TimeUnit.SECONDS);
        this.triggerPreviewMaxCount = loadInteger(PROP_TRIGGER_PREVIEW_MAX_COUNT, 30);

        this.executionThreadPrefix   = System.getProperty(PROP_EXECUTION_THREAD_PREFIX, "scheduler.x-worker-thread");
        this.executionThreadPoolSize = loadInteger(PROP_EXECUTION_THREAD_POOL_SIZE, 5);
        this.executionMaxTimeout     = loadDuration(PROP_EXECUTION_MAX_TIMEOUT,
                                                    VertxOptions.DEFAULT_MAX_WORKER_EXECUTE_TIME,
                                                    VertxOptions.DEFAULT_MAX_WORKER_EXECUTE_TIME_UNIT);
        this.evaluationMaxTimeout    = loadDuration(PROP_EVALUATION_MAX_TIMEOUT,
                                                    VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME,
                                                    VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME_UNIT);

        this.monitorThreadPrefix   = System.getProperty(PROP_MONITOR_THREAD_PREFIX, "scheduler.x-monitor-thread");
        this.monitorThreadPoolSize = loadInteger(PROP_MONITOR_THREAD_POOL_SIZE, 3);
        this.monitorMaxTimeout     = loadDuration(PROP_MONITOR_MAX_TIMEOUT,
                                                  VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME,
                                                  VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME_UNIT);
    }

    private static Duration loadDuration(String prop, long defaultTimeout, TimeUnit defaultTimeUnit) {
        try {
            return Duration.parse(System.getProperty(prop));
        } catch (DateTimeParseException | NullPointerException ex) {
            return Duration.of(defaultTimeout, Utils.toChronoUnit(defaultTimeUnit));
        }
    }

    private static int loadInteger(String prop, int defaultValue) {
        try {
            return Integer.parseInt(System.getProperty(prop));
        } catch (NumberFormatException | NullPointerException ex) {
            return defaultValue;
        }
    }

}
