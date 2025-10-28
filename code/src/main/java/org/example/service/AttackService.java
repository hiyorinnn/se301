package org.example.service;

import org.example.model.User;

import java.util.List;
import java.util.Map;

public class AttackService {

    public long crackPasswords(List<User> users, Map<String, String> hashToPassword, ProgressReporter reporter) {
        long found = 0;
        long completed = 0;

        for (User user : users) {
            String password = hashToPassword.get(user.getHashedPassword());
            if (password != null) {
                user.markFound(password);
                found++;
            }
            completed++;
            reporter.update(completed);
        }
        return found;
    }
}
