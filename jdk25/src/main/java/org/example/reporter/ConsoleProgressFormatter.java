package org.example.reporter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Implementation of ProgressFormatter that formats progress information for console output.
public class ConsoleProgressFormatter implements ProgressFormatter {

    // Formatter to display the current time in HH:mm:ss format.
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Formats the current progress as a string for display on the console.
    // count: the number of items completed so far.
    // total: the total number of items to process.
    // Returns a string including timestamp, percentage complete, items found, and tasks remaining.
    @Override
    public String format(long count, long total) {
        // Calculate percentage completion
        double progress = (double) count / total * 100.0;

        // Calculate remaining tasks
        long remaining = total - count;

        // Get the current timestamp
        String ts = LocalDateTime.now().format(TIME_FORMAT);

        // Return a formatted string showing progress details
        return String.format("[%s] %.2f%% complete | Passwords Found: %d | Tasks Remaining: %d",
                             ts, progress, count, remaining);
    }
}

