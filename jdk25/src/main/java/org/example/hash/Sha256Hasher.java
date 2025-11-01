package org.example.hash;

import org.example.error.AppException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.lang.ScopedValue;

public class Sha256Hasher implements Hasher {

    private static final String ALGORITHM = "SHA-256";

    // JDK 25: Using Scoped Values (JEP 506) instead of ThreadLocal
    // Scoped Values provide structured context passing with better integration
    // with virtual threads and structured concurrency
    private static final ScopedValue<MessageDigest> DIGEST = ScopedValue.newInstance();

    private static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    public String hash(String input) throws AppException {
        try {
            // JDK 25: Use ScopedValue for context-passing instead of ThreadLocal
            return ScopedValue.where(DIGEST, createDigest()).call(() -> {
                try {
                    byte[] hashBytes = DIGEST.get().digest(input.getBytes(StandardCharsets.UTF_8));

                    try {
                        return HexVectorEncoder.encodeToHex(hashBytes);
                    } catch (UnsupportedOperationException | LinkageError e) {
                        return HexFormat.of().formatHex(hashBytes);
                    }

                } catch (RuntimeException e) {
                    throw new AppException("SHA-256 hashing failed", e);
                }
            });

        } catch (Exception e) {
            if (e instanceof AppException) {
                throw (AppException) e;
            }
            throw new AppException("SHA-256 hashing failed", e);
        }
    }
}
