package org.example.reporter;

public class ProgressMonitor implements AutoCloseable {
    private final Thread reporterThread;

    public ProgressMonitor(ProgressReporter reporter) {
        this.reporterThread = new Thread(reporter);
        this.reporterThread.start();
    }

    @Override
    public void close() throws InterruptedException {
        reporterThread.join();
    }
}
