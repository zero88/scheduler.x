package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.impl.Utils;

/**
 * Represents for a provider that supplies input data before the execution is started, and that will be sent to the task
 * in runtime execution.
 *
 * @param <T> Type of data
 * @since 1.0.0
 */
@FunctionalInterface
public interface JobData<T> {

    /**
     * Get an input data.
     * <p/>
     * It might be a static input value or a preloaded value from an external system
     * or a configuration to instruct how to get actual input data of the task in runtime execution.
     *
     * @return input data
     */
    @Nullable T get();

    /**
     * Declares a unique id in an external system that will be propagated to the task result.
     * <p/>
     * That makes the integration between the task monitoring and the external system seamless and easier.
     *
     * @return an external id
     * @see ExecutionResult#externalId()
     * @since 2.0.0
     */
    default @Nullable Object externalId() { return null; }

    /**
     * Create emtpy data with random external id in integer.
     *
     * @param <D> Type of data
     * @return JobData contains null data
     * @since 2.0.0
     */
    static <D> JobData<D> empty() { return empty(Utils.randomPositiveInt()); }

    /**
     * Create emtpy data with an external id.
     *
     * @param <D> Type of data
     * @return JobData contains null data
     * @since 2.0.0
     */
    static <D> JobData<D> empty(@NotNull Object externalId) {
        return new JobData<D>() {
            public @Nullable D get() { return null; }

            @Override
            public Object externalId() { return externalId; }
        };
    }

    /**
     * Create JobData from static data with random external id in integer.
     *
     * @param <D> Type of data
     * @return JobData
     * @since 2.0.0
     */
    static <D> JobData<D> create(@NotNull D data) {
        return create(data, Utils.randomPositiveInt());
    }

    /**
     * Create JobData from static data and external id.
     *
     * @param <D> Type of data
     * @return JobData
     * @since 2.0.0
     */
    static <D> JobData<D> create(@NotNull D data, @NotNull Object externalId) {
        return new JobData<D>() {
            public D get() { return data; }

            @Override
            public Object externalId() { return externalId; }
        };
    }

}
