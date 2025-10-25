package org.example.app;

import org.example.error.AppException;
import org.example.loader.*;
import org.example.service.*;
import org.example.io.*;

public class DictionaryAttackApp {

    public static void main(String[] args) {
        if (args == null || args.length < 3) {
            System.err.println("Usage: java -jar <jar> <usersFile> <dictionaryFile> <outputFile>");
            System.exit(2);
        }

        // Create concrete implementations (could be wired by DI framework)
        var userLoader = new UserLoader();
        var dictLoader = new DictionaryLoader();
        var hasher = new Sha256Hasher();
        var resultWriter = new CsvResultWriter();

        var runner = new DictionaryAttackRunner(userLoader, dictLoader, hasher, resultWriter);

        try {
            runner.run(args[0], args[1], args[2]);
        } catch (AppException e) {
            System.err.println("Error: " + e.getMessage());
            if (System.getenv().containsKey("VERBOSE")) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}
