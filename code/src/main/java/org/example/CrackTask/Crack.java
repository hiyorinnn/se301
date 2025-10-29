package org.example.CrackTask;

import org.example.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface Crack {
    /**
     * Attempts to crack user passwords against a pre-built lookup table.
     *
     * @param users The collection of users to check.
     * @param lookupTable A map of {hash -> plaintext} for password lookup.
     * @param passwordsFound An atomic counter to increment as passwords are found.
     */
    void crack(Collection<User> users, Map<String, String> lookupTable, AtomicLong passwordsFound);
}
