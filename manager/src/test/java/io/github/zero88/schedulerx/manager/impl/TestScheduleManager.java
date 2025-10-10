package io.github.zero88.schedulerx.manager.impl;

import io.github.zero88.schedulerx.SchedulingMonitor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * A test implementation of AbstractScheduleManager that returns predefined data.
 */
class TestScheduleManager extends AbstractScheduleManager {

    private JsonObject dataToReturn;

    TestScheduleManager(Vertx vertx) {
        super(vertx);
    }

    TestScheduleManager(Vertx vertx, SchedulingMonitor<Object> monitor) {
        super(vertx, monitor);
    }

    /**
     * Set the data that should be returned by loadData().
     */
    void setDataToReturn(JsonObject dataToReturn) {
        this.dataToReturn = dataToReturn;
    }

    @Override
    protected Future<JsonObject> loadData() {
        // Return predefined data for testing
        return Future.succeededFuture(dataToReturn != null ? dataToReturn : new JsonObject());
    }
}
