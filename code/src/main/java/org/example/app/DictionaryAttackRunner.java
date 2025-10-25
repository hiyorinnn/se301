package org.example.app;

import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;
import org.example.io.*;
import org.example.error.AppException;

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

        // Use instance-level structures (no static state)
        Map<String, String> hashToPassword = new HashMap<>();
        long hashesComputed = 0;
        long passwordsFound = 0;
        long tasksCompleted = 0;
        long totalTasks = users.size() * dict.size(); // need to change according to algo?? or maintain same output as original??

        System.out.println("Starting attack with " + totalTasks + " total tasks...");

        for (String password : dict) {
            try {
                String hash = hasher.hash(password);
                hashToPassword.put(hash, password);
                hashesComputed++;
                
                tasksCompleted += users.size();
                
                if (hashesComputed % 1000 == 0 || hashesComputed == dict.size()) {
                    long remaining = totalTasks - tasksCompleted;
                    double progress = (double) tasksCompleted / totalTasks * 100.0;
                    String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    System.out.printf("\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d",
                            ts, progress, passwordsFound, remaining);
                }
            } catch (AppException e) {
                // In this loop, hashing exceptions are fatal — wrap and rethrow to stop the run.
                throw new AppException("Hashing failed during execution", e);
            }
        }

        for (User user : users) {
            String userHash = user.getHashedPassword();
            String foundPassword = hashToPassword.get(userHash);
            
            if (foundPassword != null) {
                user.markFound(foundPassword);
                passwordsFound++;
            }
            
            if (passwordsFound % 1000 == 0 || passwordsFound == users.size()) {
                long remaining = totalTasks - tasksCompleted;
                double progress = (double) tasksCompleted / totalTasks * 100.0;
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                System.out.printf("\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d",
                        ts, progress, passwordsFound, remaining);
            }
        }

        System.out.println();
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + hashesComputed);
        System.out.println("Total time spent (ms): " + (System.currentTimeMillis() - start));

        // Write results
        resultWriter.write(outputPath, users);
    }
}
