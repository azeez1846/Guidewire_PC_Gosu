package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AIUnderwritingAssistant {
    private static final Logger LOGGER = Logger.getLogger(AIUnderwritingAssistant.class.getName());

    private static final AIUnderwritingAssistant INSTANCE = new AIUnderwritingAssistant();

    private AIUnderwritingAssistant() {
        LOGGER.log(Level.FINE, "→ AIUnderwritingAssistant.AIUnderwritingAssistant");}

    public static AIUnderwritingAssistant getInstance() {
        LOGGER.log(Level.FINE, "→ AIUnderwritingAssistant.getInstance");
        return INSTANCE;
    }

    public Map<String, Object> triageSubmission(PolicyPeriod period, double lossRatio, int feinCreditScore) {
        LOGGER.log(Level.FINE, "→ AIUnderwritingAssistant.triageSubmission");
        Map<String, Object> result = new HashMap<>();

        if (period == null) {
            result.put("decision", "Rejected");
            result.put("reason", "PolicyPeriod parameter is null");
            return result;
        }

        BigDecimal totalPrem = period.getTotalPremium() != null ? period.getTotalPremium() : BigDecimal.ZERO;
        result.put("jobNumber", period.getJobNumber());
        result.put("lossRatio", lossRatio);
        result.put("feinCreditScore", feinCreditScore);

        // Underwriting Rules:
        // 1. Straight-Through Processing (STP) if Premium < $5,000, Loss Ratio == 0.0, Credit Score >= 700
        if (totalPrem.compareTo(new BigDecimal("5000")) < 0 && lossRatio == 0.0 && feinCreditScore >= 700) {
            result.put("decision", "STP_APPROVED");
            result.put("automationLevel", "Full Automation (No Human UW Needed)");
            result.put("action", "Auto-Quote & Ready to Bind");
        } else if (lossRatio > 0.40 || feinCreditScore < 600) {
            result.put("decision", "HIGH_RISK_REFERRAL");
            result.put("automationLevel", "Senior Underwriter Referral Required");
            result.put("action", "Trigger Senior UW Activity Escalation");
        } else {
            result.put("decision", "STANDARD_UW_REVIEW");
            result.put("automationLevel", "Standard Underwriter Review");
            result.put("action", "Assign to Regional Underwriting Queue");
        }

        return result;
    }
}
