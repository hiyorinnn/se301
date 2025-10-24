package org.example.service;

import org.example.error.AppException;

public interface Hasher {
    String hash(String input) throws AppException;
}