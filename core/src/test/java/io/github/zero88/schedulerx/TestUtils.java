package io.github.zero88.schedulerx;

import static io.github.zero88.schedulerx.impl.Utils.brackets;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.junit5.VertxTestContext;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class TestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

    private TestUtils() { }

    @SuppressWarnings("java:S2925")
    public static void block(Duration duration, VertxTestContext testContext) {
        try {
            LOGGER.info("Doing a mock stuff in " + brackets(duration) + "...");
            TimeUnit.MILLISECONDS.sleep(duration.toMillis());
            LOGGER.info("Wake up after " + brackets(duration) + "!!!");
        } catch (InterruptedException e) {
            testContext.failNow(e);
        }
    }

    public static List<Exception> simulateRunActionInParallel(VertxTestContext testContext, Runnable action,
                                                              int nbOfThreads) {
        final List<Exception> store = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < nbOfThreads; i++) {
            new TestWorker("Worker_" + (i + 1), action, latch, store, testContext).start();
        }
        block(Duration.ofMillis(10), testContext);
        latch.countDown();
        return store;
    }

    public static ObjectMapper defaultMapper() {
        return DatabindCodec.mapper()
                            .copy()
                            .findAndRegisterModules()
                            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                            .setSerializationInclusion(Include.NON_NULL);
    }

    public static class TestWorker extends Thread {

        protected static final Logger LOGGER = LoggerFactory.getLogger(TestWorker.class);

        private final CountDownLatch latch;
        private final Runnable runnable;
        private final VertxTestContext testContext;
        private final List<Exception> store;

        public TestWorker(String name, Runnable runnable, CountDownLatch latch, List<Exception> store,
                          VertxTestContext testContext) {
            this.latch       = latch;
            this.runnable    = runnable;
            this.testContext = testContext;
            this.store       = store;
            setName(name);
        }

        @Override
        public void run() {
            try {
                LOGGER.info(brackets(getName()) + " created, blocked by the latch...");
                latch.await();
                LOGGER.info(brackets(getName()) + " started at" + brackets(Instant.now()));
                runnable.run();
            } catch (InterruptedException e) {
                testContext.failNow(e);
            } catch (Exception e) {
                this.store.add(e);
            }
        }

    }

}
