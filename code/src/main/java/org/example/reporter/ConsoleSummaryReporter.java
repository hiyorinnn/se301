package org.example.reporter;

/**
 * Default console implementation of SummaryReporter.
 */
public class ConsoleSummaryReporter implements SummaryReporter {

    @Override
    public void printSummary(long totalUsers, long totalHashes, long passwordsFound, long elapsedMs) {
        System.out.println();
        System.out.println("Total passwords found: " + passwordsFound);
        System.out.println("Total hashes computed: " + totalHashes);
        System.out.println("Total time spent (ms): " + elapsedMs);
    }
}
