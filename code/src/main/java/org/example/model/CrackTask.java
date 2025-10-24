package org.example.model;

public class CrackTask {
    
    String username;
    String password;

    CrackTask(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void execute() {
        User user = users.get(username);
        if (user == null || user.isFound) return;

        try {
            String hash = sha256(password);
            hashesComputed++;
            reverseLookupCache.put(password, hash);

            if (hash.equals(user.hashedPassword)) {
                cracked.add(username + ": " + password);
                user.isFound = true;
                user.foundPassword = password;
                passwordsFound++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
