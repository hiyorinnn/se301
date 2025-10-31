package org.example.reporter;

public interface ProgressFormatter {
    /**
     * Formats the progress output.
     *
     * @param count     current completed tasks
     * @param total     total tasks
     * @return formatted progress string
     */
    String format(long count, long total);
}
