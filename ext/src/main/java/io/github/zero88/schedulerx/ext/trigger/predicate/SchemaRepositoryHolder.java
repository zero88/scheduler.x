package io.github.zero88.schedulerx.ext.trigger.predicate;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.ServiceHelper;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.OutputFormat;
import io.vertx.json.schema.SchemaRepository;

public final class SchemaRepositoryHolder implements SchemaRepositoryProvider {

    private static class Holder {

        private static final SchemaRepositoryHolder INSTANCE = new SchemaRepositoryHolder(
            Optional.ofNullable(ServiceHelper.loadFactoryOrNull(SchemaRepositoryProvider.class))
                    .orElseGet(() -> () -> SchemaRepository.create(
                        new JsonSchemaOptions().setBaseUri("https://example.com").setOutputFormat(OutputFormat.Flag))));

    }

    public static SchemaRepositoryHolder getInstance() {
        return SchemaRepositoryHolder.Holder.INSTANCE;
    }

    private final SchemaRepositoryProvider provider;

    private SchemaRepositoryHolder(SchemaRepositoryProvider provider) { this.provider = provider; }

    @Override
    public @NotNull SchemaRepository get() { return this.provider.get(); }

}
