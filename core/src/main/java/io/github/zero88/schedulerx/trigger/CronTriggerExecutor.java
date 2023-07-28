package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.TriggerTaskExecutor;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Represents for the task executor has an execution loop based on the timer of cron expressions.
 *
 * @param <INPUT>  Type of job input data
 * @param <OUTPUT> Type of Result data
 * @see CronTrigger
 * @since 2.0.0
 */
@VertxGen
public interface CronTriggerExecutor<INPUT, OUTPUT> extends TriggerTaskExecutor<INPUT, OUTPUT, CronTrigger> {

    static <IN, OUT> CronTriggerExecutorBuilder<IN, OUT> builder() {
        return new CronTriggerExecutorImpl.CronTriggerExecutorBuilderImpl<>();
    }

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTrigger trigger();

}
