package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.RIAgreement;
import com.guidewire.pc.model.RICession;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ReinsuranceService {

    public static RICession calculateCession(PolicyPeriod period, RIAgreement agreement, BigDecimal grossLimit) {
        if (period == null || agreement == null) return null;

        RICession cession = new RICession();
        BigDecimal grossExposure = grossLimit != null ? grossLimit : new BigDecimal("1000000.00");
        BigDecimal grossPremium = period.getTotalPremium() != null ? period.getTotalPremium() : BigDecimal.ZERO;

        cession.setGrossRiskExposure(grossExposure);
        cession.setGrossWrittenPremium(grossPremium);

        if ("QuotaShare".equalsIgnoreCase(agreement.getAgreementType())) {
            BigDecimal pct = agreement.getCedingPercentage().divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
            BigDecimal cededExp = grossExposure.multiply(pct).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cededPrem = grossPremium.multiply(pct).setScale(2, RoundingMode.HALF_UP);

            cession.setCededExposure(cededExp);
            cession.setRetainedExposure(grossExposure.subtract(cededExp));
            cession.setCededPremium(cededPrem);
        } else if ("ExcessOfLoss".equalsIgnoreCase(agreement.getAgreementType())) {
            BigDecimal attach = agreement.getAttachmentPoint() != null ? agreement.getAttachmentPoint() : BigDecimal.ZERO;
            if (grossExposure.compareTo(attach) > 0) {
                BigDecimal excess = grossExposure.subtract(attach);
                BigDecimal limit = agreement.getGrossRetentionLimit();
                BigDecimal cededExp = excess.compareTo(limit) > 0 ? limit : excess;
                BigDecimal retainedExp = grossExposure.subtract(cededExp);

                BigDecimal ratio = grossExposure.compareTo(BigDecimal.ZERO) > 0 ?
                    cededExp.divide(grossExposure, 6, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                BigDecimal cededPrem = grossPremium.multiply(ratio).setScale(2, RoundingMode.HALF_UP);

                cession.setCededExposure(cededExp);
                cession.setRetainedExposure(retainedExp);
                cession.setCededPremium(cededPrem);
            } else {
                cession.setCededExposure(BigDecimal.ZERO);
                cession.setRetainedExposure(grossExposure);
                cession.setCededPremium(BigDecimal.ZERO);
            }
        }

        if (grossExposure.compareTo(agreement.getGrossRetentionLimit()) > 0) {
            cession.setRequiresFacultative(true);
        }

        return cession;
    }
}
