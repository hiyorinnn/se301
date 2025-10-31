package org.example.reporter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ProgressReporter is a runnable task that monitors and displays the progress
 * of a long-running operation. It periodically prints the percentage of completion,
 * the number of tasks completed, and the total number of tasks. 
 * 
 * It uses an AtomicLong counter to track progress and a CountDownLatch to signal
 * when the reporting has finished. The reporter stops when the work is complete
 * or if the thread is interrupted.
 */
public class ProgressReporter implements Runnable {

    // Counter tracking the number of completed tasks
    private final AtomicLong counter;

    // Total number of tasks expected to complete
    private final long total;

    // Latch to signal completion of the reporter
    private final CountDownLatch latch;

    /**
     * Constructs a ProgressReporter with the given counter, total, and latch.
     * The counter is used to monitor progress, total is the target number of tasks,
     * and the latch is decremented when reporting finishes.
     */
    public ProgressReporter(AtomicLong counter, long total, CountDownLatch latch) {
        this.counter = counter;
        this.total = total;
        this.latch = latch;
    }

    /**
     * Continuously reports progress to the console. Displays a timestamp,
     * percentage complete, tasks completed, and total tasks. Stops when
     * the counter reaches the total or if the thread is interrupted.
     * The latch is decremented when the reporter finishes.
     */
    @Override
    public void run() {
        try {
            while (true) {
                long count = this.counter.get();
                double progress = (double) count / (double) this.total * 100.0;
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                System.out.printf("\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d", 
                                  ts, progress, count, this.total);

                if (count >= this.total) {
                    System.out.println(); // print final newline
                    return;
                }

                try {
                    Thread.sleep(1L); // sleep briefly to reduce CPU usage
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("\nProgress reporter was interrupted.");
                    return;
                }
            }
        } finally {
            this.latch.countDown();
        }
    }
}
//    @Override
//    public void run() {
//        try {
//            while (true) {
//                long count = this.counter.get();
//                double progress = (double) count / total * 100.0;
//                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
//                System.out.printf("\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d", ts, progress, count, this.total);
//
//                if (count >= total) {
//                    System.out.println(); // Final newline
//                    return;
//                }
//
//                Thread.sleep(1L);
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            System.err.println("\nProgress reporter was interrupted.");
//        } finally {
//            System.out.println(); // Ensure newline on any exit
//        }
//    }

