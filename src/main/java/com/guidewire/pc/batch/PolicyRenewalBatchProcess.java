package com.guidewire.pc.batch;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DataStoreService;

import java.util.List;
import java.util.logging.Logger;

public class PolicyRenewalBatchProcess implements BatchProcess {
    private static final Logger LOGGER = Logger.getLogger(PolicyRenewalBatchProcess.class.getName());

    @Override
    public String getType() { return "PolicyRenewal"; }

    @Override
    public String getDescription() { return "Scans issued policies and generates renewal jobs for upcoming expirations."; }

    @Override
    public BatchProcessResult run() {
        LOGGER.info("Executing Guidewire Batch Job: PolicyRenewalBatchProcess...");
        DataStoreService dataStore = DataStoreService.getInstance();
        List<PolicyPeriod> submissions = dataStore.getSubmissions();
        int renewalsCreated = 0;

        for (PolicyPeriod period : submissions) {
            if ("Issued".equalsIgnoreCase(period.getStatus())) {
                PolicyPeriod renewal = new PolicyPeriod();
                renewal.setPolicyNumber(period.getPolicyNumber());
                renewal.setProductCode(period.getProductCode());
                renewal.setAccount(period.getAccount());
                renewal.setJobType("Renewal");
                renewal.setStatus("Draft");
                renewal.setProducerCode(period.getProducerCode());
                dataStore.createSubmission(renewal);
                renewalsCreated++;
            }
        }

        return new BatchProcessResult(getType(), true, renewalsCreated, "Created " + renewalsCreated + " renewal jobs.");
    }
}
