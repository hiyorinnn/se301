package org.example.cracktask;

import org.example.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/* Crack strategy: try to resolve users' password hashes using a lookup table.
   Increment the provided counter as passwords are found. */
public interface Crack {
    // Attempt to crack each user's password using the lookupTable and update passwordsFound.
    void crack(Collection<User> users, Map<String, String> lookupTable, AtomicLong passwordsFound);
}

