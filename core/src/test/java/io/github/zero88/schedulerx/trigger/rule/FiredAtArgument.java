package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;

final class FiredAtArgument {

    final Duration leeway;
    final Instant firedAt;
    final boolean expected;

    private FiredAtArgument(Duration leeway, Instant firedAt, boolean expected) {
        this.leeway   = leeway;
        this.firedAt  = firedAt;
        this.expected = expected;
    }

    static FiredAtArgument of(Instant firedAt, boolean expected) {
        return new FiredAtArgument(null, firedAt, expected);
    }

    static FiredAtArgument of(Duration leeway, Instant firedAt, boolean expectedSatisfied) {
        return new FiredAtArgument(leeway, firedAt, expectedSatisfied);
    }

    @Override
    public String toString() {
        return "{leeway=" + leeway + ", firedAt=" + firedAt + ", expected=" + expected + '}';
    }

}
