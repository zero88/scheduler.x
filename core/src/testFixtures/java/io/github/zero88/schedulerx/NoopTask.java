package io.github.zero88.schedulerx;

/**
 * Represents for dummy task that do nothing
 *
 * @param <IN>  Type of task input data
 * @param <OUT> Type of task result data
 */
public interface NoopTask<IN, OUT> extends Task<IN, OUT> {

    static <IN, OUT> Task<IN, OUT> create() { return (jobData, executionContext) -> { }; }

    static <IN, OUT> Task<IN, OUT> create(int stopAtRound) {
        return (jobData, executionContext) -> {
            if (executionContext.round() == stopAtRound) {
                executionContext.forceStopExecution();
            }
        };
    }

}
