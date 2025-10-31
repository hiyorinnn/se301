package org.example.store;

import org.example.hash.Hasher;
import org.example.error.AppException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Builds a lookup table that maps password hashes to their plaintext values.
 * Uses parallel processing for efficiency and supports dependency-injected hashing.
 */
public class LookupTableBuilder implements StoreHashPassword {
    private final Hasher hasher;

    // Allows injecting a custom hasher implementation
    public LookupTableBuilder(Hasher hasher) {
        this.hasher = hasher;
    }

    /**
     * Builds a hash lookup table from a set of plaintext passwords.
     * Runs in parallel and updates a shared counter as entries are processed.
     */
    public Map<String, String> buildHashLookupTable(Set<String> dictionary, AtomicLong processed) throws AppException {
        Map<String, String> hashToPlaintext = new ConcurrentHashMap<>();

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
        } catch (Exception e) {
            throw new AppException("Failed to build hash lookup table: " + e.getMessage(), e);
        }

        return hashToPlaintext;
    }
}
