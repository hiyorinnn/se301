package org.example.reporter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reports the progress of a task to the console in real-time.
 * Monitors a counter and prints updates using a formatter.
 * Adjusts the reporting interval based on progress speed.
 * Signals completion via a latch when the task finishes or is interrupted.
 */

public class LiveProgressReporter implements Runnable {

    private final AtomicLong counter;
    private final long total;
    private final CountDownLatch latch;
    private final ProgressFormatter formatter;

    private static final long INITIAL_SLEEP_MS = 10L;
    private static final long MAX_SLEEP_MS = 100L;
    private static final long UPDATE_THRESHOLD = 100L;
    private static final int UNCHANGED_LIMIT = 5;

    public LiveProgressReporter(AtomicLong counter, long total, CountDownLatch latch, ProgressFormatter formatter) {
        this.counter = counter;
        this.total = total;
        this.latch = latch;
        this.formatter = formatter;
    }

    @Override
    public void run() {
        try {
            long lastCount = -1;
            long sleepMs = INITIAL_SLEEP_MS;
            int unchangedIterations = 0;

            while (true) {
                long count = counter.get();
                long delta = count - lastCount;

                boolean shouldUpdate = delta >= UPDATE_THRESHOLD || delta > 0 || count >= total;

                if (shouldUpdate) {
                    System.out.print("\r" + formatter.format(count, total));
                    lastCount = count;
                    unchangedIterations = 0;
                    sleepMs = INITIAL_SLEEP_MS;
                } else {
                    unchangedIterations++;
                    if (unchangedIterations > UNCHANGED_LIMIT) {
                        sleepMs = Math.min(sleepMs * 2, MAX_SLEEP_MS);
                    }
                }

                if (count >= total) {
                    System.out.println();
                    break;
                }

                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("\nProgress reporter interrupted.");
                    break;
                }
            }
        } finally {
            latch.countDown();
        }
    }
}
