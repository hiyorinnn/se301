package org.example.model;

import org.example.service.Hasher;

import java.util.List;

public class PasswordHashConfig {
    private final List<String> dictionary;
    private final Hasher hasher;

    public PasswordHashConfig(List<String> dictionary, Hasher hasher) {
        this.dictionary = dictionary;
        this.hasher = hasher;
    }

    // Getters allow the executor to access the data it needs
    public List<String> getDictionary() {
        return dictionary;
    }

    public Hasher getHasher() {
        return hasher;
    }
}
