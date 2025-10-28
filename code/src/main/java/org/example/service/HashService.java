package org.example.service;

import org.example.error.AppException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashService {
    private final Hasher hasher;

    public HashService(Hasher hasher) {
        this.hasher = hasher;
    }

    public Map<String, String> buildHashDictionary(List<String> passwords, ProgressReporter reporter) throws AppException {
        Map<String, String> hashToPassword = new HashMap<>(passwords.size());
        long count = 0;

        for (String password : passwords) {
            String hash = hasher.hash(password);
            hashToPassword.put(hash, password);
            count++;
            reporter.update(count);
        }

        return hashToPassword;
    }
}
