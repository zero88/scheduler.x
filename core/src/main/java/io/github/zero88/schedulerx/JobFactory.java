package io.github.zero88.schedulerx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * Factory for creating Job instances from class names or service loader.
 *
 * @since 2.0.0
 */
public final class JobFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFactory.class);
    private static final Map<String, Job<?, ?>> CACHE = new ConcurrentHashMap<>();

    private JobFactory() {
    }

    /**
     * Get the singleton instance of JobFactory.
     *
     * @return the JobFactory instance
     */
    public static JobFactory getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Create a Job instance from a job class name.
     *
     * @param jobClassName the fully qualified class name of the job
     * @param <I>          type of job input
     * @param <O>          type of job output
     * @return a Job instance
     * @throws IllegalArgumentException if the job class cannot be found or instantiated
     */
    @SuppressWarnings("unchecked")
    public <I, O> Job<I, O> create(@NotNull String jobClassName) {
        try {
            // First check if we have a cached instance
            if (CACHE.containsKey(jobClassName)) {
                LOGGER.debug("Using cached job instance for: " + jobClassName);
                return (Job<I, O>) CACHE.get(jobClassName);
            }

            // Try to load the class
            Class<?> jobClass = Class.forName(jobClassName);

            // Ensure it implements Job interface
            if (!Job.class.isAssignableFrom(jobClass)) {
                throw new IllegalArgumentException("Class " + jobClassName + " does not implement Job interface");
            }

            // Instantiate using default constructor
            Job<I, O> jobInstance = (Job<I, O>) jobClass.getDeclaredConstructor().newInstance();
            LOGGER.debug("Created job instance using default constructor: " + jobClassName);

            // Cache the instance for future use
            CACHE.put(jobClassName, jobInstance);
            return jobInstance;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Job class " + jobClassName + " must have a default constructor", e);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Job class not found: " + jobClassName, e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to instantiate job class: " + jobClassName, e);
        }
    }

    /**
     * Clear the job instance cache.
     */
    public void clearCache() {
        CACHE.clear();
    }

    /**
     * Holder for the singleton instance.
     */
    private static class Holder {
        private static final JobFactory INSTANCE = new JobFactory();
    }
}
