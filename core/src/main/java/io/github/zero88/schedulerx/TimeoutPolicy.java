package io.github.zero88.schedulerx;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BinaryOperator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents for holder to keep timeout configuration.
 *
 * @since 2.0.0
 */
public final class TimeoutPolicy {

    private final Duration evaluationTimeout;
    private final Duration executionTimeout;

    private TimeoutPolicy(Duration evaluationTimeout, Duration executionTimeout) {
        this.evaluationTimeout = evaluationTimeout;
        this.executionTimeout  = executionTimeout;
    }

    /**
     * Create the default timeout policy
     */
    public static TimeoutPolicy byDefault() {
        return create(null, null);
    }

    /**
     * Create timeout policy with execution timeout
     *
     * @param executionTimeout given execution timeout
     * @return timeout policy
     */
    public static TimeoutPolicy create(@NotNull Duration executionTimeout) {
        return create(null, executionTimeout);
    }

    /**
     * Create timeout policy with execution timeout
     *
     * @param evaluationTimeout given evaluation timeout
     * @param executionTimeout  given execution timeout
     * @return timeout policy
     */
    @JsonCreator
    public static TimeoutPolicy create(@JsonProperty("evaluationTimeout") @Nullable Duration evaluationTimeout,
                                       @JsonProperty("executionTimeout") @Nullable Duration executionTimeout) {
        final BinaryOperator<Duration> check = (duration, defaultMax) -> {
            if (duration == null || duration.compareTo(Duration.ZERO) <= 0 || duration.compareTo(defaultMax) > 0) {
                return defaultMax;
            }
            return duration;
        };
        return new TimeoutPolicy(check.apply(evaluationTimeout, DefaultOptions.getInstance().evaluationMaxTimeout),
                                 check.apply(executionTimeout, DefaultOptions.getInstance().executionMaxTimeout));
    }

    /**
     * Declares the trigger evaluation timeout. Default is {@link DefaultOptions#evaluationMaxTimeout}
     *
     * @return the evaluation timeout
     * @since 2.0.0
     */
    @JsonGetter
    public @NotNull Duration evaluationTimeout() { return evaluationTimeout; }

    /**
     * Declares the execution timeout. Default is {@link DefaultOptions#executionMaxTimeout}
     *
     * @return the execution timeout
     * @since 2.0.0
     */
    @JsonGetter
    public @NotNull Duration executionTimeout() { return executionTimeout; }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TimeoutPolicy that = (TimeoutPolicy) o;

        return Objects.equals(evaluationTimeout, that.evaluationTimeout) &&
               Objects.equals(executionTimeout, that.executionTimeout);
    }

    @Override
    public int hashCode() {
        int result = evaluationTimeout != null ? evaluationTimeout.hashCode() : 0;
        result = 31 * result + (executionTimeout != null ? executionTimeout.hashCode() : 0);
        return result;
    }

}
