package org.example.service;

import org.example.store.HashLookupBuilder;
import org.example.reporter.ConsoleProgressFormatter;
import org.example.reporter.LiveProgressReporter;
import org.example.threads.SingleThreadProvider;
import org.example.error.AppException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class HashLookupService {
    private final HashLookupBuilder builder;

    public HashLookupService(HashLookupBuilder builder) {
        this.builder = builder;
    }

    /**
     * Build the hash->plaintext lookup table while reporting live progress.
     */
    public Map<String, String> buildWithProgress(Set<String> dict) throws InterruptedException, AppException {
        AtomicLong processed = new AtomicLong(0);
        CountDownLatch reporterLatch = new CountDownLatch(1);

        var reporter = new LiveProgressReporter(processed, dict.size(), reporterLatch, new ConsoleProgressFormatter());

        try (SingleThreadProvider reporterProvider = new SingleThreadProvider()) {
            reporterProvider.submitTask(reporter);
            Map<String, String> map = builder.buildHashLookupTable(dict, processed);
            reporterLatch.await(); // wait reporter to finish printing final state
            return map;
        }
    }
}

