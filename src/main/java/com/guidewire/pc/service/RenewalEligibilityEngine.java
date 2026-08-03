package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RenewalEligibilityEngine {
    private static final Logger LOGGER = Logger.getLogger(RenewalEligibilityEngine.class.getName());
    private static final RenewalEligibilityEngine instance = new RenewalEligibilityEngine();

    private RenewalEligibilityEngine() {}

    public static RenewalEligibilityEngine getInstance() {
        return instance;
    }

    public RenewalEligibilityResult evaluateRenewalEligibility(PolicyPeriod period, double proposedRateIncreasePct) {
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

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public boolean isRenewalEligible() { return renewalEligible; }
        public void setRenewalEligible(boolean renewalEligible) { this.renewalEligible = renewalEligible; }

        public String getDecisionAction() { return decisionAction; }
        public void setDecisionAction(String decisionAction) { this.decisionAction = decisionAction; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public int getPriorClaimsCount() { return priorClaimsCount; }
        public void setPriorClaimsCount(int priorClaimsCount) { this.priorClaimsCount = priorClaimsCount; }

        public BigDecimal getTotalPriorLosses() { return totalPriorLosses; }
        public void setTotalPriorLosses(BigDecimal totalPriorLosses) { this.totalPriorLosses = totalPriorLosses; }

        public double getProposedRateIncreasePct() { return proposedRateIncreasePct; }
        public void setProposedRateIncreasePct(double proposedRateIncreasePct) { this.proposedRateIncreasePct = proposedRateIncreasePct; }

        public boolean isStatutoryNoticeRequired() { return statutoryNoticeRequired; }
        public void setStatutoryNoticeRequired(boolean statutoryNoticeRequired) { this.statutoryNoticeRequired = statutoryNoticeRequired; }

        public int getNoticeDays() { return noticeDays; }
        public void setNoticeDays(int noticeDays) { this.noticeDays = noticeDays; }
    }
}
