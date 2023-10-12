package io.github.zero88.schedulerx.ext.trigger.predicate;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import io.vertx.json.schema.SchemaRepository;

public interface SchemaRepositoryProvider extends Supplier<SchemaRepository> {

    /**
     * @return the schema repository
     */
    @Override
    @NotNull SchemaRepository get();

}
