package org.example.model;

import java.util.*;
import org.example.service.*;

public class CrackTask {
    
    String username;
    String password;
    
    private static Map<String, User> users;
    private static List<String> cracked;
    private static Map<String, String> reverseLookupCache;
    private static int passwordsFound = 0;
    private static int hashesComputed = 0;
    private static Hasher hasher = new Sha256Hasher();

    public static int getPasswordsFound() { 
        return passwordsFound; 
    }


    public static int getHashesComputed() { 
        return hashesComputed; 
    }

    public CrackTask(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Static methods to set references
    public static void setUsers(Map<String, User> usersMap) {
        users = usersMap;
    }
    
    public static void setCracked(List<String> crackedList) {
        cracked = crackedList;
    }
    
    public static void setReverseLookupCache(Map<String, String> cache) {
        reverseLookupCache = cache;
    }

    public void execute() {
        User user = users.get(username);
        if (user == null || user.isFound) return;

        try {
            String hash = hasher.hash(password);
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
