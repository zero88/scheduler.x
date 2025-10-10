package io.github.zero88.schedulerx;

/**
 * Represents for a fake job that does nothing
 *
 * @param <IN>  Type of job input data
 * @param <OUT> Type of job result data
 */
public interface NoopJob<IN, OUT> extends SyncJob<IN, OUT> {

    static <IN, OUT> Job<IN, OUT> create() { return (jobData, executionContext) -> { }; }

    static <IN, OUT> Job<IN, OUT> create(int stopAtRound) {
        return (jobData, executionContext) -> {
            if (executionContext.round() == stopAtRound) {
                executionContext.forceStopExecution();
            }
        };
    }

}
