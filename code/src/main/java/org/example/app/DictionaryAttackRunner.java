package org.example.app;

import org.example.reporter.ConsoleProgressFormatter;
import org.example.reporter.LiveProgressReporter;
import org.example.store.HashLookupBuilder;
import org.example.loader.LoadService;
import org.example.model.User;
import org.example.threads.ExecutorProvider;
import org.example.threads.SingleThreadProvider;
import org.example.threads.ConfigurableExecutorProvider;
import org.example.io.ResultWriter;
import org.example.cracktask.Crack;
import org.example.error.AppException;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

// Orchestrates a dictionary attack pipeline
public class DictionaryAttackRunner {

    private final LoadService loadService;
    private final ResultWriter resultWriter;
    private final HashLookupBuilder storeHashPwd;
    private final Crack cracker;

    // Constructor injects dependencies for loading, writing, hashing, and cracking
    public DictionaryAttackRunner(LoadService loadService,
                                  ResultWriter resultWriter,
                                  HashLookupBuilder storeHashPwd,
                                  Crack cracker) {
        this.loadService = loadService;
        this.resultWriter = resultWriter;
        this.storeHashPwd = storeHashPwd;
        this.cracker = cracker;
    }

    // Main entry: runs full dictionary attack
    public void run(String usersPath, String dictPath, String outputPath) throws AppException {
        long start = System.currentTimeMillis();

        try {
            // Load users and dictionary
            LoadService.LoadedData data = loadData(usersPath, dictPath);

            // Build hash lookup table with live progress reporting
            Map<String, String> hashToPlaintext = buildHashLookupWithProgress(data.dict());

            // Attempt to crack users' passwords
            long passwordsFound = performCrack(data.users(), hashToPlaintext);

            // Write cracked results to output
            writeResults(outputPath, data.users());

            // Print summary info
            printSummary(data.users().size(), hashToPlaintext.size(), passwordsFound, start);

        } catch (AppException e) {
            System.err.println("Fatal error: " + e.getMessage());
            throw e; 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Warning: Progress reporter was interrupted.");
        }
    }

    // Load users and dictionary using a fixed-thread executor
    private LoadService.LoadedData loadData(String usersPath, String dictPath) throws AppException {
        int numLoadTasks = 2;
        try (ExecutorProvider loadProvider = ConfigurableExecutorProvider.fixedCpuPool(numLoadTasks)) {
            return loadService.load(usersPath, dictPath, loadProvider);
        }
    }

    // Build hash lookup table and report progress live
    private Map<String, String> buildHashLookupWithProgress(Set<String> dict) throws InterruptedException, AppException {
        AtomicLong processed = new AtomicLong(0);
        CountDownLatch reporterLatch = new CountDownLatch(1);

        LiveProgressReporter hashProgress = new LiveProgressReporter(
                processed,
                dict.size(),
                reporterLatch,
                new ConsoleProgressFormatter()
        );

        try (SingleThreadProvider reporterProvider = new SingleThreadProvider()) {
            reporterProvider.submitTask(hashProgress);

            // Build hash -> plaintext map
            Map<String, String> hashToPlaintext = storeHashPwd.buildHashLookupTable(dict, processed);

            // Wait for reporter to finish
            reporterLatch.await();
            return hashToPlaintext;
        }
    }

    // Attempt to crack passwords for all users
    private long performCrack(Set<User> users, Map<String, String> hashToPlaintext) {
        AtomicLong passwordsFound = new AtomicLong(0);
        System.out.println("Starting attack with " + users.size() + " total tasks...");
        cracker.crack(users, hashToPlaintext, passwordsFound);
        return passwordsFound.get();
    }

    // Write final results to specified output path
    private void writeResults(String outputPath, Set<User> users) throws AppException {
        resultWriter.write(outputPath, users);
    }

    // Print summary stats about the attack
    private void printSummary(long totalUsers, long totalHashes, long passwordsFound, long startTime) {
        System.out.println();
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + totalHashes);
        System.out.println("Total time spent (ms): " + (System.currentTimeMillis() - startTime));
    }

}
