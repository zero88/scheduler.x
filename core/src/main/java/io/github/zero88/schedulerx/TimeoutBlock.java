package io.github.zero88.schedulerx;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.impl.Utils.HumanReadableTimeFormat;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public final class TimeoutBlock {

    private final Vertx vertx;
    private final Duration timeout;

    public TimeoutBlock(Vertx vertx, Duration timeout) {
        this.vertx   = vertx;
        this.timeout = timeout;
    }

    public <T> Promise<T> wrap(@NotNull Promise<T> promise) {
        if (timeout.isNegative() || timeout.isZero()) {
            return promise;
        }
        Future<Void> controller = Future.future(p -> vertx.setTimer(timeout.toMillis(), ignore -> p.complete()));
        Future.any(promise.future(), controller).onSuccess(event -> {
            if (!event.isComplete(0)) {
                promise.fail(new NoStackTraceTimeoutException(timeout));
            }
        });
        return promise;
    }

    static class NoStackTraceTimeoutException extends TimeoutException {

        NoStackTraceTimeoutException(Duration timeout) {
            super(timeout == null ? "Timeout" : "Timeout after " + HumanReadableTimeFormat.format(timeout));
        }

        @Override
        public Throwable fillInStackTrace() { return this; }

    }

}
