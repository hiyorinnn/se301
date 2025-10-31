package org.example.store;

import org.example.error.AppException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public interface StoreHashPassword {
    /**
     * Builds the lookup table and updates a counter as it works.
     * @param dictionary The list of words to hash.
     * @param processed The counter to increment for each processed word.
     * @return The completed lookup map.
     * @throws AppException If hashing fails.
     */
    Map<String, String> buildHashLookupTable(Set<String> dictionary, AtomicLong processed)throws AppException;
}
