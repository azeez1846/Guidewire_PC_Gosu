package com.guidewire.pc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LossControlInspectionService {
    private static final Logger LOGGER = Logger.getLogger(LossControlInspectionService.class.getName());
    private static final LossControlInspectionService INSTANCE = new LossControlInspectionService();

    public static LossControlInspectionService getInstance() {
        return INSTANCE;
    }

    public static class SafetyRecommendation {
        public String recCode;
        public String title;
        public String severity; // MANDATORY, ADVISORY
        public int complianceDeadlineDays;
        public String status; // OPEN, COMPLETED, OVERDUE
        public String correctiveAction;

        public SafetyRecommendation(String recCode, String title, String severity, int days, String status, String correctiveAction) {
            this.recCode = recCode;
            this.title = title;
            this.severity = severity;
            this.complianceDeadlineDays = days;
            this.status = status;
            this.correctiveAction = correctiveAction;
        }
    }

    public static class InspectionSurveyReport {
        public String surveyId;
        public String policyNumber;
        public String inspectedLocation;
        public String inspectorEngineer;
        public String surveyDate;
        public int overallRiskScore; // 0-100 (Higher is safer)
        public List<SafetyRecommendation> recommendations = new ArrayList<>();
        public int openMandatoryCount;
        public boolean triggersDirectNoticeOfCancellation;
        public String underwritingActionRequired;
    }

    public InspectionSurveyReport generateSurveyReport(String policyNumber, String locationAddress, boolean hasCriticalElectricalFlaw, boolean hasCookingHazards) {
        LOGGER.log(Level.FINE, "→ LossControlInspectionService.generateSurveyReport for: " + policyNumber);
        InspectionSurveyReport survey = new InspectionSurveyReport();
        survey.surveyId = "SRV-LC-" + (System.currentTimeMillis() % 100000);
        survey.policyNumber = policyNumber != null ? policyNumber : "POL-COMM-8801";
        survey.inspectedLocation = locationAddress != null ? locationAddress : "100 Industrial Pkwy, Building A";
        survey.inspectorEngineer = "Senior Risk Control Engineer Robert Sterling, CSP";
        survey.surveyDate = "2026-08-16";

        if (hasCriticalElectricalFlaw) {
            survey.recommendations.add(new SafetyRecommendation("REC-ELEC-01", "Replace Obsolete FPE Stab-Lok Electrical Panel", "MANDATORY", 30, "OVERDUE", "Replace unlisted circuit breaker panel with modern UL-listed Square D panel to prevent arcing fire hazard."));
        }

        if (hasCookingHazards) {
            survey.recommendations.add(new SafetyRecommendation("REC-FIRE-02", "Install UL-300 Automatic Kitchen Suppression", "MANDATORY", 60, "OPEN", "Retrofit kitchen grease hood with NFPA 96 / UL-300 compliant wet chemical fire suppression system."));
        }

        // Advisory standard recommendation
        survey.recommendations.add(new SafetyRecommendation("REC-GEN-03", "Maintain Annual Fire Extinguisher Inspection Tags", "ADVISORY", 90, "COMPLETED", "Ensure all ABC dry chemical extinguishers possess up-to-date monthly inspection tags."));

        int mandatoryOpen = 0;
        boolean overdueFound = false;
        for (SafetyRecommendation r : survey.recommendations) {
            if ("MANDATORY".equalsIgnoreCase(r.severity) && !"COMPLETED".equalsIgnoreCase(r.status)) {
                mandatoryOpen++;
                if ("OVERDUE".equalsIgnoreCase(r.status)) {
                    overdueFound = true;
                }
            }
        }
        survey.openMandatoryCount = mandatoryOpen;
        survey.triggersDirectNoticeOfCancellation = overdueFound;

        if (survey.triggersDirectNoticeOfCancellation) {
            survey.overallRiskScore = 38;
            survey.underwritingActionRequired = "CRITICAL NON-COMPLIANCE: Overdue mandatory safety recommendations detected. Issue 30-Day Direct Notice of Cancellation (DNOC) pursuant to policy conditions.";
        } else if (mandatoryOpen > 0) {
            survey.overallRiskScore = 72;
            survey.underwritingActionRequired = "MONITOR COMPLIANCE: 60-day diary set for loss control engineer reinspection.";
        } else {
            survey.overallRiskScore = 95;
            survey.underwritingActionRequired = "PREFERRED RISK: Loss control survey successfully cleared with 0 open mandatory recommendations.";
        }

        return survey;
    }

    public Map<String, Object> toMap(InspectionSurveyReport s) {
        Map<String, Object> map = new HashMap<>();
        map.put("surveyId", s.surveyId);
        map.put("policyNumber", s.policyNumber);
        map.put("inspectedLocation", s.inspectedLocation);
        map.put("inspectorEngineer", s.inspectorEngineer);
        map.put("surveyDate", s.surveyDate);
        map.put("overallRiskScore", s.overallRiskScore);
        map.put("recommendations", s.recommendations);
        map.put("openMandatoryCount", s.openMandatoryCount);
        map.put("triggersDirectNoticeOfCancellation", s.triggersDirectNoticeOfCancellation);
        map.put("underwritingActionRequired", s.underwritingActionRequired);
        map.put("status", "SUCCESS");
        return map;
    }
}
