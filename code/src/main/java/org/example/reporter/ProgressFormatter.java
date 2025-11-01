package org.example.reporter;

/* Formats progress updates for display or logging. */
public interface ProgressFormatter {
    /* Return a string representing progress given current count and total. */
    String format(long count, long total);
}
