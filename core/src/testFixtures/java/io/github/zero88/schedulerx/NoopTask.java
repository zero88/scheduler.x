package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

/**
 * Represents for dummy task that do nothing
 *
 * @param <IN>  Type of input
 * @param <OUT> Type of output
 */
public interface NoopTask<IN, OUT> extends Task<IN, OUT> {

    static <IN, OUT> NoopTask<IN, OUT> create() { return new NoopTask<IN, OUT>() { }; }

    @Override
    default void execute(@NotNull JobData<IN> jobData, @NotNull TaskExecutionContext<OUT> executionContext) { }

}
