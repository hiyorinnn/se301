package org.example.hash;

import org.example.error.AppException;

public interface Hasher {
    String hash(String input) throws AppException;
}