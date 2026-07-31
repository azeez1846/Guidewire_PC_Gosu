package com.guidewire.pc;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.pcf.PCFParser;
import com.guidewire.pc.service.DataStoreService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceBenchmarkTest {

    @Test
    public void testPCFParserCachingPerformance() {
        PCFParser parser = new PCFParser(new File("."));

        // First parse (warmup)
        long start1 = System.nanoTime();
        PCFParser.PCFDefinition def1 = parser.parsePCF("Desktop");
        long duration1 = System.nanoTime() - start1;

        assertNotNull(def1, "Desktop PCF should be found and parsed");

        // Second parse (cached in-memory)
        long start2 = System.nanoTime();
        PCFParser.PCFDefinition def2 = parser.parsePCF("Desktop");
        long duration2 = System.nanoTime() - start2;

        assertNotNull(def2, "Cached Desktop PCF should be returned");
        assertSame(def1, def2, "Cached PCF instance must be identical to warm-warmed singleton instance");

        double millisCached = duration2 / 1_000_000.0;
        System.out.println("🚀 [PCF Cache Benchmark] Initial: " + String.format("%.4f", duration1 / 1_000_000.0) + " ms, Cached: " + String.format("%.4f", millisCached) + " ms");
        assertTrue(millisCached < 1.0, "Cached PCF retrieval must execute in under 1.0 millisecond");
    }

    @Test
    public void testDataStoreInMemoryCachePerformance() {
        DataStoreService dataStore = DataStoreService.getInstance();

        // Query accounts 1,000 times concurrently
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            List<Account> accounts = dataStore.getAccounts();
            assertFalse(accounts.isEmpty());
        }
        long duration = System.nanoTime() - start;

        double avgMillisPerRead = (duration / 1_000_000.0) / 1000.0;
        System.out.println("🚀 [DataStore Cache Benchmark] Average cached read duration per query: " + String.format("%.4f", avgMillisPerRead) + " ms (1000 reads executed)");
        assertTrue(avgMillisPerRead < 0.1, "DataStore in-memory read must execute in under 0.1 milliseconds per call");
    }

    @Test
    public void testFindAccountAndSubmissionCacheSpeed() {
        DataStoreService dataStore = DataStoreService.getInstance();

        long start = System.nanoTime();
        Account acc = dataStore.findAccount("A0001001");
        PolicyPeriod sub = dataStore.findSubmission("S0005001");
        long duration = System.nanoTime() - start;

        assertNotNull(acc);
        assertNotNull(sub);

        double durationMillis = duration / 1_000_000.0;
        System.out.println("🚀 [Direct Cache Lookup] Account + Submission lookup duration: " + String.format("%.4f", durationMillis) + " ms");
        assertTrue(durationMillis < 0.5, "Direct cache lookup must execute in under 0.5 milliseconds");
    }
}
