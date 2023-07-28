package io.github.zero88.schedulerx;

import org.jetbrains.annotations.Nullable;

/**
 * Represents for a provider that supply an input task data
 *
 * @param <T> Type of data
 * @since 1.0.0
 */
@FunctionalInterface
public interface JobData<T> {

    /**
     * Get input data
     *
     * @return input data
     */
    @Nullable T get();

    /**
     * Create emtpy data
     *
     * @param <D> Type of data
     * @return JobData contains null data
     */
    static <D> JobData<D> empty() {
        return () -> null;
    }

}
