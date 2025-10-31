package org.example.model;

import java.util.Objects;

/**
 * Represents a user with a username and hashed password.
 * Tracks whether the user’s password has been found and stores it if available.
 */
public class User {
    private final String username;
    private final String hashedPassword;
    private boolean found = false;
    private String foundPassword = null;

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }

    public boolean isFound() { return found; }
    public String getFoundPassword() { return foundPassword; }

    // Marks this user as found and stores the recovered plaintext password
    public void markFound(String plain) {
        this.found = true;
        this.foundPassword = plain;
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
