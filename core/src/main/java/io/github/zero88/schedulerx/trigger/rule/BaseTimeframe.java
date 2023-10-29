package io.github.zero88.schedulerx.trigger.rule;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

abstract class BaseTimeframe<T> implements Timeframe<T>, TimeParser<T> {

    private T from;
    private T to;
    private int hashCode;

    protected BaseTimeframe() { }

    protected final BaseTimeframe<T> setValues(Object from, Object to) {
        final TimeframeValidator validator = validator();
        final TimeParser<T> parser = parser();
        this.from = validator.normalize(parser, from);
        this.to   = validator.normalize(parser, to);
        //noinspection unchecked
        final BaseTimeframe<T> self = (BaseTimeframe<T>) validator.validate(this);
        this.hashCode = computeHashCode();
        return self;
    }

    @Override
    public final T from() { return from; }

    @Override
    public final T to() { return to; }

    @Override
    public final @NotNull TimeParser<T> parser() { return this; }

    @Override
    public @NotNull Timeframe<T> set(@NotNull String field, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() { return hashCode; }

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
        String fromStr = from == null ? "-" : from.toString();
        String toStr = to == null ? "-" : to.toString();
        return "TimeFrame{type=" + type().getName() + ", [" + fromStr + ", " + toStr + ")}";
    }

    private int computeHashCode() {
        int result = type().hashCode();
        result = 31 * result + Optional.ofNullable(from).map(Object::hashCode).orElse(0);
        result = 31 * result + Optional.ofNullable(to).map(Object::hashCode).orElse(0);
        return result;
    }

}
