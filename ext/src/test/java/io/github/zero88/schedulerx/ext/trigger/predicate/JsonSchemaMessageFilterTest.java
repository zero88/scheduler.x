package io.github.zero88.schedulerx.ext.trigger.predicate;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.NoopJob;
import io.github.zero88.schedulerx.SchedulingAsserter;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.trigger.EventScheduler;
import io.github.zero88.schedulerx.trigger.EventTrigger;
import io.github.zero88.schedulerx.trigger.predicate.AutoCastMessageBody;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate.MessageFilter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class JsonSchemaMessageFilterTest {

    JsonObject schema2 = new JsonObject(
        "{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\",\"$id\":\"https://example.com/product.schema" +
        ".json\",\"title\":\"Product\",\"description\":\"A product from Acme's catalog\",\"type\":\"object\"," +
        "\"properties\":{\"productId\":{\"description\":\"The unique identifier for a product\"," +
        "\"type\":\"integer\"},\"productName\":{\"description\":\"Name of the product\",\"type\":\"string\"}," +
        "\"price\":{\"description\":\"The price of the product\",\"type\":\"number\",\"exclusiveMinimum\":0}," +
        "\"tags\":{\"description\":\"Tags for the product\",\"type\":\"array\",\"items\":{\"type\":\"string\"}," +
        "\"minItems\":1,\"uniqueItems\":true},\"dimensions\":{\"type\":\"object\"," +
        "\"properties\":{\"length\":{\"type\":\"number\"},\"width\":{\"type\":\"number\"}," +
        "\"height\":{\"type\":\"number\"}},\"required\":[\"length\",\"width\",\"height\"]}," +
        "\"warehouseLocation\":{\"description\":\"Coordinates of the warehouse where the product is located.\"," +
        "\"$ref\":\"https://example.com/geographical-location.schema.json\"}},\"required\":[\"productId\"," +
        "\"productName\",\"price\"]}");

    @Test
    void test_event_trigger_can_handle_msg_with_various_datatype(Vertx vertx, VertxTestContext testContext) {
        final String address = "schedulerx.event.3";
        //https://json-schema.github.io/json-schema/example1.html
        final JsonObject schema1 = new JsonObject(
            "{\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"Product\",\"description\":\"A " +
            "product from Acme's catalog\",\"type\":\"object\",\"properties\":{\"id\":{\"description\":\"The unique " +
            "identifier for a product\",\"type\":\"integer\"},\"name\":{\"description\":\"Name of the product\"," +
            "\"type\":\"string\"},\"price\":{\"type\":\"number\",\"minimum\":0,\"exclusiveMinimum\":true}," +
            "\"tags\":{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"minItems\":1,\"uniqueItems\":true}}," +
            "\"required\":[\"id\",\"name\",\"price\"]}");
        final JsonObject filterData = JsonObject.of("schema", schema1, "id", "hey");
        final MessageFilter<JsonObject> filter = new JsonSchemaMessageFilter<JsonObject>().load(filterData.getMap());
        final EventTriggerPredicate<JsonObject> predicate = EventTriggerPredicate.create(AutoCastMessageBody.create(),
                                                                                         filter);
        final EventTrigger<JsonObject> trigger = EventTrigger.<JsonObject>builder()
                                                             .address(address)
                                                             .predicate(predicate)
                                                             .build();
        final List<JsonObject> data = Collections.singletonList(
            new JsonObject("{\"id\":1,\"name\":\"A green door\",\"price\":12.50,\"tags\":[\"home\",\"green\"]}"));
        final int totalEvent = data.size();

        final Consumer<ExecutionResult<Void>> onCompleted = r -> {
            Assertions.assertEquals(totalEvent, r.round());
            Assertions.assertEquals(totalEvent, r.tick());
        };
        final SchedulingMonitor<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                   .setTestContext(testContext)
                                                                   .setCompleted(onCompleted)
                                                                   .build();
        EventScheduler.<Void, Void, JsonObject>builder()
                      .setVertx(vertx)
                      .setMonitor(asserter)
                      .setTrigger(trigger)
                      .setJob(NoopJob.create(totalEvent))
                      .build()
                      .start();
        data.forEach(d -> vertx.eventBus().publish(address, d));
    }

}
