package org.example.app;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.example.io.*;
import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;

// import java.io.IOException;
// import java.security.NoSuchAlgorithmException;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.List;

// import org.example.model.CrackTask;
// import org.example.model.User;

public class DictionaryAttackAppRunner {

    private final Loader userLoader;
    private final Loader dictLoader;
    private final Hasher hasher;
    private final ResultWriter resultWriter;

    public DictionaryAttackAppRunner(Loader userLoader, Loader dictLoader, Hasher hasher, ResultWriter resultWriter) {
        this.userLoader = userLoader;
        this.dictLoader = dictLoader;
        this.hasher = hasher;
        this.resultWriter = resultWriter;
    }

    public void run(String usersPath, String dictPath, String outputPath) throws IOException {

        long start = System.currentTimeMillis();
        List<User> users = userLoader.load(usersPath);
        List<String> dict = dictLoader.load(dictPath);
    }
    
    public static void runProgram(String[] args) throws AppException {
    if (args.length < 3) {
        throw new AppException("Usage: java -jar <jar-file-name>.jar <input_file> <dictionary_file> <output_file>");
    }

    String usersPath = args[0];
    String dictionaryPath = args[1];
    String passwordsPath = args[2];

    long start = System.currentTimeMillis();

    try {
        List<String> allPasswords = loadDictionary(dictionaryPath);
        loadUsers(usersPath);

        for (User user : users.values()) {
            for (String password : allPasswords) {
                taskQueue.add(new CrackTask(user.username, password));
            }
        }

        long totalTasks = taskQueue.size();
        System.out.println("Starting attack with " + totalTasks + " total tasks...");

        while (!taskQueue.isEmpty()) {
            CrackTask task = taskQueue.poll();
            if (task != null) task.execute();

            if (taskQueue.size() % 1000 == 0) {
                long remainingTasks = taskQueue.size();
                long completedTasks = totalTasks - remainingTasks;
                double progressPercent = (double) completedTasks / totalTasks * 100;
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                System.out.printf("\r[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d",
                        timestamp, progressPercent, passwordsFound, remainingTasks);
            }
        }

        System.out.println();
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + hashesComputed);
        System.out.println("Total time spent (ms): " + (System.currentTimeMillis() - start));

        if (passwordsFound > 0) {
            writeCrackedPasswordsToCSV(passwordsPath);
        }

    } catch (IOException | NoSuchAlgorithmException e) {
        throw new AppException("Program failed: " + e.getMessage(), e);
    }
}
}
