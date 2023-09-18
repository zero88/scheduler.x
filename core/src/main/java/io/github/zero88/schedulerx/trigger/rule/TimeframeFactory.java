package io.github.zero88.schedulerx.trigger.rule;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.DateTimeException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.vertx.core.ServiceHelper;

@SuppressWarnings("rawtypes")
final class TimeframeFactory {

    private final Map<Class, Constructor<? extends Timeframe>> timeframeMapper;

    private TimeframeFactory(Map<Class, Constructor<? extends Timeframe>> timeframeMapper) {
        this.timeframeMapper = timeframeMapper;
    }

    static TimeframeFactory getInstance() { return TimeframeFactory.Holder.INSTANCE; }

    Timeframe create(String type, Object from, Object to) {
        try {
            final Class<?> cls = Class.forName(Objects.requireNonNull(type, "Timeframe type is required"));
            final Constructor<? extends Timeframe> constructor = this.timeframeMapper.get(cls);
            if (constructor == null) {
                throw new UnsupportedOperationException("Unrecognized a timeframe with type[" + type + "]");
            }
            final Timeframe instance = constructor.newInstance();
            if (instance instanceof BaseTimeframe) {
                return ((BaseTimeframe<?>) instance).setValues(from, to);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalArgumentException("Unable to init new timeframe instance", ex);
        } catch (ClassNotFoundException ex) {
            throw new UnsupportedOperationException("Unrecognized a timeframe with type[" + type + "]", ex);
        } catch (DateTimeException | NullPointerException ex) {
            throw new IllegalArgumentException("Unable to parse a timeframe. Cause: " + ex.getMessage(), ex);
        }
    }

    private static class Holder {

        private static final TimeframeFactory INSTANCE = new TimeframeFactory(
            ServiceHelper.loadFactories(Timeframe.class, TimeframeFactory.class.getClassLoader())
                         .stream()
                         .collect(Collectors.toMap(Timeframe::type, Holder::getConstructor, (p1, p2) -> p2)));

        private static Constructor<? extends Timeframe> getConstructor(Timeframe<?> m) {
            try {
                return m.getClass().getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("must never happened", e);
            }
        }

    }

}
