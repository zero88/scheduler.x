package io.github.zero88.schedulerx.ext.trigger.predicate;

import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.predicate.EventTriggerExtensionPredicate.MessageExtensionFilter;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate.EventTriggerPredicateException;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.OutputFormat;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.Validator;

@Experimental
@SuppressWarnings("unchecked")
public final class JsonSchemaMessageFilter<T> implements MessageExtensionFilter<T> {

    private JsonSchema jsonSchema;
    private JsonSchemaOptions options;

    @Override
    public boolean test(T eventMessage) {
        final Validator validator = SchemaRepositoryHolder.getInstance().get().validator(jsonSchema);
        final OutputUnit result = validator.validate(eventMessage);
        if (Boolean.TRUE.equals(result.getValid())) {
            return true;
        }
        if (OutputFormat.Basic == options.getOutputFormat()) {
            throw new EventTriggerPredicateException(result.toJson());
        }
        return false;
    }

    @NotNull
    @Override
    public JsonSchemaMessageFilter<T> load(@NotNull Map<String, Object> props) {
        Object id = props.get("id");
        JsonObject schema = loadSchema(Objects.requireNonNull(props.get("schema"), "'schema' is required"));
        if (id instanceof String) {
            jsonSchema = JsonSchema.of((String) id, schema);
        } else {
            jsonSchema = JsonSchema.of(schema);
        }
        options = new JsonSchemaOptions(loadOptions(props.get("options")));
        SchemaRepositoryHolder.getInstance().get().dereference(jsonSchema);
        return this;
    }

    @Override
    public @NotNull JsonObject extra() {
        return JsonObject.of("schema", jsonSchema, "options", options.toJson());
    }

    private static JsonObject loadOptions(Object options) {
        if (options instanceof JsonObject) { return (JsonObject) options; }
        if (options instanceof Map) { return new JsonObject((Map<String, Object>) options); }
        return new JsonObject();
    }

    private JsonObject loadSchema(@NotNull Object theSchema) {
        if (theSchema instanceof JsonObject) { return (JsonObject) theSchema; }
        if (theSchema instanceof Map) {
            try {
                return new JsonObject((Map<String, Object>) theSchema);
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException(
                    "Unsupported the 'schema' type, got: " + theSchema.getClass().getName());
            }
        }
        throw new IllegalArgumentException("Unsupported the 'schema' type, got: " + theSchema.getClass().getName());
    }

}
