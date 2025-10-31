// to add method for each "task"
//import title change to specific

package org.example.app;

import org.example.CrackTask.Crack;
import org.example.reporter.ProgressMonitor;
import org.example.store.StoreHashPassword;
import org.example.loader.*;
import org.example.model.*;
import org.example.reporter.ProgressReporter;
import org.example.threads.ExecutorProvider;
import org.example.io.*;
import org.example.error.AppException;
import org.example.threads.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

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

    public void run(String usersPath, String dictPath, String outputPath) throws AppException {
        long start = System.currentTimeMillis();

        // 1. Load data (Virtual)
        // ExecutorProvider ioProvider = new VirtualExecutorProvider();
        // LoadService.LoadedData data = loadService.load(usersPath, dictPath, ioProvider);

        // 1. Load data (Platform Thread)
        LoadService.LoadedData data;
        try (ExecutorProvider ioProvider = ConfigurableExecutorProvider.fixedCpuPool()) {
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

                // Wait for the reporter thread to print its final "100%" and exit, used latch for better consistency
                reporterLatch.await();
            }

            // Print the start of attack
            long totalUsers = users.size();
            System.out.println("Starting attack with " + totalUsers + " total tasks...");

            // 3. Lookup
            AtomicLong passwordsFound = new AtomicLong(0);
            cracker.crack(users, hashToPlaintext, passwordsFound);

            // 4. Print final summary
            System.out.println();
            System.out.println("Total passwords found: " + passwordsFound);
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
