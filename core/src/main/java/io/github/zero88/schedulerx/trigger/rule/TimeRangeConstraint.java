package io.github.zero88.schedulerx.trigger.rule;

import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

public interface TimeRangeConstraint extends TimeframeValidator {

    @SuppressWarnings("rawtypes")
    default Timeframe validate(Timeframe timeframe) {
        final Object from = timeframe.from();
        final Object to = timeframe.to();
        if (from instanceof Temporal && to instanceof Temporal) {
            ChronoUnit unit = ChronoUnit.NANOS.isSupportedBy((Temporal) from) ? ChronoUnit.NANOS : ChronoUnit.DAYS;
            if (unit.between((Temporal) from, (Temporal) to) <= 0) {
                throw new IllegalArgumentException("'From' value must be before 'To' value");
            }
        }
        return timeframe;
    }

}
