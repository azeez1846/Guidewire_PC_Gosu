package com.guidewire.pc.service;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PolicyLifecycleService {
    private static final Logger LOGGER = Logger.getLogger(PolicyLifecycleService.class.getName());
    private static final PolicyLifecycleService instance = new PolicyLifecycleService();

    private final DataStoreService dataStore = DataStoreService.getInstance();

    private PolicyLifecycleService() {}

    public static PolicyLifecycleService getInstance() {
        return instance;
    }

    /**
     * Start a mid-term Policy Change (Endorsement)
     */
    public PolicyPeriod startPolicyChange(String policyNumber, String editEffectiveDate, String newBiLimit, String newCollDeductible) {
        PolicyPeriod orig = dataStore.findPolicyByPolicyNumber(policyNumber);
        if (orig == null) {
            throw new IllegalArgumentException("Policy not found for policy number: " + policyNumber);
        }

        PolicyPeriod changePeriod = new PolicyPeriod();
        changePeriod.setJobType(PCConstants.JOB_TYPE_POLICY_CHANGE);
        changePeriod.setJobNumber("C000" + com.guidewire.pc.util.SequenceGenerator.nextId());
        changePeriod.setPolicyNumber(orig.getPolicyNumber());
        changePeriod.setProductCode(orig.getProductCode());
        changePeriod.setAccount(orig.getAccount());
        changePeriod.setBaseState(orig.getBaseState());
        changePeriod.setProducerCode(orig.getProducerCode());
        changePeriod.setEffectiveDate(orig.getEffectiveDate());
        changePeriod.setExpirationDate(orig.getExpirationDate());
        changePeriod.setTermMonths(orig.getTermMonths());
        changePeriod.setBodilyInjuryLimit(newBiLimit != null && !newBiLimit.trim().isEmpty() ? newBiLimit : orig.getBodilyInjuryLimit());
        changePeriod.setPropertyDamageLimit(orig.getPropertyDamageLimit());
        changePeriod.setComprehensiveDeductible(orig.getComprehensiveDeductible());
        changePeriod.setCollisionDeductible(newCollDeductible != null && !newCollDeductible.trim().isEmpty() ? newCollDeductible : orig.getCollisionDeductible());
        changePeriod.setEditEffectiveDate(editEffectiveDate != null ? parseDate(editEffectiveDate) : new Date());

        // Rate endorsement with pro-rata factor
        RatingEngine.getInstance().rate(changePeriod);

        // Adjust for pro-rata factor based on edit effective date
        double proRataFactor = calculateProRataFactor(orig.getPeriodStart(), orig.getPeriodEnd(), changePeriod.getEditEffectiveDate());
        BigDecimal adjustedPrem = changePeriod.getTotalPremium().multiply(BigDecimal.valueOf(proRataFactor)).setScale(2, RoundingMode.HALF_UP);
        changePeriod.setTotalPremium(adjustedPrem);

        changePeriod.setStatus(PCConstants.STATUS_QUOTED);
        dataStore.createSubmission(changePeriod);
        LOGGER.log(Level.INFO, "Policy Change job created: {0} for policy: {1} (Pro-rata factor: {2})",
                new Object[]{changePeriod.getJobNumber(), policyNumber, String.format("%.2f", proRataFactor)});
        return changePeriod;
    }

    /**
     * Bind and complete a Policy Change
     */
    public PolicyPeriod bindPolicyChange(String jobNumber) {
        PolicyPeriod changePeriod = dataStore.findSubmission(jobNumber);
        if (changePeriod == null) {
            throw new IllegalArgumentException("Job not found: " + jobNumber);
        }
        changePeriod.setStatus(PCConstants.STATUS_BOUND);
        dataStore.updateSubmission(changePeriod);

        // Also update original policy record with new coverages
        PolicyPeriod orig = dataStore.findPolicyByPolicyNumber(changePeriod.getPolicyNumber());
        if (orig != null) {
            orig.setBodilyInjuryLimit(changePeriod.getBodilyInjuryLimit());
            orig.setCollisionDeductible(changePeriod.getCollisionDeductible());
            dataStore.updateSubmission(orig);
        }
        LOGGER.log(Level.INFO, "Policy Change bound: {0}", jobNumber);
        return changePeriod;
    }

    /**
     * Cancel an active policy
     */
    public PolicyPeriod cancelPolicy(String policyNumber, String cancelReason, String calcMethod, String cancelEffDateStr) {
        PolicyPeriod period = dataStore.findPolicyByPolicyNumber(policyNumber);
        if (period == null) {
            throw new IllegalArgumentException("Policy not found: " + policyNumber);
        }

        period.setStatus(PCConstants.STATUS_CANCELED);
        period.setJobType(PCConstants.JOB_TYPE_CANCELLATION);

        BigDecimal originalPrem = period.getTotalPremium() != null ? period.getTotalPremium() : BigDecimal.ZERO;
        Date cancelDate = parseDate(cancelEffDateStr);
        double unearnedFactor = calculateProRataFactor(period.getPeriodStart(), period.getPeriodEnd(), cancelDate != null ? cancelDate : new Date());

        BigDecimal unearnedReturn;
        if ("ShortRate".equalsIgnoreCase(calcMethod)) {
            // Short-rate retention penalty
            unearnedReturn = originalPrem.multiply(BigDecimal.valueOf(unearnedFactor * PCConstants.SHORT_RATE_RETENTION_FACTOR)).setScale(2, RoundingMode.HALF_UP);
        } else {
            // Standard Pro-Rata return
            unearnedReturn = originalPrem.multiply(BigDecimal.valueOf(unearnedFactor)).setScale(2, RoundingMode.HALF_UP);
        }

        period.setTotalPremium(unearnedReturn.negate());
        dataStore.updateSubmission(period);
        LOGGER.log(Level.INFO, "Policy cancelled: {0} Reason: {1} Refund: {2}",
                new Object[]{policyNumber, cancelReason, unearnedReturn});
        return period;
    }

    /**
     * Reinstate a cancelled policy
     */
    public PolicyPeriod reinstatePolicy(String policyNumber, String reinstatementReason) {
        PolicyPeriod period = dataStore.findPolicyByPolicyNumber(policyNumber);
        if (period == null) {
            throw new IllegalArgumentException("Policy not found: " + policyNumber);
        }

        period.setStatus(PCConstants.STATUS_ISSUED);
        period.setJobType(PCConstants.JOB_TYPE_REINSTATEMENT);
        dataStore.updateSubmission(period);
        LOGGER.log(Level.INFO, "Policy reinstated: {0} Reason: {1}", new Object[]{policyNumber, reinstatementReason});
        return period;
    }

    /**
     * Generate Policy Renewal
     */
    public PolicyPeriod renewPolicy(String policyNumber) {
        PolicyPeriod orig = dataStore.findPolicyByPolicyNumber(policyNumber);
        if (orig == null) {
            throw new IllegalArgumentException("Policy not found: " + policyNumber);
        }

        PolicyPeriod renewal = new PolicyPeriod();
        renewal.setJobType(PCConstants.JOB_TYPE_RENEWAL);
        renewal.setJobNumber("R000" + com.guidewire.pc.util.SequenceGenerator.nextId());
        renewal.setPolicyNumber(orig.getPolicyNumber());
        renewal.setProductCode(orig.getProductCode());
        renewal.setAccount(orig.getAccount());
        renewal.setBaseState(orig.getBaseState());
        renewal.setProducerCode(orig.getProducerCode());
        renewal.setTermMonths(orig.getTermMonths());
        renewal.setBodilyInjuryLimit(orig.getBodilyInjuryLimit());
        renewal.setPropertyDamageLimit(orig.getPropertyDamageLimit());
        renewal.setComprehensiveDeductible(orig.getComprehensiveDeductible());
        renewal.setCollisionDeductible(orig.getCollisionDeductible());

        // Increment term dates by 1 year
        if (orig.getExpirationDate() != null) {
            renewal.setEffectiveDate(orig.getExpirationDate());
            Date expDate = parseDate(orig.getExpirationDate());
            if (expDate != null) {
                Date newExp = new Date(expDate.getTime() + TimeUnit.DAYS.toMillis(365));
                renewal.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd").format(newExp));
            }
        }

        RatingEngine.getInstance().rate(renewal);
        renewal.setStatus(PCConstants.STATUS_QUOTED);
        dataStore.createSubmission(renewal);
        LOGGER.log(Level.INFO, "Renewal job created: {0} for policy: {1}", new Object[]{renewal.getJobNumber(), policyNumber});
        return renewal;
    }

    public PolicyPeriod copySubmission(String sourceJobNum) {
        PolicyPeriod orig = dataStore.findSubmission(sourceJobNum);
        if (orig == null) {
            throw new IllegalArgumentException("Source submission not found for copy: " + sourceJobNum);
        }
        String newJobNum = "S000" + com.guidewire.pc.util.SequenceGenerator.nextId();
        PolicyPeriod copy = orig.copySubmissionBranch(newJobNum);
        dataStore.createSubmission(copy);
        LOGGER.log(Level.INFO, "Submission {0} copied into new submission {1} for account {2}",
                new Object[]{sourceJobNum, newJobNum, orig.getAccount() != null ? orig.getAccount().getAccountNumber() : "N/A"});
        return copy;
    }

    /**
     * Quote a Policy Submission branch with UW Authority Issue evaluation
     */
    public PolicyPeriod quoteSubmissionBranch(String jobNumber) {
        PolicyPeriod period = dataStore.findSubmission(jobNumber);
        if (period == null) {
            throw new IllegalArgumentException("Job not found: " + jobNumber);
        }

        RatingEngine.getInstance().rate(period);
        UWAuthorityEngine.getInstance().evaluatePolicy(period);

        if (period.hasBlockingQuoteIssues()) {
            period.setStatus(PCConstants.STATUS_DRAFT);
            LOGGER.log(Level.WARNING, "Quote blocked for job {0} due to open blocking quote UW issues", jobNumber);
            dataStore.updateSubmission(period);
            return period;
        }

        period.setStatus(PCConstants.STATUS_QUOTED);
        dataStore.updateSubmission(period);
        LOGGER.log(Level.INFO, "Job {0} quoted successfully.", jobNumber);
        return period;
    }

    /**
     * Bind a Policy Submission branch with UW Authority Issue evaluation
     */
    public PolicyPeriod bindSubmissionBranch(String jobNumber) {
        PolicyPeriod period = dataStore.findSubmission(jobNumber);
        if (period == null) {
            throw new IllegalArgumentException("Job not found: " + jobNumber);
        }

        if (period.hasBlockingBindIssues()) {
            LOGGER.log(Level.WARNING, "Bind blocked for job {0} due to open blocking bind UW issues", jobNumber);
            throw new IllegalStateException("Cannot bind policy job " + jobNumber + " with open blocking Underwriting Issues.");
        }

        period.setStatus(PCConstants.STATUS_BOUND);
        dataStore.updateSubmission(period);
        LOGGER.log(Level.INFO, "Job {0} bound successfully.", jobNumber);
        return period;
    }

    /**
     * Start a Mid-Term Policy Rewrite job
     */
    public PolicyPeriod startRewrite(String policyNumber, String rewriteReason, String effectiveDateStr) {
        PolicyPeriod orig = dataStore.findPolicyByPolicyNumber(policyNumber);
        if (orig == null) {
            throw new IllegalArgumentException("Policy not found for rewrite: " + policyNumber);
        }

        orig.setStatus(PCConstants.STATUS_CANCELED);
        dataStore.updateSubmission(orig);

        PolicyPeriod rewrite = new PolicyPeriod();
        rewrite.setJobType(PCConstants.JOB_TYPE_REWRITE);
        rewrite.setJobNumber("RW00" + com.guidewire.pc.util.SequenceGenerator.nextId());
        rewrite.setPolicyNumber("POL-RW-" + com.guidewire.pc.util.SequenceGenerator.nextId());
        rewrite.setProductCode(orig.getProductCode());
        rewrite.setAccount(orig.getAccount());
        rewrite.setBaseState(orig.getBaseState());
        rewrite.setProducerCode(orig.getProducerCode());
        rewrite.setEffectiveDate(effectiveDateStr != null ? effectiveDateStr : orig.getEffectiveDate());
        rewrite.setExpirationDate(orig.getExpirationDate());
        rewrite.setTermMonths(orig.getTermMonths());
        rewrite.setBodilyInjuryLimit(orig.getBodilyInjuryLimit());
        rewrite.setPropertyDamageLimit(orig.getPropertyDamageLimit());
        rewrite.setComprehensiveDeductible(orig.getComprehensiveDeductible());
        rewrite.setCollisionDeductible(orig.getCollisionDeductible());

        RatingEngine.getInstance().rate(rewrite);
        rewrite.setStatus(PCConstants.STATUS_BOUND);
        dataStore.createSubmission(rewrite);

        LOGGER.log(Level.INFO, "Mid-Term Rewrite completed: Original {0} rewritten to {1} (Job: {2}, Reason: {3})",
                new Object[]{policyNumber, rewrite.getPolicyNumber(), rewrite.getJobNumber(), rewriteReason});
        return rewrite;
    }

    /**
     * Start a Rewrite New Account job
     */
    public PolicyPeriod startRewriteNewAccount(String policyNumber, String targetAccountNumber, String rewriteReason) {
        PolicyPeriod orig = dataStore.findPolicyByPolicyNumber(policyNumber);
        if (orig == null) {
            throw new IllegalArgumentException("Policy not found for rewrite new account: " + policyNumber);
        }

        com.guidewire.pc.model.Account targetAccount = dataStore.findAccountByNumber(targetAccountNumber);
        if (targetAccount == null) {
            targetAccount = new com.guidewire.pc.model.Account();
            targetAccount.setAccountNumber(targetAccountNumber);
            targetAccount.setAccountHolderName("Rewritten Account Holder");
            targetAccount.setAccountHolderType("Company");
            dataStore.createAccount(targetAccount);
        }

        orig.setStatus(PCConstants.STATUS_CANCELED);
        dataStore.updateSubmission(orig);

        PolicyPeriod rewrite = new PolicyPeriod();
        rewrite.setJobType(PCConstants.JOB_TYPE_REWRITE_NEW_ACCOUNT);
        rewrite.setJobNumber("RNA0" + com.guidewire.pc.util.SequenceGenerator.nextId());
        rewrite.setPolicyNumber("POL-RNA-" + com.guidewire.pc.util.SequenceGenerator.nextId());
        rewrite.setProductCode(orig.getProductCode());
        rewrite.setAccount(targetAccount);
        rewrite.setBaseState(orig.getBaseState());
        rewrite.setProducerCode(orig.getProducerCode());
        rewrite.setEffectiveDate(orig.getEffectiveDate());
        rewrite.setExpirationDate(orig.getExpirationDate());
        rewrite.setTermMonths(orig.getTermMonths());
        rewrite.setBodilyInjuryLimit(orig.getBodilyInjuryLimit());
        rewrite.setPropertyDamageLimit(orig.getPropertyDamageLimit());
        rewrite.setComprehensiveDeductible(orig.getComprehensiveDeductible());
        rewrite.setCollisionDeductible(orig.getCollisionDeductible());

        RatingEngine.getInstance().rate(rewrite);
        rewrite.setStatus(PCConstants.STATUS_BOUND);
        dataStore.createSubmission(rewrite);

        LOGGER.log(Level.INFO, "Rewrite New Account completed: Policy {0} transferred to Account {1} (New Policy: {2}, Job: {3})",
                new Object[]{policyNumber, targetAccountNumber, rewrite.getPolicyNumber(), rewrite.getJobNumber()});
        return rewrite;
    }

    private double calculateProRataFactor(Date start, Date end, Date effective) {
        if (start == null || end == null || effective == null) return 0.5;
        long totalMs = Math.max(1, end.getTime() - start.getTime());
        long remainingMs = Math.max(0, end.getTime() - effective.getTime());
        return Math.min(1.0, Math.max(0.0, (double) remainingMs / (double) totalMs));
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return new Date();
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (java.text.ParseException e) {
            return new Date();
        }
    }
}
