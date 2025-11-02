package org.example.service;

import org.example.cracktask.Crack;
import org.example.model.User;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class CrackService {
    private final Crack cracker;

    public CrackService(Crack cracker) {
        this.cracker = cracker;
    }

    /**
     * Run the crack operation and return how many passwords were found.
     */
    public long crackAll(Set<User> users, Map<String, String> hashToPlaintext) {
        AtomicLong passwordsFound = new AtomicLong(0);
        System.out.println("Starting attack with " + users.size() + " total tasks...");
        cracker.crack(users, hashToPlaintext, passwordsFound);
        return passwordsFound.get();
    }
}
