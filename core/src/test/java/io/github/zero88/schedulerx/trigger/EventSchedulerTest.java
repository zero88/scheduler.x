package io.github.zero88.schedulerx.trigger;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.NoopTask;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.SchedulingAsserter;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class EventSchedulerTest {

    private static Stream<Arguments> provide_invalid_interval() {
        return Stream.of(arguments(EventTriggerPredicate.any(), Arrays.asList(1, "COMPLETED"),
                                   (Consumer<ExecutionResult<Void>>) (r) -> {
                                       Throwable e = r.error();
                                       Assertions.assertTrue(r.isError());
                                       Assertions.assertNotNull(e);
                                       Assertions.assertInstanceOf(ClassCastException.class, e);
                                       Assertions.assertTrue(e.getMessage()
                                                              .contains(
                                                                  "java.lang.Integer cannot be cast to java.lang" +
                                                                  ".String"));
                                   }), arguments(EventTriggerPredicate.<String>create("COMPLETED"::equals),
                                                 Arrays.asList("Hello", "COMPLETED"),
                                                 (Consumer<ExecutionResult<Void>>) (r) -> { }));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ParameterizedTest
    @MethodSource("provide_invalid_interval")
    void test_event_trigger_misfire_when_event_info_is_not_match(EventTriggerPredicate predicate, List<Object> sendData,
                                                                 Consumer<ExecutionResult<Void>> validator, Vertx vertx,
                                                                 VertxTestContext testContext) {
        final String address = "schedulerx.event.1";
        final EventTrigger<String> trigger = EventTrigger.<String>builder()
                                                         .localOnly(true)
                                                         .address(address)
                                                         .predicate(predicate)
                                                         .build();
        final Consumer<ExecutionResult<Void>> onMisfire = result -> {
            Assertions.assertEquals(1, result.tick());
            validator.accept(result);
        };
        final SchedulingMonitor<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                   .setTestContext(testContext)
                                                                   .setMisfire(onMisfire)
                                                                   .build();
        EventScheduler.<Void, Void, String>builder()
                      .setVertx(vertx)
                      .setMonitor(asserter)
                      .setTrigger(trigger)
                      .setTask(NoopTask.create(sendData.size() - 1))
                      .build()
                      .start();
        sendData.forEach(d -> {
            vertx.eventBus().publish(address, d);
            TestUtils.sleep(1000, testContext);
        });
    }

    @Test
    void test_run_task_when_receive_event(Vertx vertx, VertxTestContext testContext) {
        final int message = 10;
        final int totalRound = 4;
        final Consumer<ExecutionResult<String>> each = result -> {
            final long data = message + (result.round() - 1);
            if (result.round() < 3) {
                Assertions.assertTrue(result.isError());
                Assertions.assertNotNull(result.error());
                Assertions.assertInstanceOf(RuntimeException.class, result.error());
                Assertions.assertEquals("Runtime: " + data, result.error().getMessage());
            }
            if (result.round() == 3) {
                Assertions.assertFalse(result.isError());
                Assertions.assertEquals(String.valueOf(data), result.data());
            }
        };
        final SchedulingAsserter<String> asserter = SchedulingAsserter.<String>builder()
                                                                      .setTestContext(testContext)
                                                                      .setEach(each)
                                                                      .build();
        final String address = "schedulerx.event.2";
        final EventTrigger<Object> trigger = EventTrigger.builder()
                                                         .address(address)
                                                         .predicate(EventTriggerPredicate.any())
                                                         .build();
        final Task<Void, String> task = (jobData, ctx) -> {
            final Object info = ctx.triggerContext().info();
            final long round = ctx.round();
            if (round == 1) {
                throw new RuntimeException("Runtime: " + info);
            }
            if (round == 2) {
                ctx.fail(new RuntimeException("Runtime: " + info));
            }
            if (round == 3) {
                ctx.complete(String.valueOf(info));
            }
            if (round == 4) {
                ctx.forceStopExecution();
            }
        };
        EventScheduler.<Void, String, Object>builder()
                      .setVertx(vertx)
                      .setMonitor(asserter)
                      .setTrigger(trigger)
                      .setTask(task)
                      .build()
                      .start();
        for (int i = 0; i < totalRound; i++) {
            TestUtils.sleep(1000, testContext);
            vertx.eventBus().publish(address, message + i);
        }
    }

    @Test
    void test_event_trigger_can_handle_msg_with_various_datatype(Vertx vertx, VertxTestContext testContext) {
        final String address = "schedulerx.event.3";
        final EventTriggerPredicate<Object> predicate = EventTriggerPredicate.ignoreType(
            d -> (d == "string" || d == Integer.valueOf(1) || Objects.equals(d, JsonArray.of("1", 2))));
        final EventTrigger<Object> trigger = EventTrigger.builder().address(address).predicate(predicate).build();
        final List<Object> data = Arrays.asList("string", 1, JsonArray.of("1", 2));
        final int totalEvent = data.size();

        final Consumer<ExecutionResult<Void>> onCompleted = r -> {
            Assertions.assertEquals(totalEvent, r.round());
            Assertions.assertEquals(totalEvent, r.tick());
        };
        final SchedulingMonitor<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                   .setTestContext(testContext)
                                                                   .setCompleted(onCompleted)
                                                                   .build();
        EventScheduler.<Void, Void, Object>builder()
                      .setVertx(vertx)
                      .setMonitor(asserter)
                      .setTrigger(trigger)
                      .setTask(NoopTask.create(totalEvent))
                      .build()
                      .start();
        data.forEach(d -> {
            vertx.eventBus().publish(address, d);
            TestUtils.sleep(1000, testContext);
        });
    }

}
