package org.example.app;

import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;
import org.example.io.*;
import org.example.error.AppException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DictionaryAttackRunner {

    private final Loader<User> userLoader;
    private final Loader<String> dictLoader;
    private final Hasher hasher;
    private final ResultWriter resultWriter;

    public DictionaryAttackRunner(Loader<User> userLoader,
            Loader<String> dictLoader,
            Hasher hasher,
            ResultWriter resultWriter) {
        this.userLoader = userLoader;
        this.dictLoader = dictLoader;
        this.hasher = hasher;
        this.resultWriter = resultWriter;
    }

    public void run(String usersPath, String dictPath, String outputPath) throws AppException {
        long start = System.currentTimeMillis();

        List<User> users = userLoader.load(usersPath);
        List<String> dict = dictLoader.load(dictPath);

        AtomicLong hashesComputed = new AtomicLong(0);
        AtomicLong passwordsFound = new AtomicLong(0);
        long totalTasks = (long) users.size() * dict.size();

        System.out.println("Starting attack with " + totalTasks + " total tasks...");
        // Start the progress reporter thread
        Thread reporter = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                long computed = hashesComputed.get();
                if (computed >= totalTasks)
                    break;
                long remaining = totalTasks - computed;
                double progress = (double) computed / totalTasks * 100.0;
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                System.out.printf("\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d",
                        ts, progress, passwordsFound.get(), remaining);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        reporter.start();

        // parallel cracking
        users.parallelStream().forEach(user -> {
            for (String pwd : dict) {
                try {
                    hashesComputed.incrementAndGet();
                    if (new CrackTask(user, pwd, hasher).execute()) {
                        // If you only ever call .execute() for one (user, pwd) at a time,
                        // synchronization is not needed
                        passwordsFound.incrementAndGet();
                    }
                } catch (AppException ignored) {
                }
            }
        });

        // Finish up and shut down reporter thread
        try {
            reporter.interrupt();
            reporter.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException("Thread interrupted while finalizing progress output", e);
        }
        // long totalTasks = queue.size();
        // long hashesComputed = 0;
        // long passwordsFound = 0;

        // while (!queue.isEmpty()) {
        // CrackTask task = queue.poll();
        // try {
        // if (task.execute()) {
        // passwordsFound++;
        // }
        // hashesComputed++;
        // } catch (AppException e) {
        // // In this loop, hashing exceptions are fatal — wrap and rethrow to stop the
        // run.
        // throw new AppException("Hashing failed during execution", e);
        // }

        // if (hashesComputed % 1000 == 0) {
        // long remaining = queue.size();
        // long completed = totalTasks - remaining;
        // double progress = (double) completed / totalTasks * 100.0;
        // String ts =
        // LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd
        // HH:mm:ss"));
        // System.out.printf("\r[%s] %.2f%% complete | Passwords Found: %d | Tasks
        // Remaining: %d",
        // ts, progress, passwordsFound, remaining);
        // }
        // }

        // int threads = Runtime.getRuntime().availableProcessors();
        // ExecutorService executor = Executors.newFixedThreadPool(threads);

        // List<Future<?>> futures = new ArrayList<>();

        // for (CrackTask task : queue) {
        // futures.add(executor.submit(() -> {
        // try {
        // if (task.execute()) {
        // passwordsFound.incrementAndGet();
        // }
        // long computed = hashesComputed.incrementAndGet();

        // // print every 1000 hashes for progress
        // if (computed % 1000 == 0) {
        // long remaining = totalTasks - computed;
        // double progress = (double) computed / totalTasks * 100.0;
        // String ts = LocalDateTime.now()
        // .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // System.out.printf(
        // "\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d",
        // ts, progress, passwordsFound.get(), remaining);
        // }

        // } catch (AppException e) {
        // System.err.println("Task failed: " + e.getMessage());
        // }
        // }));
        // }

        // // wait for all tasks
        // for (Future<?> f : futures) {
        // try {
        // f.get();
        // } catch (Exception e) {
        // throw new AppException("Error in parallel execution", e);
        // }
        // }

        // executor.shutdown();
        // try {
        // executor.awaitTermination(1, TimeUnit.HOURS);
        // } catch (InterruptedException e) {
        // Thread.currentThread().interrupt(); // restore interrupt flag
        // throw new AppException("Thread interrupted while waiting for termination",
        // e);
        // }

        System.out.println();
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + hashesComputed);
        System.out.println("Total time spent (ms): " + (System.currentTimeMillis() - start));

        // Write results
        resultWriter.write(outputPath, users);
    }
}
