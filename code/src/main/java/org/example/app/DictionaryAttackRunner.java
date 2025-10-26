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
        Deque<CrackTask> queue = new ArrayDeque<>();
        for (User u : users) {
            for (String pwd : dict) {
                queue.add(new CrackTask(u, pwd, hasher));
            }
        }

        long totalTasks = queue.size();
        System.out.println("Starting attack with " + totalTasks + " total tasks...");

        long hashesComputed = 0;
        long passwordsFound = 0;

        while (!queue.isEmpty()) {
            CrackTask task = queue.poll();

            try {
                if (task.execute()) {
                    passwordsFound++;
                }
                hashesComputed++;
            } catch (AppException e) {
                // In this loop, hashing exceptions are fatal — wrap and rethrow to stop the run.
                throw new AppException("Hashing failed during execution", e);
            }

            if (hashesComputed % 1000 == 0) {
                long remaining = queue.size();
                long completed = totalTasks - remaining;
                double progress = (double) completed / totalTasks * 100.0;
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
