package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AI Autonomous Risk Intake & Underwriting Triage Service.
 * Evaluates incoming submission risk characteristics, calculates AI confidence scores,
 * and assigns triage decisions (AUTO_ACCEPT, UNDERWRITER_REFERRAL, DECLINE).
 */
public class AIAutonomousRiskIntakeService {
    private static final Logger LOGGER = Logger.getLogger(AIAutonomousRiskIntakeService.class.getName());
    private static final AIAutonomousRiskIntakeService instance = new AIAutonomousRiskIntakeService();

    private AIAutonomousRiskIntakeService() {
        LOGGER.log(Level.FINE, "AIAutonomousRiskIntakeService initialized");
    }

    public static AIAutonomousRiskIntakeService getInstance() {
        return instance;
    }

    public TriageResult evaluateSubmission(PolicyPeriod period, String lineOfBusiness, BigDecimal annualRevenue, int lossHistoryCount, boolean hasHazardousMaterials) {
        if (lineOfBusiness == null) lineOfBusiness = "CommercialProperty";
        if (annualRevenue == null) annualRevenue = new BigDecimal("2500000.00");

        TriageResult result = new TriageResult();
        result.setSubmissionNumber(period != null ? period.getPolicyNumber() : "SUB-AI-2026-99");
        result.setLineOfBusiness(lineOfBusiness);
        result.setAnnualRevenue(annualRevenue);
        result.setLossHistoryCount(lossHistoryCount);
        result.setHasHazardousMaterials(hasHazardousMaterials);

        List<String> riskFactors = new ArrayList<>();
        double baseConfidence = 0.95;

        if (lossHistoryCount > 3) {
            riskFactors.add("High frequency claims history (" + lossHistoryCount + " claims in 3 yrs)");
            baseConfidence -= 0.20;
        }

        if (hasHazardousMaterials) {
            riskFactors.add("Presence of hazardous chemical storage on site");
            baseConfidence -= 0.15;
        }

        if (annualRevenue.compareTo(new BigDecimal("10000000.00")) > 0) {
            riskFactors.add("Large commercial account ($10M+ Revenue)");
            baseConfidence -= 0.10;
        }

        result.setRiskFactors(riskFactors);
        result.setAiConfidenceScore(Math.max(0.40, Math.min(1.0, baseConfidence)));

        // Triage Logic
        if (lossHistoryCount > 4 || (hasHazardousMaterials && annualRevenue.compareTo(new BigDecimal("15000000.00")) > 0)) {
            result.setRecommendation("DECLINE");
            result.setRationale("Risk exceeds carrier underwriting appetite due to severe loss history or unmitigated hazardous exposures.");
        } else if (!riskFactors.isEmpty()) {
            result.setRecommendation("UNDERWRITER_REFERRAL");
            result.setRationale("Requires Underwriter review due to detected risk factors: " + String.join("; ", riskFactors));
        } else {
            result.setRecommendation("AUTO_ACCEPT");
            result.setRationale("Clean risk profile matching standard carrier guidelines. Eligible for instant straight-through processing (STP).");
        }

        LOGGER.log(Level.INFO, "AI Risk Triage completed for submission {0}: Status={1}, Confidence={2}, Rationale={3}",
                new Object[]{result.getSubmissionNumber(), result.getRecommendation(), result.getAiConfidenceScore(), result.getRationale()});

        return result;
    }

    public static class TriageResult {
        private String submissionNumber;
        private String lineOfBusiness;
        private BigDecimal annualRevenue;
        private int lossHistoryCount;
        private boolean hasHazardousMaterials;
        private double aiConfidenceScore;
        private String recommendation; // AUTO_ACCEPT, UNDERWRITER_REFERRAL, DECLINE
        private String rationale;
        private List<String> riskFactors = new ArrayList<>();

        public String getSubmissionNumber() { return submissionNumber; }
        public void setSubmissionNumber(String submissionNumber) { this.submissionNumber = submissionNumber; }

        public String getLineOfBusiness() { return lineOfBusiness; }
        public void setLineOfBusiness(String lineOfBusiness) { this.lineOfBusiness = lineOfBusiness; }

        public BigDecimal getAnnualRevenue() { return annualRevenue; }
        public void setAnnualRevenue(BigDecimal annualRevenue) { this.annualRevenue = annualRevenue; }

        public int getLossHistoryCount() { return lossHistoryCount; }
        public void setLossHistoryCount(int lossHistoryCount) { this.lossHistoryCount = lossHistoryCount; }

        public boolean isHasHazardousMaterials() { return hasHazardousMaterials; }
        public void setHasHazardousMaterials(boolean hasHazardousMaterials) { this.hasHazardousMaterials = hasHazardousMaterials; }

        public double getAiConfidenceScore() { return aiConfidenceScore; }
        public void setAiConfidenceScore(double aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }

        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }

        public List<String> getRiskFactors() { return riskFactors; }
        public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    }
}
