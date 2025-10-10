package io.github.zero88.schedulerx;

import static io.github.zero88.schedulerx.impl.Utils.brackets;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JobFactoryTest {

    @BeforeEach
    void setUp() {
        JobFactory.getInstance().clearCache();
    }

    @Test
    void test_create_job_with_default_constructor() {
        Job<String, String> job = JobFactory.getInstance().create(TestJobWithDefaultConstructor.class.getName());

        assertNotNull(job);
        assertInstanceOf(TestJobWithDefaultConstructor.class, job);
    }

    @Test
    void test_caching() {
        Job<String, String> job1 = JobFactory.getInstance().create(TestJobWithDefaultConstructor.class.getName());
        Job<String, String> job2 = JobFactory.getInstance()
                                             .create("io.github.zero88.schedulerx" +
                                                     ".JobFactoryTest$TestJobWithDefaultConstructor");

        assertNotNull(job1);
        assertNotNull(job2);
        assertSame(job1, job2, "Should return cached instance");
    }

    @ParameterizedTest
    // @formatter:off
    @CsvSource({
        "non.existent.JobClass,Job class not found",
        "io.github.zero88.schedulerx.TestUtils,does not implement Job interface",
        "io.github.zero88.schedulerx.JobFactoryTest$TestJobWithWithoutDefaultConstructor,must have a default constructor"
    })
    // @formatter:on
    void test_fail_to_create_job(String jobClassName, String expectedMessage) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> JobFactory.getInstance().create(jobClassName));
        String exceptionMessage = exception.getMessage();
        String causeMessage = exception.getCause() != null ? exception.getCause().getMessage() : "";
        assertTrue(exceptionMessage.contains(expectedMessage) || causeMessage.contains(expectedMessage),
                   "Expected error message to contain: " + brackets(expectedMessage) + ", but got: " +
                   brackets(exceptionMessage) + " and cause: " + brackets(causeMessage));
    }

    static class TestJobWithDefaultConstructor implements Job<String, String> {

        @Override
        public void execute(@NotNull JobData<String> jobData, @NotNull ExecutionContext<String> executionContext) {
            executionContext.complete("Default job executed");
        }

    }


    static class TestJobWithWithoutDefaultConstructor implements Job<String, String> {

        private final String arg;

        public TestJobWithWithoutDefaultConstructor(String arg) { this.arg = arg; }

        @Override
        public void execute(@NotNull JobData<String> jobData, @NotNull ExecutionContext<String> executionContext) {
            executionContext.complete("Default job executed: " + arg);
        }

    }

}
