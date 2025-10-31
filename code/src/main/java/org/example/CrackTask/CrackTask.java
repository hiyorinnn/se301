package org.example.CrackTask;

import org.example.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CrackTask implements Crack {

    // 3. Lookup
    @Override
    public void crack(Collection<User> users, Map<String, String> lookupTable, AtomicLong passwordsFound) {
        users.parallelStream().forEach(user -> {
            if (user.isFound()) {
                return; // Skip if already found
            }

            // O(1) lookup in the hash table
            String plainPassword = lookupTable.get(user.getHashedPassword());

            // Mark found password
            if (plainPassword != null) {
                user.markFound(plainPassword);
                passwordsFound.incrementAndGet();
            }

        });
    }
}
