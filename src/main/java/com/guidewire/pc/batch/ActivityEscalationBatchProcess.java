package com.guidewire.pc.batch;

import com.guidewire.pc.model.Activity;
import com.guidewire.pc.service.DataStoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ActivityEscalationBatchProcess implements BatchProcess {
    private static final Logger LOGGER = Logger.getLogger(ActivityEscalationBatchProcess.class.getName());

    @Override
    public String getType() { return "ActivityEscalation"; }

    @Override
    public String getDescription() { return "Escalates open underwriting activities that are near or past due using Java 23 Virtual Threads."; }

    @Override
    public BatchProcessResult run() {
        LOGGER.info("Executing Guidewire Batch Job: ActivityEscalationBatchProcess on Virtual Threads...");
        DataStoreService dataStore = DataStoreService.getInstance();
        List<Activity> activities = dataStore.getActivities();
        AtomicInteger escalatedCount = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (Activity act : activities) {
                if ("Open".equalsIgnoreCase(act.getStatus()) && "High".equalsIgnoreCase(act.getPriority())) {
                    futures.add(executor.submit(() -> {
                        act.setStatus("Escalated");
                        escalatedCount.incrementAndGet();
                    }));
                }
            }
            for (Future<?> f : futures) {
                f.get();
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Activity escalation virtual thread batch job error: {0}", e.getMessage());
            return new BatchProcessResult(getType(), false, escalatedCount.get(), "Failed: " + e.getMessage());
        }

        return new BatchProcessResult(getType(), true, escalatedCount.get(), "Escalated " + escalatedCount.get() + " high priority activities via Virtual Threads.");
    }
}
