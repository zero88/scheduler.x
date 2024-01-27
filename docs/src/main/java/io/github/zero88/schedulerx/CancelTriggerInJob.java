package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.vertx.docgen.Source;

@Source
public class CancelTriggerInJob implements Job<Void, Void> {

    @Override
    public void execute(@NotNull JobData<Void> jobData, @NotNull ExecutionContext<Void> executionContext) {
        // do something
        // ...
        if (executionContext.round() == 10) {
            executionContext.forceStopExecution();
        }
    }

}
