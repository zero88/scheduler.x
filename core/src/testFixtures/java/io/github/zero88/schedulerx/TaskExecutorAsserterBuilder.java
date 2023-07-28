package io.github.zero88.schedulerx;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import io.vertx.junit5.VertxTestContext;

public class TaskExecutorAsserterBuilder<OUTPUT> {

    private VertxTestContext testContext;
    private TaskExecutorMonitor<OUTPUT> logMonitor;
    private Consumer<TaskResult<OUTPUT>> unableSchedule;
    private Consumer<TaskResult<OUTPUT>> schedule;
    private Consumer<TaskResult<OUTPUT>> misfire;
    private Consumer<TaskResult<OUTPUT>> each;
    private Consumer<TaskResult<OUTPUT>> completed;

    /**
     * Set Vertx test context
     *
     * @param testContext test context
     * @return this for fluent API
     * @see VertxTestContext
     */
    public TaskExecutorAsserterBuilder<OUTPUT> setTestContext(@NotNull VertxTestContext testContext) {
        this.testContext = testContext;
        return this;
    }

    /**
     * Set log monitor
     *
     * @param logMonitor a log monitor
     * @return this for fluent API
     * @see TaskExecutorMonitor
     * @since 2.0.0
     */
    public TaskExecutorAsserterBuilder<OUTPUT> setLogMonitor(TaskExecutorMonitor<OUTPUT> logMonitor) {
        this.logMonitor = logMonitor;
        return this;
    }

    /**
     * Set a task result verification when unable to schedule task
     *
     * @param unableSchedule a verification when unable to schedule task
     * @return this for fluent API
     * @see TaskResult
     * @see TaskExecutorMonitor#onUnableSchedule(TaskResult))
     */
    public TaskExecutorAsserterBuilder<OUTPUT> setUnableSchedule(Consumer<TaskResult<OUTPUT>> unableSchedule) {
        this.unableSchedule = unableSchedule;
        return this;
    }

    /**
     * Set a task result verification when schedule task
     *
     * @param schedule a verification when schedule task
     * @return this for fluent API
     * @see TaskResult
     * @see TaskExecutorMonitor#onSchedule(TaskResult))
     */
    public TaskExecutorAsserterBuilder<OUTPUT> setSchedule(Consumer<TaskResult<OUTPUT>> schedule) {
        this.schedule = schedule;
        return this;
    }

    /**
     * Set a task result verification when misfire task
     *
     * @param misfire a verification when misfire task
     * @return this for fluent API
     * @see TaskResult
     * @see TaskExecutorMonitor#onMisfire(TaskResult))
     */
    public TaskExecutorAsserterBuilder<OUTPUT> setMisfire(Consumer<TaskResult<OUTPUT>> misfire) {
        this.misfire = misfire;
        return this;
    }

    /**
     * Set a task result verification when each round is finished
     *
     * @param each a verification when each round is finished of schedule
     * @return this for fluent API
     * @see TaskResult
     * @see TaskExecutorMonitor#onEach(TaskResult)
     */
    public TaskExecutorAsserterBuilder<OUTPUT> setEach(Consumer<TaskResult<OUTPUT>> each) {
        this.each = each;
        return this;
    }

    /**
     * Set a task result verification when execution is completed
     *
     * @param completed a verification when execution is completed
     * @return this for fluent API
     * @see TaskResult
     * @see TaskExecutorMonitor#onCompleted(TaskResult))
     */
    public TaskExecutorAsserterBuilder<OUTPUT> setCompleted(Consumer<TaskResult<OUTPUT>> completed) {
        this.completed = completed;
        return this;
    }

    /**
     * Build an asserter
     *
     * @return TaskExecutorAsserter
     */
    public TaskExecutorAsserter<OUTPUT> build() {
        return new TaskExecutorAsserter<>(testContext, logMonitor, unableSchedule, schedule, misfire, each, completed);
    }

}
