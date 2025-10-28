package org.example.CrackTask;

import org.example.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CrackTask {
    // Atomic counters for thread-safe reporting todo shift out to report class
    private final AtomicLong passwordsFound;
    private final Collection<User> users;
    private final Map<String, String> lookupTable;


    public CrackTask(Collection<User> users, Map<String, String> lookupTable, AtomicLong passwordsFound){
        this.users = users;
        this.lookupTable = lookupTable;
        this.passwordsFound = passwordsFound;
    }

    // 3. Lookup
    public void crack() {
        // todo: hashmap if got more data, the hashmap will overflow, add interface to handle more

        users.parallelStream().forEach(user -> {
            if (user.isFound()) {
                return; // Skip if already found
            }

            // O(1) lookup in the hash table
            String plainPassword = lookupTable.get(user.getHashedPassword());

            if (plainPassword != null) {
                synchronized (user) {
                    user.markFound(plainPassword);
                }
                passwordsFound.incrementAndGet();
            }

        });
    }


}