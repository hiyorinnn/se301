package org.example.service;

import org.example.io.ResultWriter;
import org.example.model.User;
import org.example.error.AppException;

import java.util.Set;

/* Wrapper around ResultWriter to handle writing user results to a file. */
public class ResultService {
    private final ResultWriter writer;

    public ResultService(ResultWriter writer) {
        this.writer = writer;
    }

    /* Write the given users' results to the specified output path. */
    public void write(String outputPath, Set<User> users) throws AppException {
        writer.write(outputPath, users);
    }
}

