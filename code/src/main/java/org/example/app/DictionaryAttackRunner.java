package org.example.app;

import org.example.CrackTask.Crack;
import org.example.reporter.ProgressMonitor;
import org.example.reporter.ProgressReporter;
import org.example.store.StoreHashPassword;
import org.example.loader.LoadService;
import org.example.model.User;
import org.example.threads.ExecutorProvider;
import org.example.threads.ConfigurableExecutorProvider;
import org.example.io.ResultWriter;
import org.example.error.AppException;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Orchestrates a dictionary attack:
 * 1) loads users and dictionary,
 * 2) builds a hash -> plaintext lookup table,
 * 3) looks up each user's hash,
 * 4) reports progress and writes results.
 */
public class DictionaryAttackRunner {

    private final LoadService loadService;
    private final ResultWriter resultWriter;
    private final StoreHashPassword storeHashPwd;
    private final Crack cracker;

    public DictionaryAttackRunner(LoadService loadService,
            ResultWriter resultWriter, StoreHashPassword storeHashPwd, Crack cracker) {
        this.loadService = loadService;
        this.resultWriter = resultWriter;
        this.storeHashPwd = storeHashPwd;
        this.cracker = cracker;
    }

    /**
     * Runs the dictionary attack pipeline:
     * - Loads data (using an executor for I/O),
     * - Builds the hash lookup table while reporting progress,
     * - Performs lookups to mark found passwords,
     * - Prints a summary and writes results to the output path.
     *
     * Any AppException is handled and reported; interruption of the reporter sets the thread interrupt flag.
     */
    public void run(String usersPath, String dictPath, String outputPath) throws AppException {
        long start = System.currentTimeMillis();

        // 1. Load data (Platform Thread)
        LoadService.LoadedData data;
        try (ExecutorProvider ioProvider = ConfigurableExecutorProvider.fixedCpuPool(2)) {
            data = loadService.load(usersPath, dictPath, ioProvider);
        }

        // Now safe to use
        Set<User> users = data.users();
        Set<String> dict = data.dict();

        // Set-up progress reporting
        CountDownLatch reporterLatch = new CountDownLatch(1);
        AtomicLong processed = new AtomicLong(0);
        long totalHashes = dict.size();
        ProgressReporter hashProgress = new ProgressReporter(processed, totalHashes, reporterLatch);

        try {
            Map<String, String> hashToPlaintext;

            try (ProgressMonitor monitor = new ProgressMonitor(hashProgress)) {
                // 2. instantiate CrackTask
                hashToPlaintext = storeHashPwd.buildHashLookupTable(dict, processed);

                // Wait for the reporter thread to print its final "100%" and exit.
                reporterLatch.await();
            }

            // Print the start of attack
            long totalUsers = users.size();
            System.out.println("Starting attack with " + totalUsers + " total tasks...");

            // 3. Lookup
            AtomicLong passwordsFound = new AtomicLong(0);
            cracker.crack(users, hashToPlaintext, passwordsFound);

            // 4. Print final summary (use .get() to read the AtomicLong value)
            System.out.println();
            System.out.println("Total passwords found: " + passwordsFound.get());
            System.out.println("Total hashes computed: " + hashToPlaintext.size());
            System.out.println("Total time spent (ms): " + (System.currentTimeMillis() - start));

            // 5. Write results
            resultWriter.write(outputPath, data.users());
        } catch (AppException e) {
            System.err.println("Fatal error: " + e.getMessage());
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Warning: Progress reporter was interrupted.");
        }
    }
}
