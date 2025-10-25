package org.example.model;

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

    public void markFound(String plain) {
        this.found = true;
        this.foundPassword = plain;
    }
}
