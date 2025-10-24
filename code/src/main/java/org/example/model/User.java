package org.example.model;

public class User {
    String username;
    String hashedPassword;
    boolean isFound = false;
    String foundPassword = null;

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }
}
