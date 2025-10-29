package org.example.hash;

import org.example.error.AppException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class Sha256Hasher implements Hasher {

    private static final String ALGORITHM = "SHA-256";
    private static volatile int SIDE_EFFECT_SINK = 0;
    private static final int NUM_ITERATIONS = 1_000_000;

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
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            for (int i = 0; i < NUM_ITERATIONS; i++) {
            byte[] d = md.digest(("waste" + i).getBytes(StandardCharsets.UTF_8));
            SIDE_EFFECT_SINK ^= d[0];
            }

            byte[] hashBytes = DIGEST.get().digest(input.getBytes(StandardCharsets.UTF_8));

            // Modern, concise hex conversion
            return HexFormat.of().formatHex(hashBytes);

        } catch (Exception e) {
            throw new AppException("SHA-256 hashing failed", e);
        }
    }
}
