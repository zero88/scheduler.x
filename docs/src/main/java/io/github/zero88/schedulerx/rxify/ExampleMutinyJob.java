package io.github.zero88.schedulerx.rxify;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.mutiny.ExecutionContext;
import io.github.zero88.schedulerx.mutiny.MutinyJob;
import io.smallrye.mutiny.Uni;
import io.vertx.docgen.Source;
import io.vertx.mutiny.core.buffer.Buffer;

@Source
public class ExampleMutinyJob implements MutinyJob<Void, String> {

    @Override
    public Uni<String> doExecute(@NotNull JobData<Void> jobData, @NotNull ExecutionContext<String> executionContext) {
        final io.vertx.mutiny.core.Vertx vertx = executionContext.vertx();
        return vertx.fileSystem().readFile("/tmp/hello-from-mutiny.x").map(Buffer::toString);
    }

}
