package io.github.zero88.schedulerx.custom;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.schedulerx.TaskExecutorAsserter;
import io.github.zero88.schedulerx.TaskResult;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.github.zero88.schedulerx.trigger.IntervalTriggerExecutor;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class HttpClientTaskTest {

    @Test
    void test_http_task(Vertx vertx, VertxTestContext testContext) {
        final Checkpoint checkpoint = testContext.checkpoint(2);
        final String host = "postman-echo.com";
        final String path = "/get?foo1=bar1&foo2=bar2";
        final Consumer<TaskResult<JsonObject>> verification = result -> {
            checkpoint.flag();
            JsonObject response = result.data();
            Assertions.assertNotNull(response);
            Assertions.assertEquals(200, response.getValue("status"));
            Assertions.assertEquals("http://" + host + path, response.getJsonObject("response").getString("url"));
        };
        final TaskExecutorAsserter<JsonObject> asserter = TaskExecutorAsserter.<JsonObject>builder()
                                                                              .setTestContext(testContext)
                                                                              .setEach(verification)
                                                                              .build();
        IntervalTriggerExecutor.<JsonObject, JsonObject>builder()
                               .setVertx(vertx)
                               .setTrigger(IntervalTrigger.builder().interval(3).repeat(2).build())
                               .setTask(new HttpClientTask())
                               .setMonitor(asserter)
                               .setJobData(() -> new JsonObject().put("host", host).put("path", path))
                               .build()
                               .start();
    }

}
