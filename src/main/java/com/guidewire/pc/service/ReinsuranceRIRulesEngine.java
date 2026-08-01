package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.RIAgreement;
import com.guidewire.pc.model.RICession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReinsuranceRIRulesEngine {

    public static List<String> evaluateReinsuranceRules(PolicyPeriod period, RIAgreement agreement, BigDecimal grossExposure) {
        List<String> issues = new ArrayList<>();
        if (period == null || agreement == null) return issues;

        RICession cession = ReinsuranceService.calculateCession(period, agreement, grossExposure);
        if (cession != null && cession.isRequiresFacultative()) {
            issues.add("UW_FACULTATIVE_REINSURANCE_REQUIRED: Policy gross limit $" + grossExposure + " exceeds treaty retention limit $" + agreement.getGrossRetentionLimit());
        }

        if (cession != null && cession.getCededExposure().compareTo(BigDecimal.ZERO) > 0) {
            issues.add("UW_REINSURANCE_TREATY_ATTACHED: " + agreement.getAgreementType() + " treaty " + agreement.getAgreementNumber() + " attached with ceded exposure $" + cession.getCededExposure());
        }

        return issues;
    }
}
