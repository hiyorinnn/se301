package org.example.service;

import org.example.io.ResultWriter;
import org.example.model.User;
import org.example.error.AppException;

import java.util.Set;

public class ResultService {
    private final ResultWriter writer;

    public ResultService(ResultWriter writer) {
        this.writer = writer;
    }

    public void write(String outputPath, Set<User> users) throws AppException {
        writer.write(outputPath, users);
    }
}
