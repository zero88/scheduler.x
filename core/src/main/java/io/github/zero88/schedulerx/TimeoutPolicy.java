package io.github.zero88.schedulerx;

import java.time.Duration;
import java.util.function.BiFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.impl.Utils;
import io.vertx.core.VertxOptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class TimeoutPolicy {

    private final Duration evaluationTimeout;
    private final Duration executionTimeout;

    private TimeoutPolicy(Duration evaluationTimeout, Duration executionTimeout) {
        this.evaluationTimeout = evaluationTimeout;
        this.executionTimeout  = executionTimeout;
    }

    public static TimeoutPolicy byDefault() {
        return create(null, null);
    }

    public static TimeoutPolicy create(@NotNull Duration executionTimeout) {
        return create(null, executionTimeout);
    }

    @JsonCreator
    public static TimeoutPolicy create(@JsonProperty("evaluationTimeout") @Nullable Duration evaluationTimeout,
                                       @JsonProperty("executionTimeout") @Nullable Duration executionTimeout) {
        final BiFunction<Duration, Duration, Duration> check = (duration, defaultMax) -> {
            if (duration == null || duration.isNegative() || duration.isZero() || duration.compareTo(defaultMax) > 0) {
                return defaultMax;
            }
            return duration;
        };
        final Duration evTimeout = check.apply(evaluationTimeout,
                                               Duration.of(VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME,
                                                           Utils.toChronoUnit(
                                                               VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME_UNIT)));
        final Duration exeTimeout = check.apply(executionTimeout,
                                                Duration.of(VertxOptions.DEFAULT_MAX_WORKER_EXECUTE_TIME,
                                                            Utils.toChronoUnit(
                                                                VertxOptions.DEFAULT_MAX_WORKER_EXECUTE_TIME_UNIT)));
        return new TimeoutPolicy(evTimeout, exeTimeout);
    }

    /**
     * Declares the evaluation timeout.
     *
     * @return the evaluation timeout
     * @since 2.0.0
     */
    @JsonGetter
    public @NotNull Duration evaluationTimeout() { return evaluationTimeout; }

    /**
     * Declares the execution timeout.
     *
     * @return the execution timeout
     * @since 2.0.0
     */
    @JsonGetter
    public @NotNull Duration executionTimeout() { return executionTimeout; }

}
