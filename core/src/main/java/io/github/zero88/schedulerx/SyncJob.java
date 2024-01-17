package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

/**
 * Represents for Sync Job to run on each trigger time.
 * <p/>
 * It is ideal if your concrete class is a class that has a constructor without argument, which makes it easier to init
 * a new job object in runtime via the configuration from an external system.
 *
 * @since 2.0.0
 */
public interface SyncJob<INPUT, OUTPUT> extends Job<INPUT, OUTPUT> {

    @Override
    default void execute(@NotNull JobData<INPUT> jobData, @NotNull ExecutionContext<OUTPUT> executionContext) {
        executionContext.complete(doExecute(jobData, executionContext));
    }

    /**
     * Do execute job
     *
     * @param jobData          job data
     * @param executionContext job execution context
     * @return the execution result
     */
    OUTPUT doExecute(JobData<INPUT> jobData, ExecutionContext<OUTPUT> executionContext);

}
