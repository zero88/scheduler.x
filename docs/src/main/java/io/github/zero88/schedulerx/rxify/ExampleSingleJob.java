package io.github.zero88.schedulerx.rxify;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.rxjava3.ExecutionContext;
import io.github.zero88.schedulerx.rxjava3.Rx3SingleJob;
import io.reactivex.rxjava3.core.Single;
import io.vertx.docgen.Source;

@Source
public class ExampleSingleJob implements Rx3SingleJob<Void, String> {

    @Override
    public Single<String> doExecute(@NotNull JobData<Void> jobData,
                                    @NotNull ExecutionContext<String> executionContext) {
        final io.vertx.rxjava3.core.Vertx vertx = executionContext.vertx();
        return vertx.fileSystem().rxCreateTempFile("rx3", "single");
    }

}
