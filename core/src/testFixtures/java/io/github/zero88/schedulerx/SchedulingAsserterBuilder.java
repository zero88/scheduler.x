package io.github.zero88.schedulerx;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import io.vertx.junit5.VertxTestContext;

public final class SchedulingAsserterBuilder<OUTPUT> {

    private VertxTestContext testContext;
    private SchedulingMonitor<OUTPUT> logMonitor;
    private Consumer<ExecutionResult<OUTPUT>> unableSchedule;
    private Consumer<ExecutionResult<OUTPUT>> schedule;
    private Consumer<ExecutionResult<OUTPUT>> misfire;
    private Consumer<ExecutionResult<OUTPUT>> each;
    private Consumer<ExecutionResult<OUTPUT>> completed;
    private boolean autoCompleteTest = true;

    /**
     * Set Vertx test context
     *
     * @param testContext test context
     * @return this for fluent API
     * @see VertxTestContext
     */
    public SchedulingAsserterBuilder<OUTPUT> setTestContext(@NotNull VertxTestContext testContext) {
        this.testContext = testContext;
        return this;
    }

    /**
     * Set log monitor
     *
     * @param logMonitor a log monitor
     * @return this for fluent API
     * @see SchedulingMonitor
     * @since 2.0.0
     */
    public SchedulingAsserterBuilder<OUTPUT> setLogMonitor(SchedulingMonitor<OUTPUT> logMonitor) {
        this.logMonitor = logMonitor;
        return this;
    }

    /**
     * Set a job result verification when unable to schedule job
     *
     * @param unableSchedule a verification when unable to schedule job
     * @return this for fluent API
     * @see ExecutionResult
     * @see SchedulingMonitor#onUnableSchedule(ExecutionResult))
     */
    public SchedulingAsserterBuilder<OUTPUT> setUnableSchedule(Consumer<ExecutionResult<OUTPUT>> unableSchedule) {
        this.unableSchedule = unableSchedule;
        return this;
    }

    /**
     * Set a job result verification when schedule job
     *
     * @param schedule a verification when schedule job
     * @return this for fluent API
     * @see ExecutionResult
     * @see SchedulingMonitor#onSchedule(ExecutionResult))
     */
    public SchedulingAsserterBuilder<OUTPUT> setSchedule(Consumer<ExecutionResult<OUTPUT>> schedule) {
        this.schedule = schedule;
        return this;
    }

    /**
     * Set a job result verification when misfire job
     *
     * @param misfire a verification when misfire job
     * @return this for fluent API
     * @see ExecutionResult
     * @see SchedulingMonitor#onMisfire(ExecutionResult))
     */
    public SchedulingAsserterBuilder<OUTPUT> setMisfire(Consumer<ExecutionResult<OUTPUT>> misfire) {
        this.misfire = misfire;
        return this;
    }

    /**
     * Set a job result verification when each round is finished
     *
     * @param each a verification when each round is finished of schedule
     * @return this for fluent API
     * @see ExecutionResult
     * @see SchedulingMonitor#onEach(ExecutionResult)
     */
    public SchedulingAsserterBuilder<OUTPUT> setEach(Consumer<ExecutionResult<OUTPUT>> each) {
        this.each = each;
        return this;
    }

    /**
     * Set a job result verification when execution is completed
     *
     * @param completed a verification when execution is completed
     * @return this for fluent API
     * @see ExecutionResult
     * @see SchedulingMonitor#onCompleted(ExecutionResult))
     */
    public SchedulingAsserterBuilder<OUTPUT> setCompleted(Consumer<ExecutionResult<OUTPUT>> completed) {
        this.completed = completed;
        return this;
    }

    /**
     * By default, when a trigger is completed, the test will be completed automatically also via
     * {@link VertxTestContext#completeNow()}. This method flags the test needs to handle its completeness by itself.
     *
     * @return this for fluent API
     */
    public SchedulingAsserterBuilder<OUTPUT> disableAutoCompleteTest() {
        this.autoCompleteTest = false;
        return this;
    }

    /**
     * Build an asserter
     *
     * @return SchedulingAsserter
     */
    public SchedulingAsserter<OUTPUT> build() {
        return new SchedulingAsserter<>(testContext, logMonitor, unableSchedule, schedule, misfire, each, completed,
                                        autoCompleteTest);
    }

}
