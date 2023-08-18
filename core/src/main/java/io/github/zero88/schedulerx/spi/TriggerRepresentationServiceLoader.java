package io.github.zero88.schedulerx.spi;

import java.util.Map;
import java.util.stream.Collectors;

import io.github.zero88.schedulerx.trigger.HasTriggerType;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.ServiceHelper;

/**
 * Represents the service loader {@code META-INF/services} for {@link TriggerRepresentationMapper} providers
 *
 * @since 2.0.0
 */
public final class TriggerRepresentationServiceLoader {

    private static class Holder {

        @SuppressWarnings("unchecked")
        private static final TriggerRepresentationServiceLoader INSTANCE = new TriggerRepresentationServiceLoader(
            ServiceHelper.loadFactories(TriggerRepresentationMapper.class,
                                        TriggerRepresentationMapper.class.getClassLoader())
                         .stream()
                         .collect(Collectors.toMap(HasTriggerType::type, m -> m, (p1, p2) -> p2)));

    }

    public static TriggerRepresentationServiceLoader getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<String, TriggerRepresentationMapper<Trigger>> providers;

    private TriggerRepresentationServiceLoader(Map<String, TriggerRepresentationMapper<Trigger>> providers) {
        this.providers = providers;
    }

    public TriggerRepresentationMapper<Trigger> getProvider(String type) {
        return providers.getOrDefault(type, SimpleTriggerRepresentationMapper.getInstance());
    }

}
