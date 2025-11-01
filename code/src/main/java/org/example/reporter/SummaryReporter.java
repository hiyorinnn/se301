package org.example.reporter;

/**
 * Prints or logs the final summary of the attack.
 */
public interface SummaryReporter {
    /**
     * Prints summary statistics after the attack finishes.
     *
     * @param totalUsers      total number of users processed
     * @param totalHashes     total number of hashes computed
     * @param passwordsFound  total passwords successfully cracked
     * @param elapsedMs       total elapsed time in milliseconds
     */
    void printSummary(long totalUsers, long totalHashes, long passwordsFound, long elapsedMs);
}
