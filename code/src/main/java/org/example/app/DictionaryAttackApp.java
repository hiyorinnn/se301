package org.example.app;

import org.example.error.AppException;

public class DictionaryAttackApp {

    public static void main(String[] args) {
        if (args == null || args.length < 3) {
            System.err.println("Usage: java -jar <jar> <usersFile> <dictionaryFile> <outputFile>");
            System.exit(2);
        }

        try {
            var runner = AppFactory.createRunner();
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