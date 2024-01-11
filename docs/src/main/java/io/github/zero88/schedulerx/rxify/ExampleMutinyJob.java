package io.github.zero88.schedulerx.rxify;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.mutiny.ExecutionContext;
import io.github.zero88.schedulerx.mutiny.MutinyJob;
import io.smallrye.mutiny.Uni;
import io.vertx.docgen.Source;

@Source
public class ExampleMutinyJob implements MutinyJob<Void, Void> {

    @Override
    public Uni<Void> doExecute(@NotNull JobData<Void> jobData, @NotNull ExecutionContext<Void> executionContext) {
        final io.vertx.mutiny.core.Vertx vertx = executionContext.vertx();
        return vertx.fileSystem().createFile("/tmp/hello-from-scheduler.x");
    }

}
