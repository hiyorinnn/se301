package org.example.progressReporter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class ProgressReporter implements Runnable {
    private final AtomicLong counter;
    private final long total;
    // private final String title;
    private volatile boolean running = true;

    public ProgressReporter(AtomicLong counter, long total) {
        this.counter = counter;
        this.total = total;
        // this.title = title;
    }

    @Override
    public void run() {
        while (running) {
        long count = counter.get();
        double progress = (double) count / total * 100.0;
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.printf("\r[%s] %.2f%% (%d/%d)", ts, progress, count, total);
        // System.out.printf("\r[%s] %s: %.2f%% (%d/%d)", ts, progress, count,total);
        if (count >= total) break;
        try {
        Thread.sleep(500);
        } catch (InterruptedException e) {
        break;
        }
        }
        // while (running) {
        //     long count = counter.get();
        //     long remainingTasks = total - count;
        //     double progressPercent = (double) count / total * 100.0;
        //     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //     String timestamp = LocalDateTime.now().format(formatter);

        //     // Match printing style from your task processing loop
        //     System.out.printf(
        //             "\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d",
        //             timestamp, progressPercent, passwordsFound.get(), remainingTasks);

        //     if (count >= total)
        //         break;

        //     try {
        //         Thread.sleep(500);
        //     } catch (InterruptedException e) {
        //         break;
        //     }
        // }

        System.out.println(); // New line after final progress
    }

    public void stop() {
        this.running = false;
    }
}
