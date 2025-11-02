package org.example.cracktask;

import org.example.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Attempts to crack user passwords by looking them up in a precomputed hash table.
 * Uses parallel processing for efficiency.
 */
public class CrackTask implements Crack {

    /**
     * Checks each user’s hashed password against the lookup table.
     * If found, marks the user as found and increments the counter.
     */
    @Override
    public void crack(Collection<User> users, Map<String, String> lookupTable, AtomicLong passwordsFound) {
        users.parallelStream().forEach(user -> {
            if (user.isFound()) {
                return; // Skip if already found
            }

            String plainPassword = lookupTable.get(user.getHashedPassword());

            if (plainPassword != null) {
                user.markFound(plainPassword);
                passwordsFound.incrementAndGet();
            }
        });
    }
}
