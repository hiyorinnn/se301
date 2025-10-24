package org.example.app;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.example.io.*;
import org.example.loader.*;
import org.example.model.*;
import org.example.service.*;
import org.example.error.AppException;

// import java.io.IOException;
// import java.security.NoSuchAlgorithmException;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.List;

// import org.example.model.CrackTask;
// import org.example.model.User;

public class DictionaryAttackAppRunner {

    private final Loader<User> userLoader;
    private final Loader<String> dictLoader;
    private static LinkedList<CrackTask> taskQueue = new LinkedList<>();
    private static HashMap<String, User> users = new HashMap<>();
    private static int passwordsFound = 0;
    private static int hashesComputed = 0;

    public DictionaryAttackAppRunner(Loader<User> userLoader, Loader<String> dictLoader, Hasher hasher, ResultWriter<User> resultWriter) {
        this.userLoader = userLoader;
        this.dictLoader = dictLoader;

    }

    public void run(String usersPath, String dictPath, String outputPath) throws IOException, AppException {
        userLoader.load(usersPath);
        dictLoader.load(dictPath);
    }
    
    private static List<String> loadDictionary(String path) throws AppException {
        DictionaryLoader loader = new DictionaryLoader();
        return loader.load(path);
    }
    
    private static void loadUsers(String path) throws AppException {
        UserLoader loader = new UserLoader();
        List<User> userList = loader.load(path);
        users.clear();
        for (User user : userList) {
            users.put(user.getUsername(), user);
        }
    }

    
    private static void writeCrackedPasswordsToCSV(String path) throws AppException {
        CsvResultWriter writer = new CsvResultWriter();
        UserLoader userLoader = new UserLoader();
        writer.write(path, userLoader);
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
                taskQueue.add(new CrackTask(user.getUsername(), password));
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

    } catch (AppException e) {
        throw new AppException("Program failed: " + e.getMessage(), e);
    }
}
}
