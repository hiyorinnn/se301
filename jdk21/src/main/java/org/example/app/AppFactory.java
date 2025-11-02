package org.example.app;

import org.example.loader.Loading;
import org.example.loader.UserLoader;
import org.example.loader.DictionaryLoader;
import org.example.store.HashLookupBuilder;
import org.example.store.LookupTableBuilder;
import org.example.cracktask.Crack;
import org.example.cracktask.CrackTask;
import org.example.hash.Hasher;
import org.example.hash.Sha256Hasher;
import org.example.io.ResultWriter;
import org.example.io.CsvResultWriter;
import org.example.reporter.ConsoleSummaryReporter;
import org.example.reporter.SummaryReporter;

/* Lightweight factory that constructs a ready-to-run DictionaryAttackRunner.
   Keeps wiring simple and switchable for testing or production. */
public class AppFactory {

    // core components (injected or defaulted)
    private final Hasher hasher;
    private final ResultWriter writer;
    private final Crack cracker;
    private final SummaryReporter summaryReporter;

    // Use custom components
    public AppFactory(Hasher hasher, ResultWriter writer, Crack cracker, SummaryReporter summaryReporter) {
        this.hasher = hasher;
        this.writer = writer;
        this.cracker = cracker;
        this.summaryReporter = summaryReporter;
    }

    // Default configuration (SHA-256 hasher, CSV writer, CrackTask, console reporter)
    public AppFactory() {
        this(new Sha256Hasher(), new CsvResultWriter(), new CrackTask(), new ConsoleSummaryReporter());
    }

    // Build and return a fully wired DictionaryAttackRunner
    public DictionaryAttackRunner createRunner() {
        Loading loadService = new Loading(new UserLoader(), new DictionaryLoader());
        HashLookupBuilder hashBuilder = new LookupTableBuilder(hasher);

        return new DictionaryAttackRunner(loadService, hashBuilder, cracker, writer, summaryReporter);
    }
}
