package org.example.store;

import org.example.error.AppException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public interface HashLookupBuilder {
    /**
     * Builds the hash lookup table and updates the counter as entries are processed.
     * @param dictionary The set of plaintext passwords.
     * @param processed The counter to increment for each processed password.
     * @return The completed hash → plaintext map.
     * @throws AppException If hashing fails.
     */
    Map<String, String> buildHashLookupTable(Set<String> dictionary, AtomicLong processed) throws AppException;
}
