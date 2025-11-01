package org.example.PasswordHashStore;

import org.example.hash.Hasher;

import org.example.error.AppException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LookupTableBuilder {
    private final Set<String> dictionary;
    private final Hasher hasher;

    public LookupTableBuilder(Set<String> dictionary, Hasher hasher) {
        this.dictionary = dictionary;
        this.hasher = hasher;
    }

    public Map<String, String> buildHashLookupTable() throws AppException {
        Map<String, String> hashToPlaintext = new ConcurrentHashMap<>();
        AtomicLong processed = new AtomicLong(0);
        long total = dictionary.size();

        try {
            dictionary.parallelStream().forEach(plaintext -> {
                try {
                    String hash = hasher.hash(plaintext);
                    hashToPlaintext.put(hash, plaintext);

                    long count = processed.incrementAndGet();
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
            System.out.println();
        } catch (Exception e) {
            throw new AppException("Failed to build hash lookup table: " + e.getMessage(), e);
        }

        return hashToPlaintext;
    }
}