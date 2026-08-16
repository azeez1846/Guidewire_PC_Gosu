package com.guidewire.pc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SOSEntityVerificationService {
    private static final Logger LOGGER = Logger.getLogger(SOSEntityVerificationService.class.getName());
    private static final SOSEntityVerificationService INSTANCE = new SOSEntityVerificationService();

    public static SOSEntityVerificationService getInstance() {
        return INSTANCE;
    }

    public static class EntityVerificationReport {
        public String searchBusinessName;
        public String fein;
        public String sosRegistrationNumber;
        public String stateOfFormation; // DE, CA, TX, NY, FL
        public String entityType; // LLC, C-CORP, S-CORP, PARTNERSHIP
        public String filingStatus; // ACTIVE_GOOD_STANDING, SUSPENDED, DISSOLVED, DELINQUENT
        public int yearsInBusiness;
        public List<String> registeredOfficers = new ArrayList<>();
        public String registeredAgentName;
        public int dnbPaydexScore; // 0-100 (80+ = Prompt payment)
        public int financialStressScore; // 1 (Lowest Risk) to 5 (Severe Insolvency Risk)
        public boolean isShellCompanyRisk;
        public boolean isEligibleToBind;
        public String underwriterNotes;
    }

    public EntityVerificationReport verifyBusinessEntity(String businessName, String fein, String state) {
        LOGGER.log(Level.FINE, "→ SOSEntityVerificationService.verifyBusinessEntity for: " + businessName);
        EntityVerificationReport report = new EntityVerificationReport();
        report.searchBusinessName = businessName != null ? businessName : "Apex Global Industrial LLC";
        report.fein = fein != null ? fein : "94-8192014";
        report.stateOfFormation = state != null ? state : "DE";

        String lowerName = report.searchBusinessName.toLowerCase();
        if (lowerName.contains("fraud") || lowerName.contains("shell") || lowerName.contains("suspended")) {
            report.sosRegistrationNumber = "SOS-DELINQ-00912";
            report.entityType = "LLC";
            report.filingStatus = "SUSPENDED";
            report.yearsInBusiness = 0;
            report.registeredOfficers.add("Unknown Nominee Director");
            report.registeredAgentName = "Virtual Mailbox Forwarding Inc";
            report.dnbPaydexScore = 32;
            report.financialStressScore = 5;
            report.isShellCompanyRisk = true;
            report.isEligibleToBind = false;
            report.underwriterNotes = "CRITICAL: Corporate charter is SUSPENDED by Secretary of State. High shell company risk detected. Binding prohibited.";
        } else if (lowerName.contains("new") || lowerName.contains("startup") || report.fein.endsWith("99")) {
            report.sosRegistrationNumber = "SOS-2025-881920";
            report.entityType = "LLC";
            report.filingStatus = "ACTIVE_GOOD_STANDING";
            report.yearsInBusiness = 1;
            report.registeredOfficers.add("David Miller (Managing Member)");
            report.registeredAgentName = "CSC Lawyers Incorporating Service";
            report.dnbPaydexScore = 72;
            report.financialStressScore = 2;
            report.isShellCompanyRisk = false;
            report.isEligibleToBind = true;
            report.underwriterNotes = "Entity is in Good Standing. Less than 2 years in business: standard loss monitoring recommended.";
        } else {
            report.sosRegistrationNumber = "SOS-DE-C049102";
            report.entityType = "C-CORP";
            report.filingStatus = "ACTIVE_GOOD_STANDING";
            report.yearsInBusiness = 14;
            report.registeredOfficers.add("Jonathan Hayes (CEO)");
            report.registeredOfficers.add("Elena Rostova (CFO)");
            report.registeredOfficers.add("Marcus Vance (General Counsel)");
            report.registeredAgentName = "The Corporation Trust Company (CT Corp)";
            report.dnbPaydexScore = 86;
            report.financialStressScore = 1;
            report.isShellCompanyRisk = false;
            report.isEligibleToBind = true;
            report.underwriterNotes = "Established commercial entity with excellent D&B Paydex payment rating (86/100). Approved for straight-through binding.";
        }

        return report;
    }

    public Map<String, Object> toMap(EntityVerificationReport r) {
        Map<String, Object> map = new HashMap<>();
        map.put("searchBusinessName", r.searchBusinessName);
        map.put("fein", r.fein);
        map.put("sosRegistrationNumber", r.sosRegistrationNumber);
        map.put("stateOfFormation", r.stateOfFormation);
        map.put("entityType", r.entityType);
        map.put("filingStatus", r.filingStatus);
        map.put("yearsInBusiness", r.yearsInBusiness);
        map.put("registeredOfficers", r.registeredOfficers);
        map.put("registeredAgentName", r.registeredAgentName);
        map.put("dnbPaydexScore", r.dnbPaydexScore);
        map.put("financialStressScore", r.financialStressScore);
        map.put("isShellCompanyRisk", r.isShellCompanyRisk);
        map.put("isEligibleToBind", r.isEligibleToBind);
        map.put("underwriterNotes", r.underwriterNotes);
        map.put("status", "SUCCESS");
        return map;
    }
}
