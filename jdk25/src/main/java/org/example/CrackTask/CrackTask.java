package org.example.CrackTask;

import org.example.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CrackTask {
    private final AtomicLong passwordsFound;
    private final Collection<User> users;
    private final Map<String, String> lookupTable;

    public CrackTask(Collection<User> users, Map<String, String> lookupTable, AtomicLong passwordsFound){
        this.users = users;
        this.lookupTable = lookupTable;
        this.passwordsFound = passwordsFound;
    }

    public void crack() {
        users.parallelStream().forEach(user -> {
            synchronized (user) {
                if (user.isFound()) {
                    return;
                }
            }

            String plainPassword = lookupTable.get(user.getHashedPassword());

            if (plainPassword != null) {
                synchronized (user) {
                    if (!user.isFound()) {
                        user.markFound(plainPassword);
                        passwordsFound.incrementAndGet();
                    }
                }
            }

        });
    }
}