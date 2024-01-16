package io.github.zero88.schedulerx.rxjava3;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.FuturableJob;

interface Rx3Job<INPUT, OUTPUT, T> extends FuturableJob<INPUT, OUTPUT, T, ExecutionContext<OUTPUT>> {

    @Override
    default ExecutionContext<OUTPUT> transformContext(
        @NotNull io.github.zero88.schedulerx.ExecutionContext<OUTPUT> executionContext) {
        return ExecutionContext.newInstance(executionContext);
    }

}
