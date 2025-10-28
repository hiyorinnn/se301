package org.example.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProgressReporter {
    private final long total;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ProgressReporter(long total) {
        this.total = total;
    }

    public void update(long completed) {
        if (completed % 1000 == 0 || completed == total) {
            double progress = (double) completed / total * 100.0;
            String ts = LocalDateTime.now().format(fmt);
            System.out.printf("\r[%s] %.2f%% complete | Tasks Remaining: %-6d", ts, progress, (total - completed));
        }
    }
}
