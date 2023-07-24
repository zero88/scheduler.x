package io.github.zero88.schedulerx;

/**
 * Represents for input task data
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface JobData {

    Object get();

    JobData EMPTY = () -> null;

}
