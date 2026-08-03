package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CyberLiabilityEngine {
    private static final Logger LOGGER = Logger.getLogger(CyberLiabilityEngine.class.getName());
    private static final CyberLiabilityEngine instance = new CyberLiabilityEngine();

    private CyberLiabilityEngine() {}

    public static CyberLiabilityEngine getInstance() {
        return instance;
    }

    public CyberResult evaluateCyberSecurityControls(PolicyPeriod period, boolean mfaEnabled, boolean offlineBackupsDaily, boolean edrDeployed, boolean employeePhishingTrained) {
        CyberResult result = new CyberResult();
        if (period == null) return result;

        BigDecimal basePrem = period.getTotalPremium() != null ? period.getTotalPremium() : new BigDecimal("8000.00");
        BigDecimal fullPolicyLimit = new BigDecimal("1000000.00");

        result.setPolicyNumber(period.getPolicyNumber());
        result.setBaseCyberPremium(basePrem);
        result.setMfaEnabled(mfaEnabled);
        result.setOfflineBackupsDaily(offlineBackupsDaily);
        result.setEdrDeployed(edrDeployed);
        result.setEmployeePhishingTrained(employeePhishingTrained);

        int activeControls = (mfaEnabled ? 1 : 0) + (offlineBackupsDaily ? 1 : 0) + (edrDeployed ? 1 : 0) + (employeePhishingTrained ? 1 : 0);
        result.setActiveControlCount(activeControls);

        double rateModifier;
        BigDecimal ransomwareSubLimit;
        boolean subLimitCapped;

        if (!mfaEnabled) {
            // Missing MFA -> High vulnerability! Ransomware capped at $250k, 30% surcharge
            rateModifier = 1.30;
            ransomwareSubLimit = new BigDecimal("250000.00");
            subLimitCapped = true;
            result.setSecurityTier("HIGH_VULNERABILITY_NO_MFA (+30% Surcharge, $250k Ransomware Cap)");
        } else if (activeControls == 4) {
            // Excellent Security Stack -> 15% discount, full ransomware limit
            rateModifier = 0.85;
            ransomwareSubLimit = fullPolicyLimit;
            subLimitCapped = false;
            result.setSecurityTier("EXCELLENT_CYBER_POSTURE (-15% Discount)");
        } else {
            // Standard Security Stack
            rateModifier = 1.00;
            ransomwareSubLimit = new BigDecimal("500000.00");
            subLimitCapped = true;
            result.setSecurityTier("STANDARD_CYBER_POSTURE (0% Adjustment, $500k Ransomware Cap)");
        }

        BigDecimal adjustedPrem = basePrem.multiply(BigDecimal.valueOf(rateModifier)).setScale(2, RoundingMode.HALF_UP);
        result.setRateModifier(rateModifier);
        result.setRansomwareSubLimit(ransomwareSubLimit);
        result.setSubLimitCapped(subLimitCapped);
        result.setAdjustedCyberPremium(adjustedPrem);

        LOGGER.log(Level.INFO, "Cyber Security Controls evaluated for policy {0}: MFA={1}, ActiveControls={2}, Tier={3}, AdjustedPrem=${4}",
                new Object[]{period.getPolicyNumber(), mfaEnabled, activeControls, result.getSecurityTier(), adjustedPrem});

        return result;
    }

    public static class CyberResult {
        private String policyNumber;
        private BigDecimal baseCyberPremium = BigDecimal.ZERO;
        private boolean mfaEnabled;
        private boolean offlineBackupsDaily;
        private boolean edrDeployed;
        private boolean employeePhishingTrained;
        private int activeControlCount;
        private double rateModifier;
        private BigDecimal ransomwareSubLimit = BigDecimal.ZERO;
        private boolean subLimitCapped;
        private String securityTier;
        private BigDecimal adjustedCyberPremium = BigDecimal.ZERO;

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public BigDecimal getBaseCyberPremium() { return baseCyberPremium; }
        public void setBaseCyberPremium(BigDecimal baseCyberPremium) { this.baseCyberPremium = baseCyberPremium; }

        public boolean isMfaEnabled() { return mfaEnabled; }
        public void setMfaEnabled(boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }

        public boolean isOfflineBackupsDaily() { return offlineBackupsDaily; }
        public void setOfflineBackupsDaily(boolean offlineBackupsDaily) { this.offlineBackupsDaily = offlineBackupsDaily; }

        public boolean isEdrDeployed() { return edrDeployed; }
        public void setEdrDeployed(boolean edrDeployed) { this.edrDeployed = edrDeployed; }

        public boolean isEmployeePhishingTrained() { return employeePhishingTrained; }
        public void setEmployeePhishingTrained(boolean employeePhishingTrained) { this.employeePhishingTrained = employeePhishingTrained; }

        public int getActiveControlCount() { return activeControlCount; }
        public void setActiveControlCount(int activeControlCount) { this.activeControlCount = activeControlCount; }

        public double getRateModifier() { return rateModifier; }
        public void setRateModifier(double rateModifier) { this.rateModifier = rateModifier; }

        public BigDecimal getRansomwareSubLimit() { return ransomwareSubLimit; }
        public void setRansomwareSubLimit(BigDecimal ransomwareSubLimit) { this.ransomwareSubLimit = ransomwareSubLimit; }

        public boolean isSubLimitCapped() { return subLimitCapped; }
        public void setSubLimitCapped(boolean subLimitCapped) { this.subLimitCapped = subLimitCapped; }

        public String getSecurityTier() { return securityTier; }
        public void setSecurityTier(String securityTier) { this.securityTier = securityTier; }

        public BigDecimal getAdjustedCyberPremium() { return adjustedCyberPremium; }
        public void setAdjustedCyberPremium(BigDecimal adjustedCyberPremium) { this.adjustedCyberPremium = adjustedCyberPremium; }
    }
}
