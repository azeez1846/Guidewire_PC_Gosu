package com.guidewire.pc.service;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyForm;
import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PolicyFormInferenceEngine {
    private static final Logger LOGGER = Logger.getLogger(PolicyFormInferenceEngine.class.getName());
    private static final PolicyFormInferenceEngine instance = new PolicyFormInferenceEngine();

    public PolicyFormInferenceEngine() {
        LOGGER.log(Level.FINE, "→ PolicyFormInferenceEngine.PolicyFormInferenceEngine");}

    public static PolicyFormInferenceEngine getInstance() {
        LOGGER.log(Level.FINE, "→ PolicyFormInferenceEngine.getInstance");
        return instance;
    }

    public static List<PolicyForm> inferPolicyForms(PolicyPeriod period) {
        LOGGER.log(Level.FINE, "→ PolicyFormInferenceEngine.inferPolicyForms");
        List<PolicyForm> forms = new ArrayList<>();
        if (period == null) return forms;

        String prodCode = period.getProductCode() != null ? period.getProductCode() : PCConstants.PRODUCT_COMMERCIAL_AUTO;
        String state = period.getBaseState() != null ? period.getBaseState() : "CA";

        // 1. Mandatory Common Policy Jacket Forms
        forms.add(new PolicyForm("IL 00 17", "Common Policy Conditions", "11 98", true, state, "Mandatory Common Policy Jacket"));
        forms.add(new PolicyForm("IL 00 21", "Nuclear Energy Liability Exclusion Endorsement", "11 98", true, state, "Mandatory Nuclear Liability Exclusion"));

        // 2. Line of Business Specific Mandatory Forms
        if ("CommercialAuto".equalsIgnoreCase(prodCode) || PCConstants.PRODUCT_COMMERCIAL_AUTO.equalsIgnoreCase(prodCode)) {
            forms.add(new PolicyForm("CA 00 01", "Business Auto Coverage Form", "10 13", true, state, "Mandatory Business Auto Form"));
        } else if ("CommercialProperty".equalsIgnoreCase(prodCode) || PCConstants.PRODUCT_COMMERCIAL_PROPERTY.equalsIgnoreCase(prodCode)) {
            forms.add(new PolicyForm("CP 00 10", "Building and Personal Property Coverage Form", "10 12", true, state, "Mandatory Commercial Property Form"));
            forms.add(new PolicyForm("CP 10 30", "Causes of Loss - Special Form", "10 12", true, state, "Special Causes of Loss Form"));
        } else if (PCConstants.PRODUCT_GENERAL_LIABILITY.equalsIgnoreCase(prodCode)) {
            forms.add(new PolicyForm("CG 00 01", "Commercial General Liability Coverage Form", "04 13", true, state, "Mandatory CGL Form"));
        } else if (PCConstants.PRODUCT_WORKERS_COMP.equalsIgnoreCase(prodCode)) {
            forms.add(new PolicyForm("WC 00 00", "Workers Compensation Policy Form", "00 C", true, state, "Mandatory Workers Comp Form"));
        } else if (PCConstants.PRODUCT_INLAND_MARINE.equalsIgnoreCase(prodCode)) {
            forms.add(new PolicyForm("IM 70 00", "Contractors Equipment Coverage Form", "04 13", true, state, "Mandatory Inland Marine Form"));
        }

        // 3. State-Specific Statutory Notices
        if ("FL".equalsIgnoreCase(state)) {
            forms.add(new PolicyForm("IL 01 02", "Florida Statutory Notice", "05 18", true, "FL", "Florida Statutory Insured Notice"));
        } else if ("CA".equalsIgnoreCase(state)) {
            forms.add(new PolicyForm("IL 01 04", "California Statutory Notice", "08 20", true, "CA", "California Statutory Insured Notice"));
        } else if ("TX".equalsIgnoreCase(state)) {
            forms.add(new PolicyForm("IL 01 08", "Texas Statutory Notice", "01 19", true, "TX", "Texas Statutory Insured Notice"));
        }

        // 4. TRIA Disclosure Form (Inferred for high premium > $10,000 or UW Issues)
        if ((period.getTotalPremium() != null && period.getTotalPremium().compareTo(new BigDecimal("10000.00")) > 0) || !period.getUwIssues().isEmpty()) {
            forms.add(new PolicyForm("IL 09 85", "Disclosure Pursuant to Terrorism Risk Insurance Act", "01 15", false, state, "TRIA Terrorism Risk Disclosure"));
        }

        LOGGER.log(Level.INFO, "Policy Form Inference completed for policy {0}: {1} forms attached",
                new Object[]{period.getPolicyNumber(), forms.size()});

        return forms;
    }
}
