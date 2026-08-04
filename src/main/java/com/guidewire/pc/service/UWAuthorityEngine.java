package com.guidewire.pc.service;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.UWIssue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UWAuthorityEngine {
    private static final Logger LOGGER = Logger.getLogger(UWAuthorityEngine.class.getName());
    private static final UWAuthorityEngine instance = new UWAuthorityEngine();

    private UWAuthorityEngine() {
        LOGGER.log(Level.FINE, "→ UWAuthorityEngine.UWAuthorityEngine");}

    public static UWAuthorityEngine getInstance() {
        LOGGER.log(Level.FINE, "→ UWAuthorityEngine.getInstance");
        return instance;
    }

    /**
     * Evaluates policy period against OOTB Guidewire Underwriting Rules
     * and attaches generated UWIssues to the PolicyPeriod.
     */
    public List<UWIssue> evaluatePolicy(PolicyPeriod period) {
        LOGGER.log(Level.FINE, "→ UWAuthorityEngine.evaluatePolicy");
        if (period == null) return new ArrayList<>();

        List<UWIssue> generatedIssues = new ArrayList<>();

        // Rule 1: High Total Premium Threshold ($50,000)
        if (period.getTotalPremium() != null && period.getTotalPremium().compareTo(new BigDecimal("50000.00")) > 0) {
            UWIssue issue = new UWIssue(
                    "UW_HIGH_PREMIUM",
                    "High Policy Premium > $50,000",
                    "Policy total premium of $" + period.getTotalPremium() + " exceeds standard Underwriter binding authority limit of $50,000.00.",
                    PCConstants.UW_SEVERITY_BLOCKING_BIND,
                    "SeniorUnderwriter"
            );
            generatedIssues.add(issue);
        }

        // Rule 2: High Bodily Injury Limit ($1M/$2M/$5M)
        if (period.getBodilyInjuryLimit() != null &&
           (period.getBodilyInjuryLimit().contains("1M") || period.getBodilyInjuryLimit().contains("2M") || period.getBodilyInjuryLimit().contains("5M"))) {
            UWIssue issue = new UWIssue(
                    "UW_HIGH_BI_LIMIT",
                    "High Bodily Injury Coverage Limit",
                    "Bodily Injury coverage limit of " + period.getBodilyInjuryLimit() + " requires Executive Underwriter sign-off.",
                    PCConstants.UW_SEVERITY_BLOCKING_BIND,
                    "ExecutiveUnderwriter"
            );
            generatedIssues.add(issue);
        }

        // Rule 3: High Risk Base State / Coastal Territory (e.g. FL, LA, TX coastal)
        if ("FL".equalsIgnoreCase(period.getBaseState()) || "LA".equalsIgnoreCase(period.getBaseState())) {
            UWIssue issue = new UWIssue(
                    "UW_COASTAL_TERRITORY",
                    "High Exposure Coastal Territory (" + period.getBaseState() + ")",
                    "Policy property/auto location in coastal state " + period.getBaseState() + " requires catastrophe underwriting review.",
                    PCConstants.UW_SEVERITY_BLOCKING_QUOTE,
                    "SeniorUnderwriter"
            );
            generatedIssues.add(issue);
        }

        // Rule 4: Workers' Comp Hazardous Class Code
        if (PCConstants.PRODUCT_WORKERS_COMP.equalsIgnoreCase(period.getProductCode())) {
            UWIssue issue = new UWIssue(
                    "UW_WC_CLASS_CODE",
                    "Workers' Comp Specialty Class Code Review",
                    "Workers' Compensation classification requires occupational safety audit and referral check.",
                    PCConstants.UW_SEVERITY_INFORMATIONAL,
                    "Underwriter"
            );
            generatedIssues.add(issue);
        }

        // Attach issues to policy period avoiding duplicates
        for (UWIssue newIssue : generatedIssues) {
            boolean exists = period.getUwIssues().stream().anyMatch(existing -> existing.getIssueCode().equalsIgnoreCase(newIssue.getIssueCode()));
            if (!exists) {
                period.addUWIssue(newIssue);
            }
        }

        DataStoreService.getInstance().updateSubmission(period);

        LOGGER.log(Level.INFO, "UW Authority Engine evaluated policy {0} (Job: {1}). Found {2} UW issues.",
                new Object[]{period.getPolicyNumber(), period.getJobNumber(), generatedIssues.size()});

        return generatedIssues;
    }
}
