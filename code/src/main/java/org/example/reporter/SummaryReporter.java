package org.example.reporter;

/* Reports final statistics after a dictionary attack finishes. */
public interface SummaryReporter {
    /* Print summary with total users, hashes, passwords found, and elapsed time. */
    void printSummary(long totalUsers, long totalHashes, long passwordsFound, long elapsedMs);
}
