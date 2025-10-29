package org.example.hash;

import org.example.error.AppException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class Sha256Hasher implements Hasher {

    private static final String ALGORITHM = "SHA-256";

    // Thread-safe reuse of MessageDigest
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
            digest.reset(); // Reset digest state before each hash operation
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Modern, concise hex conversion
            return HexFormat.of().formatHex(hashBytes);

        } catch (RuntimeException e) {
            throw new AppException("SHA-256 hashing failed", e);
        }
    }
}
