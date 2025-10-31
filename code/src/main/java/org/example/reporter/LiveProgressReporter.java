package org.example.reporter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

// A class that reports the progress of a task in real-time to the console.
// It monitors an atomic counter and prints updates using a custom formatter
// until the task is complete.
public class LiveProgressReporter implements Runnable {

    // The counter that tracks the current progress.
    private final AtomicLong counter;

    // The total value representing task completion.
    private final long total;

    // Latch used to signal when reporting has finished.
    private final CountDownLatch latch;

    // Formatter used to generate the progress display string.
    private final ProgressFormatter formatter;

    // Constructs a LiveProgressReporter with the given counter, total, latch, and formatter.
    // counter: the AtomicLong counter tracking progress.
    // total: the target value for completion.
    // latch: the CountDownLatch to signal completion.
    // formatter: the ProgressFormatter to format the progress output.
    public LiveProgressReporter(AtomicLong counter, long total, CountDownLatch latch, ProgressFormatter formatter) {
        this.counter = counter;
        this.total = total;
        this.latch = latch;
        this.formatter = formatter;
    }

    // Starts the progress reporting loop.
    // Continuously prints progress updates to the console whenever the counter changes.
    // Stops when the counter reaches or exceeds total or if the thread is interrupted.
    // Always decrements the latch at the end to signal completion.
    @Override
    public void run() {
        try {
            long lastCount = -1;

            while (true) {
                long count = counter.get();

                // Only print if the count has changed since the last iteration
                if (count != lastCount) {
                    System.out.print("\r" + formatter.format(count, total)); // overwrite current line
                    lastCount = count;
                }

                // Stop reporting if progress is complete
                if (count >= total) {
                    System.out.println(); // print final newline
                    break;
                }

                try {
                    // Sleep briefly to reduce CPU usage
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    // Restore interrupt status and exit
                    Thread.currentThread().interrupt();
                    System.err.println("\nProgress reporter was interrupted.");
                    break;
                }
            }
        } finally {
            // Ensure the latch is always decremented even if an exception occurs
            latch.countDown();
        }
    }
}
