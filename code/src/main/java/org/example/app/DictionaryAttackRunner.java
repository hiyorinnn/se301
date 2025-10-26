package org.example.app;

import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;
import org.example.io.*;
import org.example.error.AppException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DictionaryAttackRunner {

    private final Loader<User> userLoader;
    private final Loader<String> dictLoader;
    private final Hasher hasher;
    private final ResultWriter resultWriter;
    // Configurable thread pool size
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();


    // (?)to-do: Change into di, for more flexibility
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

        // Atomic counters for thread-safe reporting
        AtomicLong passwordsFound = new AtomicLong(0);
        AtomicLong usersProcessed = new AtomicLong(0);

        // 1. Load data (single thread)
        List<User> users = userLoader.load(usersPath);
        List<String> dict = dictLoader.load(dictPath);

        long totalUsers = users.size();

        // 2. Pre-compute all dictionary hashes (M operations instead of N×M)
        long hashStart = System.currentTimeMillis();

        Map<String, String> hashToPlaintext = buildHashLookupTable(dict);

        long hashDuration = System.currentTimeMillis() - hashStart;

        // 3. Phase 2: Lookup user passwords in pre-computed hash table
        long lookupStart = System.currentTimeMillis();

        // to-do: is parallelStream() overkill here?
        users.parallelStream().forEach(user -> {
            // Skip if already found
            if (user.isFound()) {
                usersProcessed.incrementAndGet();
                return;
            }

            // O(1) lookup in the hash table
            String plainPassword = hashToPlaintext.get(user.getHashedPassword());

            if (plainPassword != null) {
                synchronized (user) {
                    user.markFound(plainPassword);
                }
                passwordsFound.incrementAndGet();
            }

            // Update progress
            long count = usersProcessed.incrementAndGet();
            if (count % 1000 == 0 || count == totalUsers) {
                double progress = (double) count / totalUsers * 100.0;
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.out.printf("\r[%s] Progress: %.2f%% | Passwords Found: %d | Users Processed: %d/%d",
                        ts, progress, passwordsFound.get(), count, totalUsers);
            }

        });

        long lookupDuration = System.currentTimeMillis() - lookupStart;

        // 4. Print final summary
        System.out.println();
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + hashToPlaintext.size());
        System.out.println("Total time spent (ms): " + (System.currentTimeMillis() - start));


        // 5. Write results
        resultWriter.write(outputPath, users);
    }


//    private Map<String, String> buildHashLookupTable(List<String> dictionary) throws AppException {
//        // Use ConcurrentHashMap for thread-safe parallel insertion
//        Map<String, String> hashToPlaintext = new ConcurrentHashMap<>();
//
//        AtomicLong processed = new AtomicLong(0);
//        long total = dictionary.size();
//
//        try {
//            dictionary.parallelStream().forEach(plaintext -> {
//                try {
//                    String hash = hasher.hash(plaintext);
//                    hashToPlaintext.put(hash, plaintext);
//                } catch (AppException e) {
//                    System.err.println("\nWarning: Failed to hash password '" + plaintext + "': " + e.getMessage());
//                }
//            });
//            System.out.println(); // New line after progress
//        } catch (Exception e) {
//            throw new AppException("Failed to build hash lookup table: " + e.getMessage(), e);
//        }
//
//        return hashToPlaintext;
//    }


    private Map<String, String> buildHashLookupTable(List<String> dictionary) throws AppException {
        // Use ConcurrentHashMap for thread-safe parallel insertion
        Map<String, String> hashToPlaintext = new ConcurrentHashMap<>();

        // Create ExecutorService with fixed thread pool
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        AtomicLong processed = new AtomicLong(0);
        long total = dictionary.size();

        // List to collect futures for error handling
        List<Future<?>> futures = new ArrayList<>();

        try {
            // Submit tasks for each dictionary entry
            for (String plaintext : dictionary) {
                Future<?> future = executor.submit(() -> {
                    try {
                        String hash = hasher.hash(plaintext);
                        hashToPlaintext.put(hash, plaintext);

                        // Update progress
                        long count = processed.incrementAndGet();
                        if (count % 10000 == 0 || count == total) {
                            double progress = (double) count / total * 100.0;
                            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            System.out.printf("\r[%s] Hashing progress: %.2f%% (%d/%d)",
                                    ts, progress, count, total);
                        }
                    } catch (AppException e) {
                        System.err.println("\nWarning: Failed to hash password '" + plaintext + "': " + e.getMessage());
                    }
                });
                futures.add(future);
            }

            // Shutdown executor and wait for all tasks to complete
            executor.shutdown();

            try {
                // Wait for all tasks to complete (with timeout)
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    executor.shutdownNow();
                    throw new AppException("Hash computation timed out after 1 hour");
                }

                // Check for any exceptions in futures
                for (Future<?> future : futures) {
                    try {
                        future.get(); // This will throw if the task threw an exception
                    } catch (ExecutionException e) {
                        // Log but continue processing other passwords
                        System.err.println("\nWarning: Task failed: " + e.getCause().getMessage());
                    }
                }

            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                throw new AppException("Hash computation was interrupted", e);
            }

            System.out.println(); // New line after progress

        } catch (Exception e) {
            executor.shutdownNow();
            throw new AppException("Failed to build hash lookup table: " + e.getMessage(), e);
        }

        return hashToPlaintext;
    }

}
