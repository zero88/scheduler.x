package io.github.zero88.schedulerx.custom;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutionContext;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class HttpClientTask implements Task {

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void execute(@NotNull JobData jobData, @NotNull TaskExecutionContext executionContext) {
        final Vertx vertx = executionContext.vertx();
        JsonObject url = (JsonObject) jobData.get();
        vertx.createHttpClient().request(HttpMethod.GET, url.getString("host"), url.getString("path"), ar1 -> {
            if (ar1.succeeded()) {
                HttpClientRequest request = ar1.result();
                request.send(ar2 -> {
                    if (ar2.succeeded()) {
                        HttpClientResponse response = ar2.result();
                        response.body(ar3 -> {
                            if (ar3.succeeded()) {
                                executionContext.complete(new JsonObject().put("status", response.statusCode())
                                                                          .put("response", ar3.result().toJson()));
                            } else {
                                executionContext.fail(ar3.cause());
                            }
                        });
                    } else {
                        executionContext.fail(ar2.cause());
                    }
                });
            } else {
                executionContext.fail(ar1.cause());
            }
        });
    }

}
