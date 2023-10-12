package io.github.zero88.schedulerx.trigger.predicate;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.json.JsonObject;

interface ExtraPropertiesExtension<T extends ExtraPropertiesExtension<T>> {

    /**
     * Load extra properties into this instance
     *
     * @param properties the extra properties
     * @return a reference to this for fluent API
     * @apiNote This method aims to help deserializing the json data to a desired instance
     */
    @NotNull T load(@NotNull Map<String, Object> properties);

    /**
     * @return an extra data
     * @apiNote This method aims to help serializing this instance to json data
     */
    @Nullable JsonObject extra();

}
