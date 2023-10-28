package io.github.zero88.schedulerx.trigger.rule.custom;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.rule.TimeParser;
import io.github.zero88.schedulerx.trigger.rule.Timeframe;

import com.fasterxml.jackson.annotation.JsonGetter;

public class SimpleDateTimeTimeframe implements Timeframe<Date> {

    private Date from;
    private Date to;
    private TimeZone zone = TimeZone.getDefault();
    private final DateFormat formatter = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss");

    @Override
    public @NotNull Class<Date> type() {
        return Date.class;
    }

    @Override
    public @Nullable Date from() { return from; }

    @Override
    public @Nullable Date to() { return to; }

    @JsonGetter
    public TimeZone timeZone() { return zone; }

    @JsonGetter
    public String getFrom() { return formatter.format(from); }

    @JsonGetter
    public String getTo() { return formatter.format(to); }

    @Override
    public boolean check(@NotNull Instant instant) {
        return false;
    }

    @Override
    public @NotNull TimeParser<Date> parser() {
        return rawValue -> {
            final Calendar calendar = Calendar.getInstance(zone);
            if (rawValue instanceof String) {
                try {
                    calendar.setTime(formatter.parse((String) rawValue));
                } catch (ParseException e) {
                    throw new DateTimeException("Unable to parse", e);
                }
            } else {
                calendar.setTime((Date) rawValue);
            }
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        };
    }

    @Override
    public @NotNull Timeframe<Date> set(@NotNull String field, @Nullable Object value) {
        if ("from".equals(field))
            from = (Date) value;
        if ("to".equals(field))
            to = (Date) value;
        if ("timeZone".equals(field)) {
            zone = value instanceof String ? TimeZone.getTimeZone((String) value) : (TimeZone) value;
            formatter.setTimeZone(zone);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SimpleDateTimeTimeframe that = (SimpleDateTimeTimeframe) o;

        return type().equals(that.type()) && Objects.equals(from, that.from) && Objects.equals(to, that.to) &&
               Objects.equals(zone, that.zone);
    }

    @Override
    public int hashCode() {
        int result = type().hashCode();
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        return result;
    }

}
