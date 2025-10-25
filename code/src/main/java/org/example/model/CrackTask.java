package org.example.model;

import org.example.service.Hasher;
import org.example.error.AppException;

public class CrackTask {
    private final User user;
    private final String candidate;
    private final Hasher hasher;
    private boolean hashingOccurred = false;

    public CrackTask(User user, String candidate, Hasher hasher) {
        this.user = user;
        this.candidate = candidate;
        this.hasher = hasher;
    }

    public boolean execute() throws AppException {
        if (user.isFound()) return false;
        String hash = hasher.hash(candidate);
        hashingOccurred = true; 
        
        if (hash.equals(user.getHashedPassword())) {
            user.markFound(candidate);
            return true;
        }
        return false;
    }
    
    public boolean didHash() {
        return hashingOccurred; 
    }
}