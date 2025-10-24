package org.example.app;

import java.io.IOException;
import org.example.io.*;
import org.example.loader.*;
import org.example.service.*;
import org.example.model.User;

// import java.io.*;
// import java.nio.file.*;
// import java.security.*;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.nio.charset.StandardCharsets;

public class DictionaryAttackApp {

    // static LinkedList<CrackTask> taskQueue = new LinkedList<>();
    // static HashMap<String, User> users = new HashMap<>();
    // static ArrayList<String> cracked = new ArrayList<>();
    // static HashMap<String, String> reverseLookupCache = new HashMap<>();
    // static int passwordsFound = 0;
    // static int hashesComputed = 0;

    public static void main(String[] args) {

        if (args == null || args.length < 3 ) {
            System.err.println("Usage: java -jar <jar> <usersFile> <dictionaryFile> <outputFile>");
            System.exit(2);
        }
        
        Loader<User> userLoader = new UserLoader();
        Loader<String> dictLoader = new DictionaryLoader();
        Hasher hasher = new Sha256Hasher();
        ResultWriter<User> resultWriter = new CsvResultWriter();

        DictionaryAttackAppRunner runner = new DictionaryAttackAppRunner(userLoader, dictLoader, hasher, resultWriter);

        try {
            runner.run(args[0], args[1], args[2]);
            //to add more exception with Exception the last
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
}

