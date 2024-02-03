package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The trigger rule builder to construct {@link TriggerRule}
 *
 * @since 2.0.0
 */
@SuppressWarnings("rawtypes")
public final class TriggerRuleBuilder {

    private List<Timeframe> timeframes = new ArrayList<>();
    private Instant beginTime;
    private Instant until;
    private Duration leeway;

    TriggerRuleBuilder() { }

    /**
     * Add timeframe
     */
    public TriggerRuleBuilder timeframe(Timeframe timeframe) {
        return timeframes(timeframe);
    }

    /**
     * Add multiple timeframes
     */
    public TriggerRuleBuilder timeframes(Timeframe... timeframes) {
        Arrays.stream(timeframes).filter(Objects::nonNull).forEach(tf -> this.timeframes.add(tf));
        return this;
    }

    /**
     * Set list of timeframes
     */
    public TriggerRuleBuilder timeframes(List<Timeframe> timeframes) {
        if (timeframes != null) {
            this.timeframes = timeframes.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        return this;
    }

    public TriggerRuleBuilder beginTime(Instant beginTime) {
        this.beginTime = beginTime;
        return this;
    }

    public TriggerRuleBuilder until(Instant until) {
        this.until = until;
        return this;
    }

    public TriggerRuleBuilder leeway(Duration leeway) {
        this.leeway = leeway;
        return this;
    }

    public TriggerRule build() {
        return new TriggerRuleImpl(beginTime, until, timeframes, leeway);
    }

}
