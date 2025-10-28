package org.example.model;


// todo maybe make a DTO, so future if the input not txt anymore jsut change DTO, dont need change user
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
