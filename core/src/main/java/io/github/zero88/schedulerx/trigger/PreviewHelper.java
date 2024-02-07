package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.rule.TriggerRule;

class PreviewHelper {

    private PreviewHelper() { }

    @NotNull
    static PreviewParameter normalize(@NotNull PreviewParameter parameter, @NotNull TriggerRule defaultRule,
                                      @NotNull ZoneId defaultZoneId) {
        return parameter.setRule(Optional.ofNullable(parameter.getRule()).orElse(defaultRule))
                        .setTimeZone(Optional.ofNullable(parameter.getTimeZone()).orElse(defaultZoneId))
                        .setStartedAt(Optional.ofNullable(parameter.getStartedAt()).orElseGet(Instant::now));
    }

    @NotNull
    static List<OffsetDateTime> preview(@NotNull TimebasedTrigger trigger, @NotNull PreviewParameter normalized) {
        final List<OffsetDateTime> result = new ArrayList<>();
        final long count = normalized.getTimes();
        final ZoneId zoneId = Objects.requireNonNull(normalized.getTimeZone());
        final TriggerRule rule = Objects.requireNonNull(normalized.getRule());
        Instant next = Objects.requireNonNull(normalized.getStartedAt());
        int skipTime = 1;
        do {
            next = trigger.nextTriggerTime(next);
            if (rule.isPending(next)) {
                skipTime++;
                continue;
            }
            if (rule.isExceeded(next)) {
                break;
            }
            if (rule.satisfy(next)) {
                result.add(next.atZone(zoneId).toOffsetDateTime());
            }
        } while (result.size() != count || skipTime > 1000);
        return result;
    }

}
