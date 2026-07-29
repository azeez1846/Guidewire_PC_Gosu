package com.guidewire.pc.batch;

import com.guidewire.pc.model.Activity;
import com.guidewire.pc.service.DataStoreService;

import java.util.List;
import java.util.logging.Logger;

public class ActivityEscalationBatchProcess implements BatchProcess {
    private static final Logger LOGGER = Logger.getLogger(ActivityEscalationBatchProcess.class.getName());

    @Override
    public String getType() { return "ActivityEscalation"; }

    @Override
    public String getDescription() { return "Escalates open underwriting activities that are near or past due."; }

    @Override
    public BatchProcessResult run() {
        LOGGER.info("Executing Guidewire Batch Job: ActivityEscalationBatchProcess...");
        DataStoreService dataStore = DataStoreService.getInstance();
        List<Activity> activities = dataStore.getActivities();
        int escalatedCount = 0;

        for (Activity act : activities) {
            if ("Open".equalsIgnoreCase(act.getStatus()) && "High".equalsIgnoreCase(act.getPriority())) {
                act.setStatus("Escalated");
                escalatedCount++;
            }
        }

        return new BatchProcessResult(getType(), true, escalatedCount, "Escalated " + escalatedCount + " high priority activities.");
    }
}
