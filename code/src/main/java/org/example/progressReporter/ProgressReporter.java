package org.example.progressReporter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;

public class ProgressReporter implements Runnable {
    private final AtomicLong completedTasks;
    private final long totalTasks;
    private final AtomicLong passwordsFound;
    private final CountDownLatch attackFinishedLatch;
    private long lastReportedCount = 0;
    private static final int REPORT_BATCH_SIZE = 1000; 

    public ProgressReporter(AtomicLong completedTasks, long totalTasks, AtomicLong passwordsFound, CountDownLatch attackFinishedLatch) {
        this.completedTasks = completedTasks;
        this.totalTasks = totalTasks;
        this.passwordsFound = passwordsFound;
        this.attackFinishedLatch = attackFinishedLatch;
    }

    @Override
    public void run() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            while (!attackFinishedLatch.await(10, TimeUnit.MILLISECONDS)) {
                long count = completedTasks.get();
                while (lastReportedCount + REPORT_BATCH_SIZE <= count) {
                    lastReportedCount += REPORT_BATCH_SIZE;
                    printStatus(lastReportedCount, formatter);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Always print final status
            printStatus(completedTasks.get(), formatter);
        }
    }

        private void printStatus(long count, DateTimeFormatter formatter) {
        long remaining = Math.max(0L, totalTasks - count);
        double percent = totalTasks > 0 ? (double) count / totalTasks * 100.0 : 0;
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.printf(
            "\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d    ",
            timestamp, percent, passwordsFound.get(), remaining
        );
    }
}



