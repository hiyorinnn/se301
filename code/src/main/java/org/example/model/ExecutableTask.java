package org.example.model;

import org.example.error.AppException;

public interface ExecutableTask {
    boolean execute() throws AppException;
}
