package org.example.store;

import org.example.error.AppException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/* Builds a hash -> plaintext lookup from a set of dictionary passwords.
   Increments the provided counter as entries are processed.
   Throws AppException on failure. */
public interface HashLookupBuilder {
    /* Create the lookup map from plaintext passwords to their hashes.
       Implementations should update the processed counter while building
       and return a map mapping hash -> plaintext. */
    Map<String, String> buildHashLookupTable(Set<String> dictionary, AtomicLong processed) throws AppException;
}
