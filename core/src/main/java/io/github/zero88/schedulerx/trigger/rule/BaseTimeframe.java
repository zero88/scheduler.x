package io.github.zero88.schedulerx.trigger.rule;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

abstract class BaseTimeframe<T> implements Timeframe<T>, TimeParser<T> {

    private T from;
    private T to;

    protected BaseTimeframe() { }

    protected final BaseTimeframe<T> setValues(Object from, Object to) {
        final TimeframeValidator validator = validator();
        final TimeParser<T> parser = parser();
        this.from = validator.normalize(parser, from);
        this.to   = validator.normalize(parser, to);
        validator.validate(this);
        return this;
    }

    @Override
    public final T from() { return from; }

    @Override
    public final T to() { return to; }

    @Override
    public final @NotNull TimeParser<T> parser() { return this; }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + type().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Timeframe<?> that = (Timeframe<?>) o;
        return Objects.equals(type(), that.type()) && Objects.equals(from, that.from()) &&
               Objects.equals(to, that.to());
    }

    @Override
    public String toString() {
        return "TimeFrame{type=" + type().getName() + ", (" + from + ", " + to + ")}";
    }

}
