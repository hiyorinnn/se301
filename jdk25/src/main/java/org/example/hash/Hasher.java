package org.example.hash;

import org.example.error.AppException;

/**
 * Defines a contract for hashing strings.
 * Implementations can use any hashing algorithm.
 */
public interface Hasher {

    /**
     * Hashes the given input string and returns its hashed representation.
     */
    String hash(String input) throws AppException;
}