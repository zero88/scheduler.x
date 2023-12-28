package io.github.zero88.schedulerx;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import io.vertx.core.Vertx;

@Internal
public interface HasVertx {

    /**
     * Vertx
     *
     * @return vertx
     */
    @NotNull Vertx vertx();

}
