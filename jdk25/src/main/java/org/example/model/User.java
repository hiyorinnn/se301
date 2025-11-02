package org.example.model;

import java.util.Objects;

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

    public synchronized boolean markFound(String plain) {
        if (found) {
            return false;
        }
        this.found = true;
        this.foundPassword = plain;
        return true;
    }

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
