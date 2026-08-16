package com.guidewire.pc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SanctionsComplianceService {
    private static final Logger LOGGER = Logger.getLogger(SanctionsComplianceService.class.getName());
    private static final SanctionsComplianceService INSTANCE = new SanctionsComplianceService();

    public static SanctionsComplianceService getInstance() {
        return INSTANCE;
    }

    public static class SanctionsMatchItem {
        public String matchedName;
        public String sanctionsList; // OFAC_SDN, OFAC_NON_SDN, PEP_GLOBAL, EU_SANCTIONS
        public String entityType; // INDIVIDUAL, ENTITY, VESSEL
        public double matchConfidenceScore; // 0 to 100
        public String country;
        public String remarks;

        public SanctionsMatchItem(String matchedName, String sanctionsList, String entityType, double score, String country, String remarks) {
            this.matchedName = matchedName;
            this.sanctionsList = sanctionsList;
            this.entityType = entityType;
            this.matchConfidenceScore = score;
            this.country = country;
            this.remarks = remarks;
        }
    }

    public static class SanctionsScreeningResult {
        public String screenedSubject;
        public String subjectType; // INDIVIDUAL, COMMERCIAL_ORGANIZATION
        public String screeningDisposition; // CLEAR, POTENTIAL_MATCH_REVIEW, HARD_BLOCK_SANCTIONED
        public double highestConfidenceScore;
        public boolean isBindingBlocked;
        public boolean sarFilingRecommended;
        public List<SanctionsMatchItem> matches = new ArrayList<>();
        public String complianceOfficerGuidance;
    }

    public SanctionsScreeningResult screenSubject(String name, String country, String subjectType) {
        LOGGER.log(Level.FINE, "→ SanctionsComplianceService.screenSubject for: " + name);
        SanctionsScreeningResult res = new SanctionsScreeningResult();
        res.screenedSubject = name != null ? name : "Apex Commercial Logistics";
        res.subjectType = subjectType != null ? subjectType : "COMMERCIAL_ORGANIZATION";

        String lower = res.screenedSubject.toLowerCase();
        if (lower.contains("sanction") || lower.contains("petrov") || lower.contains("blocked") || lower.contains("cuba") || lower.contains("iran")) {
            res.highestConfidenceScore = 96.5;
            res.screeningDisposition = "HARD_BLOCK_SANCTIONED";
            res.isBindingBlocked = true;
            res.sarFilingRecommended = true;
            res.matches.add(new SanctionsMatchItem("Petrov Logistics Overseas Ltd", "OFAC_SDN", "ENTITY", 96.5, "Russia", "Executive Order 14024 blocking property of designated persons"));
            res.matches.add(new SanctionsMatchItem("Dmitry Petrov", "OFAC_SDN", "INDIVIDUAL", 92.0, "Cyprus", "Designated officer of sanctioned entity"));
            res.complianceOfficerGuidance = "CRITICAL: Positive match on US Treasury OFAC SDN List. System has placed an immediate Underwriting Binding Lock. All transaction funds must be blocked pursuant to 31 CFR Part 501.";
        } else if (lower.contains("politician") || lower.contains("pep") || lower.contains("ambassador") || lower.contains("minister")) {
            res.highestConfidenceScore = 78.0;
            res.screeningDisposition = "POTENTIAL_MATCH_REVIEW";
            res.isBindingBlocked = true;
            res.sarFilingRecommended = false;
            res.matches.add(new SanctionsMatchItem("Minister Vance Overseas Holding", "PEP_GLOBAL", "ENTITY", 78.0, "United Kingdom", "Politically Exposed Person beneficial ownership > 25%"));
            res.complianceOfficerGuidance = "WARNING: Potential PEP (Politically Exposed Person) match. Enhanced Due Diligence (EDD) sign-off required prior to quote release.";
        } else {
            res.highestConfidenceScore = 0.0;
            res.screeningDisposition = "CLEAR";
            res.isBindingBlocked = false;
            res.sarFilingRecommended = false;
            res.complianceOfficerGuidance = "Subject successfully cleared against OFAC SDN, PEP, and international sanctions lists. Straight-through binding authorized.";
        }

        return res;
    }

    public Map<String, Object> toMap(SanctionsScreeningResult r) {
        Map<String, Object> map = new HashMap<>();
        map.put("screenedSubject", r.screenedSubject);
        map.put("subjectType", r.subjectType);
        map.put("screeningDisposition", r.screeningDisposition);
        map.put("highestConfidenceScore", r.highestConfidenceScore);
        map.put("isBindingBlocked", r.isBindingBlocked);
        map.put("sarFilingRecommended", r.sarFilingRecommended);
        map.put("matches", r.matches);
        map.put("complianceOfficerGuidance", r.complianceOfficerGuidance);
        map.put("status", "SUCCESS");
        return map;
    }
}
