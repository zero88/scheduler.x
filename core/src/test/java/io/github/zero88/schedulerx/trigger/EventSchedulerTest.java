package io.github.zero88.schedulerx.trigger;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.NoopTask;
import io.github.zero88.schedulerx.SchedulingAsserter;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TestUtils;
import io.github.zero88.schedulerx.trigger.TriggerCondition.ReasonCode;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class EventSchedulerTest {

    private static Stream<Arguments> provide_predicate() {
        final Arguments arg1 = arguments(EventTriggerPredicate.<String>create(o -> o.startsWith("CO")),
                                         Arrays.asList(1, "COMPLETED"), (Consumer<ExecutionResult<Void>>) (r) -> {
                final TriggerCondition condition = r.triggerContext().condition();
                final Throwable err = condition.cause();
                Assertions.assertEquals(ReasonCode.CONDITION_IS_NOT_MATCHED, condition.reasonCode());
                Assertions.assertNotNull(err);
                Assertions.assertInstanceOf(ClassCastException.class, err);
            });
        final Arguments arg2 = arguments(EventTriggerPredicate.<String>create("COMPLETED"::equals),
                                         Arrays.asList("Hello", "COMPLETED"),
                                         (Consumer<ExecutionResult<Void>>) (r) -> Assertions.assertEquals(
                                             ReasonCode.CONDITION_IS_NOT_MATCHED,
                                             r.triggerContext().condition().reasonCode()));
        final Arguments arg3 = arguments(new EventTriggerPredicate<String>() {
            @Override
            public String convert(@NotNull MultiMap headers, @Nullable Object body) {
                if ("1".equals(body))
                    throw new RuntimeException("failed in convert");
                return (String) body;
            }

            @Override
            public boolean test(@Nullable String eventMessage) { return true; }

            @Override
            public @NotNull JsonObject toJson() { return new JsonObject(); }
        }, Arrays.asList("1", "COMPLETED"), (Consumer<ExecutionResult<Void>>) (r) -> {
            final TriggerCondition condition = r.triggerContext().condition();
            final Throwable err = condition.cause();
            Assertions.assertEquals(ReasonCode.UNEXPECTED_ERROR, condition.reasonCode());
            Assertions.assertNotNull(err);
            Assertions.assertInstanceOf(RuntimeException.class, err);
        });
        final Arguments arg4 = arguments(EventTriggerPredicate.create(o -> {
            if ("1".equals(o))
                throw new IllegalArgumentException("Throw in test");
            return true;
        }), Arrays.asList("1", "COMPLETED"), (Consumer<ExecutionResult<Void>>) (r) -> {
            final TriggerCondition condition = r.triggerContext().condition();
            final Throwable err = condition.cause();
            Assertions.assertEquals(ReasonCode.UNEXPECTED_ERROR, condition.reasonCode());
            Assertions.assertNotNull(err);
            Assertions.assertInstanceOf(IllegalArgumentException.class, err);
        });
        return Stream.of(arg1, arg2, arg3, arg4);
    }

    @ParameterizedTest
    @MethodSource("provide_predicate")
    void test_event_trigger_misfire(EventTriggerPredicate<String> predicate, List<Object> data,
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
            Assertions.assertEquals(0, result.round());
            validator.accept(result);
        };
        final Consumer<ExecutionResult<Void>> onCompleted = result -> {
            Assertions.assertEquals(2, result.tick());
            Assertions.assertEquals(1, result.round());
        };
        final SchedulingMonitor<Void> asserter = SchedulingAsserter.<Void>builder()
                                                                   .setTestContext(testContext)
                                                                   .setMisfire(onMisfire)
                                                                   .setCompleted(onCompleted)
                                                                   .build();
        EventScheduler.<Void, Void, String>builder()
                      .setVertx(vertx)
                      .setMonitor(asserter)
                      .setTrigger(trigger)
                      .setTask(NoopTask.create(data.size() - 1))
                      .build()
                      .start();
        data.forEach(d -> {
            vertx.eventBus().publish(address, d);
            TestUtils.block(Duration.ofSeconds(1), testContext);
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
            TestUtils.block(Duration.ofSeconds(1), testContext);
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
            TestUtils.block(Duration.ofSeconds(1), testContext);
        });
    }

}
