package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RenewalEligibilityEngine {
    private static final Logger LOGGER = Logger.getLogger(RenewalEligibilityEngine.class.getName());
    private static final RenewalEligibilityEngine instance = new RenewalEligibilityEngine();

    private RenewalEligibilityEngine() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.RenewalEligibilityEngine");}

    public static RenewalEligibilityEngine getInstance() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.getInstance");
        return instance;
    }

    public RenewalEligibilityResult evaluateRenewalEligibility(PolicyPeriod period, double proposedRateIncreasePct) {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.evaluateRenewalEligibility");
        RenewalEligibilityResult result = new RenewalEligibilityResult();
        if (period == null) return result;

        result.setPolicyNumber(period.getPolicyNumber());
        result.setProposedRateIncreasePct(proposedRateIncreasePct);

        var claims = ClaimCenterService.getInstance().getClaimsForPolicy(period.getPolicyNumber());
        int claimCount = claims.size();
        BigDecimal totalLosses = BigDecimal.ZERO;
        for (var c : claims) {
            if (c.getLossAmount() != null) {
                totalLosses = totalLosses.add(c.getLossAmount());
            }
        }

        result.setPriorClaimsCount(claimCount);
        result.setTotalPriorLosses(totalLosses);

        // Non-Renewal Trigger Rules (>3 claims or >$50k losses)
        if (claimCount > 3 || totalLosses.compareTo(new BigDecimal("50000.00")) > 0) {
            result.setRenewalEligible(false);
            result.setDecisionAction("NON_RENEW_ISSUED");
            result.setReason("Excessive loss frequency/severity: " + claimCount + " claims totalling $" + totalLosses);
        } else {
            result.setRenewalEligible(true);
            if (proposedRateIncreasePct >= 0.15) {
                result.setDecisionAction("RENEWAL_CONDITIONAL_NOTICE_REQUIRED");
                result.setStatutoryNoticeRequired(true);
                result.setNoticeDays(45);
                result.setReason("Proposed premium increase (" + (int)(proposedRateIncreasePct * 100) + "%) exceeds statutory 15% notice threshold.");
            } else {
                result.setDecisionAction("RENEWAL_AUTO_OFFER");
                result.setStatutoryNoticeRequired(false);
                result.setReason("Policy eligible for standard automated renewal offer.");
            }
        }

        LOGGER.log(Level.INFO, "Renewal Eligibility evaluated for policy {0}: Action={1}, Reason={2}",
                new Object[]{period.getPolicyNumber(), result.getDecisionAction(), result.getReason()});

        return result;
    }

    public static class RenewalEligibilityResult {
        private String policyNumber;
        private boolean renewalEligible;
        private String decisionAction;
        private String reason;
        private int priorClaimsCount;
        private BigDecimal totalPriorLosses = BigDecimal.ZERO;
        private double proposedRateIncreasePct;
        private boolean statutoryNoticeRequired;
        private int noticeDays;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public boolean isRenewalEligible() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.isRenewalEligible"); return renewalEligible; }
        public void setRenewalEligible(boolean renewalEligible) {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.setRenewalEligible"); this.renewalEligible = renewalEligible; }

        public String getDecisionAction() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.getDecisionAction"); return decisionAction; }
        public void setDecisionAction(String decisionAction) {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.setDecisionAction"); this.decisionAction = decisionAction; }

        public String getReason() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.getReason"); return reason; }
        public void setReason(String reason) {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.setReason"); this.reason = reason; }

        public int getPriorClaimsCount() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.getPriorClaimsCount"); return priorClaimsCount; }
        public void setPriorClaimsCount(int priorClaimsCount) {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.setPriorClaimsCount"); this.priorClaimsCount = priorClaimsCount; }

        public BigDecimal getTotalPriorLosses() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.getTotalPriorLosses"); return totalPriorLosses; }
        public void setTotalPriorLosses(BigDecimal totalPriorLosses) {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.setTotalPriorLosses"); this.totalPriorLosses = totalPriorLosses; }

        public double getProposedRateIncreasePct() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.getProposedRateIncreasePct"); return proposedRateIncreasePct; }
        public void setProposedRateIncreasePct(double proposedRateIncreasePct) {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.setProposedRateIncreasePct"); this.proposedRateIncreasePct = proposedRateIncreasePct; }

        public boolean isStatutoryNoticeRequired() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.isStatutoryNoticeRequired"); return statutoryNoticeRequired; }
        public void setStatutoryNoticeRequired(boolean statutoryNoticeRequired) {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.setStatutoryNoticeRequired"); this.statutoryNoticeRequired = statutoryNoticeRequired; }

        public int getNoticeDays() {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.getNoticeDays"); return noticeDays; }
        public void setNoticeDays(int noticeDays) {
        LOGGER.log(Level.FINE, "→ RenewalEligibilityEngine.setNoticeDays"); this.noticeDays = noticeDays; }
    }
}
