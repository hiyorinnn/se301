package org.example.app;

import org.example.error.AppException;

/**
 * Application entry point for running the dictionary attack.
 * Expects three arguments: users file, dictionary file, and output file.
 */
public class DictionaryAttackApp {

    /**
     * Main method.
     * Usage: java -jar <jar> <usersFile> <dictionaryFile> <outputFile>
     */
    public static void main(String[] args) {
        if (args == null || args.length < 3) {
            System.err.println("Usage: java -jar <jar> <usersFile> <dictionaryFile> <outputFile>");
            System.exit(2);
        }

        try {
            AppFactory factory = new AppFactory();
            DictionaryAttackRunner runner = factory.createRunner();
            runner.run(args[0], args[1], args[2]);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted: shutting down.");
            if (System.getenv().containsKey("VERBOSE")) {
                e.printStackTrace();
            }
            System.exit(130);
            
        } catch (AppException e) {
            System.err.println("Error: " + e.getMessage());
            if (System.getenv().containsKey("VERBOSE")) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}