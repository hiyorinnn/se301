package org.example.reporter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class ProgressReporter implements Runnable {
    private final AtomicLong counter;
    private final long total;
    private final CountDownLatch latch;

    public ProgressReporter(AtomicLong counter, long total, CountDownLatch latch) {
        this.counter = counter;
        this.total = total;
        this.latch = latch;
    }

    public void run() {
        try {
            while (true) {
                long count = this.counter.get();
                double progress = (double) count / (double) this.total * 100.0;
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.out.printf("\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d", ts, progress, count, this.total);

                // Check if work is done
                if (count >= this.total) {
                    System.out.println(); // Print final newline to not overwrite the 100% line
                    return; // Exit the loop and the run() method
                }

                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("\nProgress reporter was interrupted.");
                    return; // Also exits the loop
                }
            }
        } finally {
            this.latch.countDown();
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
}
