package com.guidewire.pc.service;

import com.guidewire.pc.model.AuditInformation;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AuditJobService {
    private static final Logger LOGGER = Logger.getLogger(AuditJobService.class.getName());


    public static AuditInformation startAudit(PolicyPeriod period, String auditType, String auditMethod, BigDecimal estimatedExposure) {
        LOGGER.log(Level.FINE, "→ AuditJobService.startAudit");
        if (period == null) return null;

        AuditInformation auditInfo = new AuditInformation(
            auditType != null ? auditType : "FinalAudit",
            auditMethod != null ? auditMethod : "Voluntary",
            estimatedExposure != null ? estimatedExposure : period.getBasePremium()
        );

        auditInfo.setAuditStatus("Draft");
        auditInfo.setAuditDueDate(LocalDate.now().plusDays(30).toString());

        return auditInfo;
    }

    public static AuditInformation enterAuditedExposure(AuditInformation auditInfo, BigDecimal auditedExposure) {
        LOGGER.log(Level.FINE, "→ AuditJobService.enterAuditedExposure");
        if (auditInfo == null) return null;

        auditInfo.setAuditedExposure(auditedExposure != null ? auditedExposure : BigDecimal.ZERO);
        auditInfo.setAuditStatus("InProcess");
        return auditInfo;
    }

    public static BigDecimal calculateAuditAdjustment(AuditInformation auditInfo, PolicyPeriod period) {
        LOGGER.log(Level.FINE, "→ AuditJobService.calculateAuditAdjustment");
        if (auditInfo == null || period == null) return BigDecimal.ZERO;

        BigDecimal estimated = auditInfo.getEstimatedExposure();
        BigDecimal audited = auditInfo.getAuditedExposure();
        BigDecimal basePrem = period.getBasePremium() != null ? period.getBasePremium() : BigDecimal.ZERO;

        if (estimated == null || estimated.compareTo(BigDecimal.ZERO) == 0) {
            auditInfo.setAuditPremiumAdjustment(BigDecimal.ZERO);
            return BigDecimal.ZERO;
        }

        BigDecimal ratio = audited.divide(estimated, 6, RoundingMode.HALF_UP);
        BigDecimal newAuditedPremium = basePrem.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal adjustment = newAuditedPremium.subtract(basePrem).setScale(2, RoundingMode.HALF_UP);

        auditInfo.setAuditPremiumAdjustment(adjustment);
        return adjustment;
    }

    public static AuditInformation closeAudit(AuditInformation auditInfo, PolicyPeriod period) {
        LOGGER.log(Level.FINE, "→ AuditJobService.closeAudit");
        if (auditInfo == null) return null;

        calculateAuditAdjustment(auditInfo, period);
        auditInfo.setAuditStatus("Closed");
        auditInfo.setAuditCompleteDate(LocalDate.now().toString());

        return auditInfo;
    }

    public static Transaction createAuditTransaction(AuditInformation auditInfo, PolicyPeriod period) {
        LOGGER.log(Level.FINE, "→ AuditJobService.createAuditTransaction");
        if (auditInfo == null || period == null) return null;

        BigDecimal adjustment = calculateAuditAdjustment(auditInfo, period);
        return new Transaction(null, period.getPolicyNumber(), adjustment, "AuditPremiumAdjustment");
    }
}
