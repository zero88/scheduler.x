package io.github.zero88.schedulerx;

import io.github.zero88.schedulerx.impl.SchedulingCompositeMonitorImpl;

/**
 * Represents for a delegated monitor that holds multiple scheduling monitors.
 * <p/>
 *
 * @param <OUT> Type of job result data
 * @apiNote The holder keeps only one monitor per java class, if try to register many instances of same class, the
 *     holder will keep the last one
 * @since 2.0.0
 */
public interface SchedulingCompositeMonitor<OUT> extends SchedulingMonitor<OUT> {

    static <O> SchedulingCompositeMonitor<O> create() {
        return new SchedulingCompositeMonitorImpl<>();
    }

    /**
     * Register a new scheduling monitor.
     *
     * @param monitor the scheduling monitor
     * @return a reference to this for fluent API
     */
    SchedulingCompositeMonitor<OUT> register(SchedulingMonitor<OUT> monitor);

}
