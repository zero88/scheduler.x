package io.github.zero88.schedulerx.manager;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface Manager {

    Future<Void> load(JsonObject data);

}
