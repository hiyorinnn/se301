package org.example.app;

import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;
import org.example.io.*;
import org.example.error.AppException;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class DictionaryAttackRunner {

    private final Loader<User> userLoader;
    private final Loader<String> dictLoader;
    private final Hasher hasher;
    private final ResultWriter resultWriter;


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
        System.out.println("Starting attack run...");

        // Atomic counters for thread-safe reporting
        AtomicLong passwordsFound = new AtomicLong(0);
        AtomicLong usersProcessed = new AtomicLong(0);

        // 1. Load data
        List<User> users = userLoader.load(usersPath);
        List<String> dict = dictLoader.load(dictPath);

        long totalUsers = users.size();
        long dictSize = dict.size();

        System.out.println("Loaded " + totalUsers + " users and " + dictSize + " dictionary words.");
        System.out.println("Using " + Runtime.getRuntime().availableProcessors() + " available processors...");

        // 2. Pre-compute all dictionary hashes (M operations instead of N×M)
        System.out.println("\nPhase 1: Pre-computing dictionary hashes...");
        long hashStart = System.currentTimeMillis();

        Map<String, String> hashToPlaintext = buildHashLookupTable(dict);

        long hashDuration = System.currentTimeMillis() - hashStart;
        System.out.println("Pre-computed " + hashToPlaintext.size() + " hashes in " + hashDuration + "ms");
        System.out.println("Total hashes computed: " + hashToPlaintext.size());

        // 3. Phase 2: Lookup user passwords in pre-computed hash table
        System.out.println("\nPhase 2: Looking up user passwords...");
        long lookupStart = System.currentTimeMillis();

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

            // Update progress every 100 users to reduce contention
            long count = usersProcessed.incrementAndGet();
            if (count % 100 == 0 || count == totalUsers) {
                double progress = (double) count / totalUsers * 100.0;
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.out.printf("\r[%s] Progress: %.2f%% | Passwords Found: %d | Users Processed: %d/%d",
                        ts, progress, passwordsFound.get(), count, totalUsers);
            }
        });

        long lookupDuration = System.currentTimeMillis() - lookupStart;

        // 4. Print final summary
        long totalDuration = System.currentTimeMillis() - start;
        System.out.println("\n\nAttack finished.");
        System.out.println("Total passwords found: " + passwordsFound.get());
        System.out.println("Total hashes computed: " + hashToPlaintext.size());
        System.out.println("Hash computation time: " + hashDuration + "ms");
        System.out.println("Lookup time: " + lookupDuration + "ms");
        System.out.println("Total time: " + totalDuration + "ms");

        // Calculate efficiency improvement
        long naiveHashCount = totalUsers * dictSize;
        double improvement = (double) naiveHashCount / hashToPlaintext.size();
        System.out.printf("Efficiency: Computed %d hashes instead of %d (%.1fx improvement)\n",
                hashToPlaintext.size(), naiveHashCount, improvement);

        // 5. Write results
        resultWriter.write(outputPath, users);
    }


    private Map<String, String> buildHashLookupTable(List<String> dictionary) throws AppException {
        // Use ConcurrentHashMap for thread-safe parallel insertion
        Map<String, String> hashToPlaintext = new ConcurrentHashMap<>();

        AtomicLong processed = new AtomicLong(0);
        long total = dictionary.size();

        try {
            dictionary.parallelStream().forEach(plaintext -> {
                try {
                    String hash = hasher.hash(plaintext);
                    hashToPlaintext.put(hash, plaintext);

                    // Progress reporting every 10,000 hashes
                    long count = processed.incrementAndGet();
                    if (count % 10000 == 0 || count == total) {
                        double progress = (double) count / total * 100.0;
                        System.out.printf("\r  Hashing progress: %.2f%% (%d/%d)",
                                progress, count, total);
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
}
