package org.example.service;

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
            // Gets a MessageDigest instance that is unique to the current thread
            MessageDigest md = DIGEST.get();

            // Reset before each use
            md.reset();

            // Converts the input string (aka pwd from dict list) into a sequence of bytes using the standard UTF-8 encoding.
            // and performs the hash calculation on those bytes and returns the result as a raw byte array.
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Modern, concise hex conversion to convert raw array byte
            return HexFormat.of().formatHex(hashBytes);


        } catch (RuntimeException e) {
            throw new AppException("SHA-256 hashing failed", e);
        }
    }
}
