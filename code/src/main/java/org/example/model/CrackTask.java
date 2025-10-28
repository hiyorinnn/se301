package org.example.model;

import org.example.service.Hasher;

import org.example.error.AppException;
import org.example.service.Sha256Hasher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

//todo maybe decouple crack-task form dictionaryAttackRunner and Hasher with interface, break them up
public class CrackTask {
//    private final User user;
//    private final String dictionaryPassword;
//    private final Hasher hasher;
////    private static int hashCount;
//    private static final AtomicLong hashCount = new AtomicLong(0);
//
//    public CrackTask(List<String> dictionary) {
//        this.user = user;
//        this.dictionaryPassword = dictionaryPassword;
//        this.hasher = hasher;
//    }
//
//    public boolean execute() throws AppException {
//        if (user.isFound()) return false;
//
//        String hash = hasher.hash(dictionaryPassword);
//        hashCount.incrementAndGet();
//
//        if (hash.equals(user.getHashedPassword())) {
//            user.markFound(dictionaryPassword);
//            return true;
//        }
//        return false;
//    }
//
//    public static long getCount() {
//        return hashCount.get(); // Use the thread-safe get method
//    }
    private final List<String> dictionary;
    private final Hasher hasher;
    // Configurable thread pool size
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    // modified to have dependency injection for better flexibility
    public CrackTask(List<String> dictionary, Hasher hasher) {
        this.dictionary = dictionary;
        this.hasher = hasher;
    }

    //todo: Add getters and setters, maybe split up the method if possible

    // todo idk whether to make to generic


    public Map<String, String> buildHashLookupTable() throws AppException {
        // Use ConcurrentHashMap for thread-safe parallel insertion
        Map<String, String> hashToPlaintext = new ConcurrentHashMap<>();

        AtomicLong processed = new AtomicLong(0);
        long total = dictionary.size();

        try {
            dictionary.parallelStream().forEach(plaintext -> {
                try {
                    String hash = hasher.hash(plaintext);
                    hashToPlaintext.put(hash, plaintext);

                    // Update progress (use the correct counter!)
                    long count = processed.incrementAndGet();

                    //todo: shift this outtttt
                    if (count % 1000 == 0 || count == total) {
                        double progress = (double) count / total * 100.0;
                        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        System.out.printf(
                                "\r[%s] Hashing progress: %.2f%% (%d/%d)",
                                ts, progress, count, total
                        );
                    }

                } catch (AppException e) {
                    System.err.println("\nWarning: Failed to hash password '" + plaintext + "': " + e.getMessage());
                }
            });
            System.out.println(); // New line after progress
        } catch (Exception e) {
            throw new AppException("Failed to build hash lookup table: " + e.getMessage(), e);
        }

        return hashToPlaintext;
    }


//    public Map<String, String> buildHashLookupTable() throws AppException {
//        // Use ConcurrentHashMap for thread-safe parallel insertion
//        Map<String, String> hashToPlaintext = new ConcurrentHashMap<>();
//
//        // Create ExecutorService with fixed thread pool
//        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
//
//        AtomicLong processed = new AtomicLong(0);
//        long total = dictionary.size();
//
//        // List to collect futures for error handling
//        List<Future<?>> futures = new ArrayList<>();
//
//        try {
//            // Submit tasks for each dictionary entry
//            for (String plaintext : dictionary) {
//                Future<?> future = executor.submit(() -> {
//                    try {
//                        String hash = hasher.hash(plaintext);
//                        hashToPlaintext.put(hash, plaintext);
//
//                        // Update progress
//                        long count = processed.incrementAndGet();
//                        if (count % 10000 == 0 || count == total) {
//                            double progress = (double) count / total * 100.0;
//                            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
//                            System.out.printf("\r[%s] Hashing progress: %.2f%% (%d/%d)",
//                                    ts, progress, count, total);
//                        }
//                    } catch (AppException e) {
//                        System.err.println("\nWarning: Failed to hash password '" + plaintext + "': " + e.getMessage());
//                    }
//                });
//                futures.add(future);
//            }
//
//            // Shutdown executor and wait for all tasks to complete
//            executor.shutdown();
//
//            try {
//                // Wait for all tasks to complete (with timeout)
//                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
//                    executor.shutdownNow();
//                    throw new AppException("Hash computation timed out after 1 hour");
//                }
//
//                // Check for any exceptions in futures
//                for (Future<?> future : futures) {
//                    try {
//                        future.get(); // This will throw if the task threw an exception
//                    } catch (ExecutionException e) {
//                        // Log but continue processing other passwords
//                        System.err.println("\nWarning: Task failed: " + e.getCause().getMessage());
//                    }
//                }
//
//            } catch (InterruptedException e) {
//                executor.shutdownNow();
//                Thread.currentThread().interrupt();
//                throw new AppException("Hash computation was interrupted", e);
//            }
//
//            System.out.println(); // New line after progress
//
//        } catch (Exception e) {
//            executor.shutdownNow();
//            throw new AppException("Failed to build hash lookup table: " + e.getMessage(), e);
//        }
//
//        return hashToPlaintext;
//    }
}