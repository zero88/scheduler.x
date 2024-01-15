package io.github.zero88.schedulerx.impl;

import io.github.zero88.schedulerx.SchedulingMonitor;

/**
 * An internal scheduling monitor to ensure the monitor operation is run on the dedicated thread
 *
 * @param <OUT> Type of job result data
 * @since 2.0.0
 */
interface SchedulingMonitorInternal<OUT> extends SchedulingMonitor<OUT> {

    SchedulingMonitor<OUT> unwrap();

}
