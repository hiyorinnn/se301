package org.example.PasswordHashStore;

import org.example.error.AppException;
import org.example.hash.Hasher;
import org.example.progressReporter.ProgressReporter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

//todo maybe decouple crack-task form dictionaryAttackRunner and Hasher with interface, break them up
public class DictionaryHashTask {
    private final List<String> dictionary;
    private final Hasher hasher;
    // Configurable thread pool size
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    // modified to have dependency injection for better flexibility
    public DictionaryHashTask(List<String> dictionary, Hasher hasher) {
        this.dictionary = dictionary;
        this.hasher = hasher;
    }

    // todo: Add getters and setters, maybe split up the method if possible

    // todo idk whether to make to generic

    public Map<String, String> buildHashLookupTable() throws AppException {
        Map<String, String> hashToPlaintext = new ConcurrentHashMap<>();
        AtomicLong processed = new AtomicLong(0);
        long total = dictionary.size();

        // 1. Start a progress reporter thread
        ProgressReporter progress = new ProgressReporter(processed, total);
        Thread reporter = new Thread(progress);
        reporter.start();

        try {
            dictionary.parallelStream().forEach(plaintext -> {
                try {
                    String hash = hasher.hash(plaintext);
                    hashToPlaintext.put(hash, plaintext);
                    processed.incrementAndGet();
                } catch (AppException e) {
                    System.err.println("\nWarning: Failed to hash password '" + plaintext + "': " + e.getMessage());
                }
            });
            // 2. Wait for the reporter to finish
            reporter.interrupt();
            try {
                reporter.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AppException("Reporter interrupted while finalizing progress.", e);
            }
        } catch (Exception e) {
            throw new AppException("Failed to build hash lookup table: " + e.getMessage(), e);
        }

        return hashToPlaintext;
    }

}