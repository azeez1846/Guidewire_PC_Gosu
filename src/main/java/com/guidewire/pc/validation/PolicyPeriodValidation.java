package com.guidewire.pc.validation;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;

public class PolicyPeriodValidation {

    public static PCValidationContext validate(PolicyPeriod period, String level) {
        PCValidationContext context = new PCValidationContext(level);
        if (period == null) {
            context.addError(null, level, "PolicyPeriod is null");
            return context;
        }

        // Draft Level Checks
        if (period.getAccount() == null) {
            context.addError(period, "Draft", "Account is required on policy period.");
        }
        if (period.getEffectiveDate() == null || period.getEffectiveDate().isEmpty()) {
            context.addError(period, "Draft", "Effective Date is required.");
        }

        if ("Draft".equalsIgnoreCase(level)) {
            return context;
        }

        // Quotation Level Checks
        if (period.getProducerCode() == null || period.getProducerCode().trim().isEmpty()) {
            context.addError(period, "Quotation", "Producer Code is required for quoting.");
        }
        if (period.getBaseState() == null || period.getBaseState().trim().isEmpty()) {
            context.addError(period, "Quotation", "Base State is required for quoting.");
        }
        if (period.getCoverages().isEmpty()) {
            context.addError(period, "Quotation", "At least one Coverage pattern must be attached prior to quoting.");
        }

        if ("Quotation".equalsIgnoreCase(level)) {
            return context;
        }

        // Bind Level Checks
        if (period.getAccount() != null) {
            String fein = period.getAccount().getFein();
            if (fein == null || fein.trim().isEmpty()) {
                context.addError(period.getAccount(), "Bind", "Account FEIN / Tax ID is required prior to binding.");
            }
        }
        if (period.getTotalPremium() == null || period.getTotalPremium().compareTo(BigDecimal.ZERO) <= 0) {
            context.addWarning(period, "Bind", "Total premium is zero or uncalculated prior to binding.");
        }

        return context;
    }
}
