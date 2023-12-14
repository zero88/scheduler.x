package io.github.zero88.schedulerx;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.impl.Utils.HumanReadableTimeFormat;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * Represents for a helper to create a timeout block.
 *
 * @since 2.0.0
 */
public final class TimeoutBlock {

    private final Vertx vertx;
    private final Duration timeout;

    public TimeoutBlock(Vertx vertx, Duration timeout) {
        this.vertx   = vertx;
        this.timeout = timeout;
    }

    /**
     * Wrap a given promise with the definition timeout
     *
     * @param promise promise
     * @param <T>     type of promise result
     * @return a promise. if timeout, the promise fails by {@link TimeoutException}
     */
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

        NoStackTraceTimeoutException(@NotNull Duration timeout) {
            super("Timeout after " + HumanReadableTimeFormat.format(timeout));
        }

        @Override
        public synchronized Throwable fillInStackTrace() { return this; }

    }

}
