package io.github.zero88.schedulerx;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.junit5.VertxTestContext;

public final class TestUtils {

    private TestUtils() { }

    public static void sleep(int millis, VertxTestContext testContext) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            testContext.failNow(e);
        }
    }

    public static List<Exception> simulateRunActionInParallel(VertxTestContext testContext,
                                                              Runnable action, int nbOfThreads) {
        final List<Exception> store = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < nbOfThreads; i++) {
            new TestWorker("Worker_" + (i + 1), action, latch, store, testContext).start();
        }
        sleep(10, testContext);
        latch.countDown();
        return store;
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
                LOGGER.info("[" + getName() + "] created, blocked by the latch...");
                latch.await();
                LOGGER.info("[" + getName() + "] starts at: " + Instant.now());
                runnable.run();
            } catch (InterruptedException e) {
                testContext.failNow(e);
            } catch (Exception e) {
                this.store.add(e);
            }
        }

    }

}
