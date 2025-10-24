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

    public String getUsername() {
        return this.username;
    }

    public String getHashedPassword() {
        return this.hashedPassword; 
    }

    public String getFoundPassword() {
        return this.foundPassword;
    }

    public boolean isFound() {
        return this.isFound;
    }
}
