package io.github.zero88.schedulerx.rxjava3;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.Future;
import io.vertx.rxjava3.MaybeHelper;

/**
 * Job interface for {@link Maybe} in <a href="https://reactivex.io/">#reactivex</a> version.
 *
 * @since 2.0.0
 */
public interface Rx3MaybeJob<INPUT, OUTPUT> extends Rx3Job<INPUT, OUTPUT, Maybe<OUTPUT>> {

    @Override
    default Future<OUTPUT> transformResult(Maybe<OUTPUT> result) { return MaybeHelper.toFuture(result); }

    Maybe<OUTPUT> doExecute(@NotNull JobData<INPUT> jobData,
                            @NotNull io.github.zero88.schedulerx.rxjava3.ExecutionContext<OUTPUT> executionContext);

}
