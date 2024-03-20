package io.github.zero88.ratelimit;

public class RateLimitException extends RuntimeException {

    private final RateLimitResult<Object> result;

    public RateLimitException(RateLimitResult<Object> result) { this.result = result; }

    public RateLimitResult<Object> getResult() {
        return result;
    }

}
