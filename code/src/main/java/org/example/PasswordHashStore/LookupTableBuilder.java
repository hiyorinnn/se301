package org.example.PasswordHashStore;

import org.example.service.Hasher;

import org.example.error.AppException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

//todo maybe decouple crack-task form dictionaryAttackRunner and Hasher with interface, break them up
public class LookupTableBuilder {
    private final List<String> dictionary;
    private final Hasher hasher;

    // modified to have dependency injection for better flexibility
    public LookupTableBuilder(List<String> dictionary, Hasher hasher) {
        this.dictionary = dictionary;
        this.hasher = hasher;
    }

    //todo: Add getters and setters, maybe split up the method if possible

    // todo idk whether to make to generic


    public Map<String, String> buildHashLookupTable() throws AppException {
        // Use ConcurrentHashMap for thread-safe parallel insertion
        Map<String, String> hashToPlaintext = new ConcurrentHashMap<>();

        AtomicLong processed = new AtomicLong(0);
        long total = dictionary.size();

        try {
            dictionary.parallelStream().forEach(plaintext -> {
                try {
                    String hash = hasher.hash(plaintext);
                    hashToPlaintext.put(hash, plaintext);

                    // Update progress (use the correct counter!)
                    long count = processed.incrementAndGet();

                    //todo: shift this outt
                    if (count % 1000 == 0 || count == total) {
                        double progress = (double) count / total * 100.0;
                        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        System.out.printf(
                                "\r[%s] Hashing progress: %.2f%% (%d/%d)",
                                ts, progress, count, total
                        );
                    }

                } catch (AppException e) {
                    System.err.println("\nWarning: Failed to hash password '" + plaintext + "': " + e.getMessage());
                }
            });
            System.out.println(); // New line after progress
        } catch (Exception e) {
            throw new AppException("Failed to build hash lookup table: " + e.getMessage(), e);
        }

        return hashToPlaintext;
    }
}