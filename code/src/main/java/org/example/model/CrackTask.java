package org.example.model;

import org.example.service.Hasher;

import org.example.error.AppException;

public class CrackTask implements ExecutableTask, HashTrackingTask {
    private final User user;
    private final String dictionaryPassword;
    private final Hasher hasher;
    private static int hashCount;
    private boolean hashingOccurred = false;

    public CrackTask(User user, String dictionaryPassword, Hasher hasher) {
        this.user = user;
        this.dictionaryPassword = dictionaryPassword;
        this.hasher = hasher;
    }

    public boolean execute() throws AppException {
        if (user.isFound()) return false;
        String hash = hasher.hash(dictionaryPassword);
        hashCount++;
        hashingOccurred = true; 
        
        if (hash.equals(user.getHashedPassword())) {
            user.markFound(dictionaryPassword);
            return true;
        }
        return false;
    }

    public static int getCount() {
        return hashCount;
    }
    
    public boolean didHash() {
        return hashingOccurred; 
    }
}