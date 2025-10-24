package org.example.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.example.error.AppException;

public class Sha256Hasher implements Hasher {

    private static final String ALGORITHM = "SHA-256";
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    // reuse digest
    private static final ThreadLocal<MessageDigest> DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    });

    @Override
    public String hash(String input) throws AppException {
        try {
            MessageDigest digest = DIGEST.get();
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            char[] hexChars = new char[hashBytes.length * 2];
            for (int j = 0; j < hashBytes.length; j++) {
                int v = hashBytes[j] & 0xFF;
                hexChars[j * 2] = HEX_ARRAY[v >>> 4];
                hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
            }

            return new String(hexChars);
        } catch (RuntimeException e) {
            throw new AppException("SHA-256 hashing failed", e);
        }
    }
}
