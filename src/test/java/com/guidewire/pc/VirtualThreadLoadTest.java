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
    public void testSimulatedIoBlockingLoadVirtualVsPlatform() throws Exception {
        int taskCount = 10000;
        int simulatedIoDelayMs = 15; // Simulates DB / REST API network latency

        AtomicInteger virtualSuccess = new AtomicInteger(0);
        AtomicInteger platformSuccess = new AtomicInteger(0);

        // 1. Virtual Threads Test (Every request gets a lightweight virtual thread)
        long vStart = System.currentTimeMillis();
        try (ExecutorService vExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                final int idx = i;
                futures.add(vExecutor.submit(() -> {
                    try {
                        java.util.concurrent.TimeUnit.MILLISECONDS.sleep(simulatedIoDelayMs); // Unmounts thread during IO wait
                    } catch (InterruptedException ignored) {}
                    PolicyPeriod p = new PolicyPeriod();
                    p.setJobNumber("VJOB-" + idx);
                    virtualSuccess.incrementAndGet();
                }));
            }
            for (Future<?> f : futures) {
                f.get();
            }
        }
        long vDuration = System.currentTimeMillis() - vStart;

        // 2. Fixed OS Platform Threads Test (Limited to 50 OS threads)
        long pStart = System.currentTimeMillis();
        try (ExecutorService pExecutor = Executors.newFixedThreadPool(50)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                final int idx = i;
                futures.add(pExecutor.submit(() -> {
                    try {
                        java.util.concurrent.TimeUnit.MILLISECONDS.sleep(simulatedIoDelayMs); // Blocks OS thread
                    } catch (InterruptedException ignored) {}
                    PolicyPeriod p = new PolicyPeriod();
                    p.setJobNumber("PJOB-" + idx);
                    platformSuccess.incrementAndGet();
                }));
            }
            for (Future<?> f : futures) {
                f.get();
            }
        }
        long pDuration = System.currentTimeMillis() - pStart;

        double speedup = (double) pDuration / (double) vDuration;

        System.out.println("===============================================================");
        System.out.println("  Java 23 Realistic I/O Blocking Load Test Benchmark Results");
        System.out.println("  Task Count: " + taskCount + " concurrent requests (" + simulatedIoDelayMs + "ms simulated I/O delay)");
        System.out.println("  Virtual Threads Duration:  " + vDuration + " ms (Completed: " + virtualSuccess.get() + ")");
        System.out.println("  Platform Threads Duration: " + pDuration + " ms (Completed: " + platformSuccess.get() + ")");
        System.out.println("  🚀 Virtual Threads Speedup: " + String.format("%.2f", speedup) + "x FASTER!");
        System.out.println("===============================================================");

        assertEquals(taskCount, virtualSuccess.get());
        assertEquals(taskCount, platformSuccess.get());
        assertTrue(vDuration < pDuration, "Virtual Threads should complete significantly faster under I/O blocking workloads");
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
