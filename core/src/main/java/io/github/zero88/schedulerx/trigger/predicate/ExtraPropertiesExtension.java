package io.github.zero88.schedulerx.trigger.predicate;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface ExtraPropertiesExtension<T extends ExtraPropertiesExtension<T>> {

    /**
     * Load extra properties into this instance
     *
     * @param properties the extra properties
     * @return a reference to this for fluent API
     */
    @NotNull T load(@Nullable Map<String, Object> properties);

}
