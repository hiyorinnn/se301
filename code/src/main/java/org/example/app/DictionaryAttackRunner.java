package org.example.app;

import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;
import org.example.io.*;
import org.example.error.AppException;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

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

        List<User> users;
        List<String> dict;

        // Load both files concurrently using virtual threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<List<User>> usersFuture = executor.submit(() -> userLoader.load(usersPath));
            Future<List<String>> dictFuture = executor.submit(() -> dictLoader.load(dictPath));

            // Wait for both tasks to complete
            users = usersFuture.get();
            dict  = dictFuture.get();

        } catch (ExecutionException e) {
            throw new AppException("Failed during concurrent file loading", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException("Loading interrupted", e);
        }

        Map<String, String> hashToPassword = new HashMap<>();
        long hashesComputed = 0;
        long passwordsFound = 0;
        long totalTasks = dict.size() + users.size();

        System.out.println("Starting attack with " + totalTasks + " total tasks...");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Hash dictionary passwords
        for (String password : dict) {
            try {
                String hash = hasher.hash(password);
                hashToPassword.put(hash, password);
                hashesComputed++;

                if (hashesComputed % 1000 == 0) {
                    long remaining = totalTasks - hashesComputed;
                    double progress = (double) hashesComputed / totalTasks * 100.0;
                    String ts = LocalDateTime.now().format(fmt);
                    System.out.printf(
                        "\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %-6d   ",
                        ts, progress, passwordsFound, remaining
                    );
                }
            } catch (AppException e) {
                throw new AppException("Hashing failed during execution", e);
            }
        }

        long tasksCompleted = hashesComputed;

        // Check user hashes against computed dictionary hashes
        for (User user : users) {
            String foundPassword = hashToPassword.get(user.getHashedPassword());
            tasksCompleted++;

            if (foundPassword != null) {
                user.markFound(foundPassword);
                passwordsFound++;
            }

            if (tasksCompleted % 1000 == 0 || tasksCompleted == totalTasks) {
                long remaining = totalTasks - tasksCompleted;
                double progress = (double) tasksCompleted / totalTasks * 100.0;
                String ts = LocalDateTime.now().format(fmt);
                System.out.printf(
                    "\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %-6d   ",
                    ts, progress, passwordsFound, remaining
                );
            }
        }

        System.out.println();
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + hashesComputed);
        System.out.println("Total time spent (ms): " + (System.currentTimeMillis() - start));

        resultWriter.write(outputPath, users);
    }
}
