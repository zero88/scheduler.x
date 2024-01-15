package io.github.zero88.schedulerx.custom;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.ExecutionContext;
import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class HttpClientJob implements Job<JsonObject, JsonObject> {

    @Override
    public boolean isAsync() { return true; }

    @Override
    public void execute(@NotNull JobData<JsonObject> jobData, @NotNull ExecutionContext<JsonObject> executionContext) {
        doExecute(executionContext.vertx(), jobData).onSuccess(executionContext::complete)
                                                    .onFailure(executionContext::fail);
    }

    private Future<JsonObject> doExecute(Vertx vertx, @NotNull JobData<JsonObject> jobData) {
        JsonObject config = jobData.get();
        return vertx.createHttpClient()
                    .request(HttpMethod.GET, config.getString("host"), config.getString("path"))
                    .map(req -> req.setFollowRedirects(true))
                    .flatMap(HttpClientRequest::send)
                    .flatMap(response -> response.body()
                                                 .map(ar3 -> new JsonObject().put("status", response.statusCode())
                                                                             .put("response", ar3.toJson())));
    }

}
