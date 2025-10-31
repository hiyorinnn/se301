package org.example.store;

import org.example.hash.Hasher;

import org.example.error.AppException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LookupTableBuilder implements StoreHashPassword {
    private final Hasher hasher;

    // modified to have dependency injection for better flexibility
    public LookupTableBuilder(Hasher hasher) {
        this.hasher = hasher;
    }

    public Map<String, String> buildHashLookupTable(Set<String> dictionary, AtomicLong processed) throws AppException {
        Map<String, String> hashToPlaintext = new ConcurrentHashMap<>();

        try {
            dictionary.parallelStream().forEach(plaintext -> {
                try {
                    // Hash password store into lookup table
                    String hash = hasher.hash(plaintext);
                    hashToPlaintext.put(hash, plaintext);

                    // Increment live reporter's counter
                    processed.incrementAndGet();

                } catch (AppException e) {
                    System.err.println("\nWarning: Failed to hash password '" + plaintext + "': " + e.getMessage());
                }
            });

        } catch (Exception e) {
            throw new AppException("Failed to build hash lookup table: " + e.getMessage(), e);
        }

        return hashToPlaintext;
    }

}