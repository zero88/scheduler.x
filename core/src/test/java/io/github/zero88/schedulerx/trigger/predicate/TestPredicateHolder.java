package io.github.zero88.schedulerx.trigger.predicate;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

class TestPredicateHolder {

    static class MockForbiddenFilter implements EventTriggerPredicate.MessageFilter<Object> {

        @Override
        public boolean test(Object eventMessage) { return false; }

        @Override
        public int hashCode() {
            return MockForbiddenFilter.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj.getClass() == MockForbiddenFilter.class;
        }

    }


    static class MockNullConverter implements EventTriggerPredicate.MessageConverter<Object> {

        @Override
        public Object apply(MultiMap headers, Object body) { return body; }

        @Override
        public int hashCode() {
            return MockNullConverter.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj.getClass() == MockNullConverter.class;
        }

    }


    static class MockUnableDeserializeEventTrigger implements EventTriggerPredicate<Object> {

        @Override
        public @Nullable Object convert(@NotNull MultiMap headers, @Nullable Object body) { return null; }

        @Override
        public boolean test(@Nullable Object eventMessage) { return false; }

        @Override
        public @NotNull JsonObject toJson() { return new JsonObject(); }

    }


    static class MockEventTriggerExtensionPredicate implements EventTriggerExtensionPredicate<Object> {

        Map<String, Object> extra;

        @Override
        public @Nullable Object convert(@NotNull MultiMap headers, @Nullable Object body) { return null; }

        @Override
        public boolean test(@Nullable Object eventMessage) { return false; }

        @Override
        public @NotNull JsonObject toJson() { return new JsonObject(); }

        @Override
        public @NotNull EventTriggerExtensionPredicate<Object> load(@Nullable Map<String, Object> properties) {
            this.extra = properties;
            return this;
        }

    }


    static class FailedEventTriggerExtensionPredicate extends MockEventTriggerExtensionPredicate {

        @Override
        public @NotNull EventTriggerExtensionPredicate<Object> load(@Nullable Map<String, Object> properties) {
            throw new RuntimeException("simulate to failed");
        }

    }

}
