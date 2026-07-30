package com.guidewire.pc;

import com.guidewire.pc.batch.ActivityEscalationBatchProcess;
import com.guidewire.pc.batch.PolicyRenewalBatchProcess;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.ParallelAcceleratorService;
import com.guidewire.pc.service.ParallelAcceleratorService.ParallelEvaluationResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class VirtualThreadLoadTest {

    @Test
    public void testHighConcurrencyVirtualThreadsVsPlatformThreads() throws Exception {
        int taskCount = 2000;
        AtomicInteger virtualSuccessCount = new AtomicInteger(0);
        AtomicInteger platformSuccessCount = new AtomicInteger(0);

        // 1. Virtual Threads High-Load Test
        long vStart = System.currentTimeMillis();
        try (ExecutorService vExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                final int idx = i;
                futures.add(vExecutor.submit(() -> {
                    // Simulate I/O bound DB & API search query task
                    PolicyPeriod p = new PolicyPeriod();
                    p.setJobNumber("VJOB-" + idx);
                    p.setProductCode("PersonalAuto");
                    p.setStatus("Draft");
                    virtualSuccessCount.incrementAndGet();
                }));
            }
            for (Future<?> f : futures) {
                f.get();
            }
        }
        long vDuration = System.currentTimeMillis() - vStart;

        // 2. Fixed Platform Threads High-Load Test
        long pStart = System.currentTimeMillis();
        try (ExecutorService pExecutor = Executors.newFixedThreadPool(100)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                final int idx = i;
                futures.add(pExecutor.submit(() -> {
                    PolicyPeriod p = new PolicyPeriod();
                    p.setJobNumber("PJOB-" + idx);
                    p.setProductCode("PersonalAuto");
                    p.setStatus("Draft");
                    platformSuccessCount.incrementAndGet();
                }));
            }
            for (Future<?> f : futures) {
                f.get();
            }
        }
        long pDuration = System.currentTimeMillis() - pStart;

        System.out.println("===============================================================");
        System.out.println("  Java 23 High Concurrency Virtual Thread Benchmark Results");
        System.out.println("  Task Count: " + taskCount + " concurrent requests");
        System.out.println("  Virtual Threads Duration:  " + vDuration + " ms (Completed: " + virtualSuccessCount.get() + ")");
        System.out.println("  Platform Threads Duration: " + pDuration + " ms (Completed: " + platformSuccessCount.get() + ")");
        System.out.println("===============================================================");

        assertEquals(taskCount, virtualSuccessCount.get());
        assertEquals(taskCount, platformSuccessCount.get());
        assertTrue(vDuration <= pDuration + 500, "Virtual Threads execution should complete efficiently under load");
    }

    @Test
    public void testParallelAcceleratorVirtualThreadService() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S0009999");
        period.setProductCode("PersonalAuto");
        period.setBaseState("CA");
        period.setStatus("Draft");

        ParallelEvaluationResult result = ParallelAcceleratorService.getInstance()
                .evaluatePolicyAcceleratorsInParallel("1HGCR4F80HA000001", period);

        assertNotNull(result);
        assertNotNull(result.getVinResult());
        assertNotNull(result.getRatingResult());
        assertNotNull(result.getAiUnderwritingResult());
        assertTrue(result.getExecutionTimeMs() >= 0);
    }

    @Test
    public void testVirtualThreadBatchProcesses() {
        PolicyRenewalBatchProcess renewalBatch = new PolicyRenewalBatchProcess();
        var renewalResult = renewalBatch.run();
        assertNotNull(renewalResult);
        assertTrue(renewalResult.isSuccess());

        ActivityEscalationBatchProcess escalationBatch = new ActivityEscalationBatchProcess();
        var escalationResult = escalationBatch.run();
        assertNotNull(escalationResult);
        assertTrue(escalationResult.isSuccess());
    }
}
