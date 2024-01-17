package io.github.zero88.schedulerx;

import java.util.concurrent.CompletionStage;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Future;

/**
 * Job interface for java concurrent {@link CompletionStage}.
 *
 * @since 2.0.0
 */
public interface CompletionStageJob<INPUT, OUTPUT>
    extends FuturableJob<INPUT, OUTPUT, CompletionStage<OUTPUT>, ExecutionContext<OUTPUT>> {

    @Override
    default ExecutionContext<OUTPUT> transformContext(@NotNull ExecutionContext<OUTPUT> executionContext) {
        return executionContext;
    }

    @Override
    default Future<OUTPUT> transformResult(CompletionStage<OUTPUT> result) {
        return Future.fromCompletionStage(result);
    }

    CompletionStage<OUTPUT> doExecute(@NotNull JobData<INPUT> jobData,
                                      @NotNull ExecutionContext<OUTPUT> executionContext);

}
