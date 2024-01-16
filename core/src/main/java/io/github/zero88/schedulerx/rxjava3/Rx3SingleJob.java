package io.github.zero88.schedulerx.rxjava3;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.rxjava3.SingleHelper;

/**
 * Job interface for {@link Single} in <a href="https://reactivex.io/">#reactivex</a> version.
 *
 * @since 2.0.0
 */
public interface Rx3SingleJob<INPUT, OUTPUT> extends Rx3Job<INPUT, OUTPUT, Single<OUTPUT>> {

    @Override
    default Future<OUTPUT> transformResult(Single<OUTPUT> result) { return SingleHelper.toFuture(result); }

    Single<OUTPUT> doAsync(@NotNull JobData<INPUT> jobData,
                           @NotNull io.github.zero88.schedulerx.rxjava3.ExecutionContext<OUTPUT> executionContext);

}
