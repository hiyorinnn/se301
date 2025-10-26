package org.example.app;

import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;
import org.example.io.*;
import org.example.error.AppException;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        AtomicLong usersCompleted = new AtomicLong(0);
        AtomicLong hashesComputed = new AtomicLong(0); // For total hash count

        // 1. Load data
        List<User> users = userLoader.load(usersPath);
        List<String> dict = dictLoader.load(dictPath);

        long totalUsers = users.size();
        long dictSize = dict.size();
        long totalPossibleHashes = totalUsers * dictSize;

        System.out.println("Loaded " + totalUsers + " users and " + dictSize + " dictionary words.");
        System.out.println("Total possible hashes: " + totalPossibleHashes);
        System.out.println("Using " + Runtime.getRuntime().availableProcessors() + " available processors...");

        // 2. Run the attack using a parallel stream over the users.
        users.parallelStream().forEach(user -> {
            // If user is already found (e.g., from a previous run), skip.
            if (user.isFound()) {
                usersCompleted.incrementAndGet();
                return;
            }

            try {
                // 3. For each user, iterate through the entire dictionary
                for (String passwordGuess : dict) {
                    hashesComputed.incrementAndGet();

                    // This is the logic from your CrackTask, now inside the stream
                    // It uses your Hasher.hash() and User.getHashedPassword()
                    if (hasher.hash(passwordGuess).equals(user.getHashedPassword())) {
                        user.markFound(passwordGuess); // Use your markFound() method
                        passwordsFound.incrementAndGet();
                        break; // Password found, stop checking this user
                    }
                }
            } catch (AppException e) {
                // Log an error for this specific user but continue with others
                System.err.println("\nFailed to process user " + user.getUsername() + ": " + e.getMessage());
            }

            // 4. Update progress
            long count = usersCompleted.incrementAndGet();
            if (count % 10 == 0 || count == totalUsers) { // Report progress
                double progress = (double) count / totalUsers * 100.0;
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.out.printf("\r[%s] Progress: %.2f%% | Passwords Found: %d | Users Processed: %d/%d",
                        ts, progress, passwordsFound.get(), count, totalUsers);
            }
        });

        // 5. Print final summary
        long duration = System.currentTimeMillis() - start;
        System.out.println(); // Move to a new line after the progress bar
        System.out.println("Attack finished.");
        System.out.println("Total passwords found: " + passwordsFound.get());
        System.out.println("Total hashes computed: " + hashesComputed.get());
        System.out.println("Total time spent (ms): " + duration);

        // 6. Write results
        resultWriter.write(outputPath, users);
    }
}
