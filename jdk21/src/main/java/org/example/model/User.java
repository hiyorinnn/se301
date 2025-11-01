package org.example.model;

import java.util.Objects;

/**
 * Represents a user with a username and hashed password.
 * Tracks whether the user’s password has been found and stores it if available.
 */
public class User {
    private final String username;
    private final String hashedPassword;
    private volatile boolean found = false;
    private volatile String foundPassword = null;

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }

    public boolean isFound() { return found; }
    public String getFoundPassword() { return foundPassword; }

    // Marks this user as found and stores the recovered plaintext password
    // Thread-safe: uses volatile for visibility, but needs synchronization for check-then-act
    public synchronized boolean markFound(String plain) {
        if (found) {
            return false; // Already found by another thread
        }
        this.found = true;
        this.foundPassword = plain;
        return true; // Successfully marked as found
    }

    // Equality based on username and hashed password
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return username.equals(user.username) &&
               hashedPassword.equals(user.hashedPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, hashedPassword);
    }
}
