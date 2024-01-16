package io.github.zero88.schedulerx.custom;

import java.time.Duration;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.IntervalScheduler;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulingAsserter;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class HttpClientJobTest {

    @Test
    void test_http_job(Vertx vertx, VertxTestContext testContext) {
        final String host = "postman-echo.com";
        final String path = "/get?foo1=bar1&foo2=bar2";
        final Consumer<ExecutionResult<JsonObject>> onEach = result -> {
            JsonObject response = result.data();
            Assertions.assertNotNull(response);
            Assertions.assertEquals(200, response.getValue("status"));
            Assertions.assertTrue(response.getJsonObject("response").getString("url").contains(host + path));
        };
        final SchedulingMonitor<JsonObject> asserter = SchedulingAsserter.<JsonObject>builder()
                                                                         .setTestContext(testContext)
                                                                         .setEach(onEach)
                                                                         .build();
        final JobData<JsonObject> jobData = JobData.create(new JsonObject().put("host", host).put("path", path));
        final IntervalTrigger trigger = IntervalTrigger.builder().interval(Duration.ofSeconds(5)).repeat(2).build();
        IntervalScheduler.<JsonObject, JsonObject>builder()
                         .setVertx(vertx)
                         .setTrigger(trigger)
                         .setJob(new HttpClientJob())
                         .setMonitor(asserter)
                         .setJobData(jobData)
                         .build()
                         .start();
    }

}
