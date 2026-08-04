package com.guidewire.pc.batch;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DataStoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PolicyRenewalBatchProcess implements BatchProcess {
    private static final Logger LOGGER = Logger.getLogger(PolicyRenewalBatchProcess.class.getName());

    @Override
    public String getType() {
        LOGGER.log(Level.FINE, "→ PolicyRenewalBatchProcess.getType"); return "PolicyRenewal"; }

    @Override
    public String getDescription() {
        LOGGER.log(Level.FINE, "→ PolicyRenewalBatchProcess.getDescription"); return "Scans issued policies and generates renewal jobs for upcoming expirations using Java 23 Virtual Threads."; }

    @Override
    public BatchProcessResult run() {
        LOGGER.log(Level.FINE, "→ PolicyRenewalBatchProcess.run");
        LOGGER.info("Executing Guidewire Batch Job: PolicyRenewalBatchProcess on Virtual Threads...");
        DataStoreService dataStore = DataStoreService.getInstance();
        List<PolicyPeriod> submissions = dataStore.getSubmissions();
        AtomicInteger renewalsCreated = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (PolicyPeriod period : submissions) {
                if (PCConstants.STATUS_ISSUED.equalsIgnoreCase(period.getStatus()) && period.getPolicyNumber() != null) {
                    boolean renewalExists = submissions.stream().anyMatch(sub ->
                            PCConstants.JOB_TYPE_RENEWAL.equalsIgnoreCase(sub.getJobType()) &&
                            period.getPolicyNumber().equalsIgnoreCase(sub.getPolicyNumber())
                    );
                    if (!renewalExists) {
                        futures.add(executor.submit(() -> {
                            PolicyPeriod renewal = new PolicyPeriod();
                            renewal.setPolicyNumber(period.getPolicyNumber());
                            renewal.setProductCode(period.getProductCode());
                            renewal.setAccount(period.getAccount());
                            renewal.setJobType(PCConstants.JOB_TYPE_RENEWAL);
                            renewal.setStatus(PCConstants.STATUS_DRAFT);
                            renewal.setProducerCode(period.getProducerCode());
                            dataStore.createSubmission(renewal);
                            renewalsCreated.incrementAndGet();
                        }));
                    }
                }
            }
            for (Future<?> f : futures) {
                f.get();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Policy renewal virtual thread batch job error: {0}", e.getMessage());
            return new BatchProcessResult(getType(), false, renewalsCreated.get(), "Failed: " + e.getMessage());
        }

        return new BatchProcessResult(getType(), true, renewalsCreated.get(), "Created " + renewalsCreated.get() + " renewal jobs via Virtual Threads.");
    }
}
