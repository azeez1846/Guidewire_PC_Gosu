package com.guidewire.pc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PriorLossService {
    private static final Logger LOGGER = Logger.getLogger(PriorLossService.class.getName());
    private static final PriorLossService INSTANCE = new PriorLossService();

    public static PriorLossService getInstance() {
        return INSTANCE;
    }

    public static class PriorLossClaimRecord {
        public String claimNumber;
        public String lossDate;
        public String lineOfBusiness;
        public String peril;
        public String status; // CLOSED, OPEN
        public BigDecimal paidLoss;
        public BigDecimal reservedLoss;
        public BigDecimal totalIncurred;
        public String description;

        public PriorLossClaimRecord(String claimNumber, String lossDate, String lineOfBusiness, String peril, String status, BigDecimal paidLoss, BigDecimal reservedLoss, String description) {
            this.claimNumber = claimNumber;
            this.lossDate = lossDate;
            this.lineOfBusiness = lineOfBusiness;
            this.peril = peril;
            this.status = status;
            this.paidLoss = paidLoss;
            this.reservedLoss = reservedLoss;
            this.totalIncurred = paidLoss.add(reservedLoss);
            this.description = description;
        }
    }

    public static class PriorLossReport {
        public String searchKey; // FEIN, TaxID, Account/Insured Name
        public String provider; // LexisNexis C.L.U.E. Commercial / ISO ClaimSearch
        public int lookbackYears;
        public List<PriorLossClaimRecord> claims = new ArrayList<>();
        public int totalClaimsCount;
        public BigDecimal totalPaidAmount = BigDecimal.ZERO;
        public BigDecimal totalIncurredAmount = BigDecimal.ZERO;
        public BigDecimal estimatedThreeYearEarnedPremium = BigDecimal.ZERO;
        public double lossRatioPct;
        public BigDecimal lossModifierFactor; // e.g. 0.85 (15% credit) to 1.30 (30% debit)
        public String lossModifierDescription;
        public boolean requiresUnderwriterReferral;
        public String underwriterReferralReason;
    }

    public PriorLossReport retrievePriorLossHistory(String searchKey, BigDecimal annualEarnedPremium) {
        LOGGER.log(Level.FINE, "→ PriorLossService.retrievePriorLossHistory for: " + searchKey);
        PriorLossReport report = new PriorLossReport();
        report.searchKey = searchKey != null ? searchKey : "TAX-94-1829104";
        report.provider = "LexisNexis C.L.U.E. Commercial & ISO ClaimSearch v2.8";
        report.lookbackYears = 3;

        BigDecimal earnedPrem = annualEarnedPremium != null && annualEarnedPremium.compareTo(BigDecimal.ZERO) > 0
                ? annualEarnedPremium.multiply(new BigDecimal("3.0")) // 3 years
                : new BigDecimal("45000.00");
        report.estimatedThreeYearEarnedPremium = earnedPrem;

        String keyLower = report.searchKey.toLowerCase();
        if (keyLower.contains("clean") || keyLower.contains("preferred") || keyLower.contains("a0001001")) {
            // Clean loss history - 0 claims
            report.totalClaimsCount = 0;
            report.totalPaidAmount = BigDecimal.ZERO;
            report.totalIncurredAmount = BigDecimal.ZERO;
            report.lossRatioPct = 0.0;
            report.lossModifierFactor = new BigDecimal("0.85");
            report.lossModifierDescription = "Preferred 3-Year Claims-Free Discount (-15%)";
            report.requiresUnderwriterReferral = false;
        } else if (keyLower.contains("high") || keyLower.contains("severe") || keyLower.contains("adverse")) {
            // High loss history - 3 claims, high severity
            report.claims.add(new PriorLossClaimRecord("CLM-2024-8191", "2024-03-15", "CommercialProperty", "Water Pipe Burst", "CLOSED", new BigDecimal("32000.00"), BigDecimal.ZERO, "Frozen pipe burst flooding 2nd floor tenant suites"));
            report.claims.add(new PriorLossClaimRecord("CLM-2025-1044", "2025-08-22", "GeneralLiability", "Slip and Fall", "CLOSED", new BigDecimal("18500.00"), BigDecimal.ZERO, "Customer slipped on wet entryway tile during rainstorm"));
            report.claims.add(new PriorLossClaimRecord("CLM-2026-0092", "2026-01-10", "CommercialProperty", "Electrical Fire", "OPEN", new BigDecimal("12000.00"), new BigDecimal("25000.00"), "Breakroom electrical panel fire causing smoke damage"));

            for (PriorLossClaimRecord c : report.claims) {
                report.totalPaidAmount = report.totalPaidAmount.add(c.paidLoss);
                report.totalIncurredAmount = report.totalIncurredAmount.add(c.totalIncurred);
            }
            report.totalClaimsCount = report.claims.size();
            report.lossRatioPct = report.totalIncurredAmount.divide(report.estimatedThreeYearEarnedPremium, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            report.lossModifierFactor = new BigDecimal("1.30");
            report.lossModifierDescription = "Adverse Loss History Debit Surcharge (+30%)";
            report.requiresUnderwriterReferral = true;
            report.underwriterReferralReason = "High loss frequency (3 claims) and loss ratio exceeds 70% (Incurred: $" + report.totalIncurredAmount + ")";
        } else {
            // Standard moderate history - 1 small claim
            report.claims.add(new PriorLossClaimRecord("CLM-2024-4109", "2024-11-04", "CommercialProperty", "Wind / Hail", "CLOSED", new BigDecimal("4500.00"), BigDecimal.ZERO, "Hail damage to exterior HVAC condenser units"));
            report.totalPaidAmount = new BigDecimal("4500.00");
            report.totalIncurredAmount = new BigDecimal("4500.00");
            report.totalClaimsCount = 1;
            report.lossRatioPct = report.totalIncurredAmount.divide(report.estimatedThreeYearEarnedPremium, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            report.lossModifierFactor = new BigDecimal("1.00");
            report.lossModifierDescription = "Standard Loss Rating Basis (0% modifier)";
            report.requiresUnderwriterReferral = false;
        }

        return report;
    }

    public Map<String, Object> toMap(PriorLossReport report) {
        Map<String, Object> map = new HashMap<>();
        map.put("searchKey", report.searchKey);
        map.put("provider", report.provider);
        map.put("lookbackYears", report.lookbackYears);
        map.put("totalClaimsCount", report.totalClaimsCount);
        map.put("totalPaidAmount", report.totalPaidAmount);
        map.put("totalIncurredAmount", report.totalIncurredAmount);
        map.put("estimatedThreeYearEarnedPremium", report.estimatedThreeYearEarnedPremium);
        map.put("lossRatioPct", Math.round(report.lossRatioPct * 100.0) / 100.0);
        map.put("lossModifierFactor", report.lossModifierFactor);
        map.put("lossModifierDescription", report.lossModifierDescription);
        map.put("requiresUnderwriterReferral", report.requiresUnderwriterReferral);
        map.put("underwriterReferralReason", report.underwriterReferralReason);
        map.put("claims", report.claims);
        map.put("status", "SUCCESS");
        return map;
    }
}
