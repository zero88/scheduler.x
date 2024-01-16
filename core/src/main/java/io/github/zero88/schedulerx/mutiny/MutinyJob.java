package io.github.zero88.schedulerx.mutiny;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.FuturableJob;
import io.github.zero88.schedulerx.JobData;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.UniHelper;
import io.vertx.core.Future;

/**
 * Job interface for {@link Uni} in <a href="https://smallrye.io/smallrye-mutiny">#mutiny</a> version.
 *
 * @since 2.0.0
 */
public interface MutinyJob<INPUT, OUTPUT> extends FuturableJob<INPUT, OUTPUT, Uni<OUTPUT>, ExecutionContext<OUTPUT>> {

    @Override
    default ExecutionContext<OUTPUT> transformContext(
        @NotNull io.github.zero88.schedulerx.ExecutionContext<OUTPUT> executionContext) {
        return ExecutionContext.newInstance(executionContext);
    }

    @Override
    default Future<OUTPUT> transformResult(Uni<OUTPUT> result) { return UniHelper.toFuture(result); }

    Uni<OUTPUT> doAsync(@NotNull JobData<INPUT> jobData,
                        @NotNull io.github.zero88.schedulerx.mutiny.ExecutionContext<OUTPUT> executionContext);

}
