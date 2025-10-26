package org.example.model;

import org.example.service.Hasher;

import org.example.error.AppException;

import java.util.concurrent.atomic.AtomicLong;

public class CrackTask {
    private final User user;
    private final String dictionaryPassword;
    private final Hasher hasher;
//    private static int hashCount;
    private static final AtomicLong hashCount = new AtomicLong(0);

    public CrackTask(User user, String dictionaryPassword, Hasher hasher) {
        this.user = user;
        this.dictionaryPassword = dictionaryPassword;
        this.hasher = hasher;
    }

    public boolean execute() throws AppException {
        if (user.isFound()) return false;

        String hash = hasher.hash(dictionaryPassword);
        hashCount.incrementAndGet();

        if (hash.equals(user.getHashedPassword())) {
            user.markFound(dictionaryPassword);
            return true;
        }
        return false;
    }

    public static long getCount() {
        return hashCount.get(); // Use the thread-safe get method
    }
}