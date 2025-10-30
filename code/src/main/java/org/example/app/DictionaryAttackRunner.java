package org.example.app;

import org.example.loader.*;
import org.example.model.*;
import org.example.progressReporter.ProgressReporter;
import org.example.threads.ExecutorProvider;
import org.example.threads.VirtualExecutorProvider;
import org.example.io.*;
import org.example.CrackTask.CrackTask;
import org.example.PasswordHashStore.DictionaryHashTask;
import org.example.error.AppException;
import org.example.hash.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class DictionaryAttackRunner {

    private final LoadService loadService;
    private final Hasher hasher;
    private final ResultWriter resultWriter;

    public DictionaryAttackRunner(LoadService loadService,
            Hasher hasher,
            ResultWriter resultWriter) {
        this.loadService = loadService;
        this.hasher = hasher;
        this.resultWriter = resultWriter;
    }

    public void run(String usersPath, String dictPath, String outputPath) throws AppException {
        long start = System.currentTimeMillis();

        // Atomic counters for thread-safe reporting
        AtomicLong passwordsFound = new AtomicLong(0);
        AtomicLong completedTasks = new AtomicLong(0);

        // 1. Load data (single thread)
        ExecutorProvider ioProvider = new VirtualExecutorProvider();
        LoadService.LoadedData data = loadService.load(usersPath, dictPath, ioProvider);

        List<User> users = data.users();
        List<String> dict = data.dict();
        AtomicLong processed = new AtomicLong(0);
        long total = dict.size();
        CountDownLatch attackFinishedLatch = new CountDownLatch(1);

        System.out.println("Starting attack with " + total + " total tasks...");

        // 1. Start a progress reporter thread
        ProgressReporter reporter = new ProgressReporter(completedTasks, total, passwordsFound, attackFinishedLatch);
        Thread reporterThread = new Thread(reporter);
        reporterThread.start();

        // 2. Build hash lookup table
        DictionaryHashTask hasherTask = new DictionaryHashTask(dict, hasher);
        Map<String, String> hashToPlaintext = hasherTask.buildHashLookupTable(processed);

        // 3. Crack
        CrackTask cracker = new CrackTask(users, hashToPlaintext, passwordsFound);
        cracker.crack();

        attackFinishedLatch.countDown();   
        try {         
            reporterThread.join();       
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Reporter thread interrupted during shutdown.");

        // 4. Print final summary todo: seperate this
        System.out.println();
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + hashToPlaintext.size());
        System.out.println("Total time spent (ms): " + (System.currentTimeMillis() - start));

        // 5. Write results
        resultWriter.write(outputPath, data.users());
    }

    }}
