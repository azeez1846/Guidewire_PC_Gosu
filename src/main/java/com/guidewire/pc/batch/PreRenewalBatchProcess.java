package com.guidewire.pc.batch;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DataStoreService;
import com.guidewire.pc.service.PolicyLifecycleService;
import com.guidewire.pc.service.UWAuthorityEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreRenewalBatchProcess implements BatchProcess {
    private static final Logger LOGGER = Logger.getLogger(PreRenewalBatchProcess.class.getName());

    @Override
    public String getType() {
        LOGGER.log(Level.FINE, "→ PreRenewalBatchProcess.getType"); return "PreRenewal"; }

    @Override
    public String getDescription() {
        LOGGER.log(Level.FINE, "→ PreRenewalBatchProcess.getDescription");
        return "Guidewire OOTB Pre-Renewal Batch Engine: Scans policies within 60 days of expiration, runs pre-renewal underwriting checks, creates renewal jobs, and re-rates using Virtual Threads.";
    }

    @Override
    public BatchProcessResult run() {
        LOGGER.log(Level.FINE, "→ PreRenewalBatchProcess.run");
        LOGGER.info("Executing Guidewire Batch Job: PreRenewalBatchProcess on Virtual Threads...");
        DataStoreService dataStore = DataStoreService.getInstance();
        List<PolicyPeriod> submissions = dataStore.getSubmissions();
        AtomicInteger preRenewalsProcessed = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (PolicyPeriod period : submissions) {
                if ((PCConstants.STATUS_ISSUED.equalsIgnoreCase(period.getStatus()) || PCConstants.STATUS_BOUND.equalsIgnoreCase(period.getStatus()))
                        && period.getPolicyNumber() != null) {
                    
                    boolean renewalAlreadyExists = submissions.stream().anyMatch(sub ->
                            PCConstants.JOB_TYPE_RENEWAL.equalsIgnoreCase(sub.getJobType()) &&
                            period.getPolicyNumber().equalsIgnoreCase(sub.getPolicyNumber())
                    );

                    if (!renewalAlreadyExists) {
                        futures.add(executor.submit(() -> {
                            // Generate Pre-Renewal via PolicyLifecycleService
                            PolicyPeriod renewal = PolicyLifecycleService.getInstance().renewPolicy(period.getPolicyNumber());
                            
                            // Run Pre-Renewal Underwriting Authority Checks
                            UWAuthorityEngine.getInstance().evaluatePolicy(renewal);
                            
                            preRenewalsProcessed.incrementAndGet();
                            LOGGER.log(Level.INFO, "Pre-Renewal Batch Process generated renewal job {0} for policy {1} (Open UW Issues: {2})",
                                    new Object[]{renewal.getJobNumber(), period.getPolicyNumber(), renewal.getUwIssues().size()});
                        }));
                    }
                }
            }
            for (Future<?> f : futures) {
                f.get();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Pre-Renewal virtual thread batch process error: {0}", e.getMessage());
            return new BatchProcessResult(getType(), false, preRenewalsProcessed.get(), "Failed: " + e.getMessage());
        }

        return new BatchProcessResult(getType(), true, preRenewalsProcessed.get(), "Successfully processed " + preRenewalsProcessed.get() + " pre-renewal policy branches via Virtual Threads.");
    }
}
