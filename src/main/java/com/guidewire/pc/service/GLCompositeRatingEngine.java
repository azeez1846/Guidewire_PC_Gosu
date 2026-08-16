package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GLCompositeRatingEngine {
    private static final Logger LOGGER = Logger.getLogger(GLCompositeRatingEngine.class.getName());
    private static final GLCompositeRatingEngine INSTANCE = new GLCompositeRatingEngine();

    public static GLCompositeRatingEngine getInstance() {
        return INSTANCE;
    }

    public static class GLCompositeResult {
        public String policyNumber;
        public BigDecimal grossSalesAmount;
        public BigDecimal salesPremium;
        public BigDecimal squareFootage;
        public BigDecimal areaPremium;
        public BigDecimal payrollAmount;
        public BigDecimal payrollPremium;
        public BigDecimal ocpLiabilityLimit;
        public BigDecimal ocpPremium;
        public String liquorHazardTier; // TIER_1_RESTAURANT, TIER_2_TAVERN, TIER_3_NIGHTCLUB, NONE
        public BigDecimal liquorLiabilityPremium;
        public BigDecimal productsCompletedOpsLimit;
        public BigDecimal productsCompletedOpsPremium;
        public BigDecimal subtotalPrem;
        public BigDecimal stateTaxesAndFees;
        public BigDecimal totalCompositePremium;
    }

    public GLCompositeResult rateCompositeGL(PolicyPeriod period, BigDecimal grossSales, BigDecimal sqFt, BigDecimal payroll,
                                             BigDecimal ocpLimit, String liquorTier, BigDecimal liquorSales,
                                             BigDecimal productsCompletedLimit) {
        LOGGER.log(Level.FINE, "→ GLCompositeRatingEngine.rateCompositeGL");
        GLCompositeResult res = new GLCompositeResult();
        res.policyNumber = period != null && period.getPolicyNumber() != null ? period.getPolicyNumber() : "POL-GL-COMP-7001";

        res.grossSalesAmount = grossSales != null ? grossSales : new BigDecimal("2500000.00");
        res.squareFootage = sqFt != null ? sqFt : new BigDecimal("45000.00");
        res.payrollAmount = payroll != null ? payroll : new BigDecimal("600000.00");
        res.ocpLiabilityLimit = ocpLimit != null ? ocpLimit : BigDecimal.ZERO;
        res.liquorHazardTier = liquorTier != null ? liquorTier : "NONE";
        res.productsCompletedOpsLimit = productsCompletedLimit != null ? productsCompletedLimit : new BigDecimal("2000000.00");

        // 1. Gross Sales Rating ($4.50 per $1,000 sales)
        BigDecimal salesUnits = res.grossSalesAmount.divide(new BigDecimal("1000.00"), 4, RoundingMode.HALF_UP);
        res.salesPremium = salesUnits.multiply(new BigDecimal("4.50")).setScale(2, RoundingMode.HALF_UP);

        // 2. Area Square Footage Rating ($85.00 per 1,000 sq ft)
        BigDecimal areaUnits = res.squareFootage.divide(new BigDecimal("1000.00"), 4, RoundingMode.HALF_UP);
        res.areaPremium = areaUnits.multiply(new BigDecimal("85.00")).setScale(2, RoundingMode.HALF_UP);

        // 3. Payroll Exposure Rating ($1.80 per $100 payroll)
        BigDecimal payrollUnits = res.payrollAmount.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        res.payrollPremium = payrollUnits.multiply(new BigDecimal("1.80")).setScale(2, RoundingMode.HALF_UP);

        // 4. OCP Endorsement (0.12% of OCP limit)
        if (res.ocpLiabilityLimit.compareTo(BigDecimal.ZERO) > 0) {
            res.ocpPremium = res.ocpLiabilityLimit.multiply(new BigDecimal("0.0012")).setScale(2, RoundingMode.HALF_UP);
        } else {
            res.ocpPremium = BigDecimal.ZERO;
        }

        // 5. Liquor Liability Tier Rating
        if (!"NONE".equalsIgnoreCase(res.liquorHazardTier) && liquorSales != null && liquorSales.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal liqUnits = liquorSales.divide(new BigDecimal("1000.00"), 4, RoundingMode.HALF_UP);
            BigDecimal baseLiqRate = new BigDecimal("8.50"); // per $1k liquor sales

            BigDecimal tierMultiplier = switch (res.liquorHazardTier.toUpperCase()) {
                case "TIER_2_TAVERN" -> new BigDecimal("1.60");
                case "TIER_3_NIGHTCLUB" -> new BigDecimal("2.50");
                default -> new BigDecimal("1.00"); // TIER_1_RESTAURANT
            };
            res.liquorLiabilityPremium = liqUnits.multiply(baseLiqRate).multiply(tierMultiplier).setScale(2, RoundingMode.HALF_UP);
        } else {
            res.liquorLiabilityPremium = BigDecimal.ZERO;
        }

        // 6. Products-Completed Operations ($1.20 per $1,000 limit)
        BigDecimal pcoUnits = res.productsCompletedOpsLimit.divide(new BigDecimal("1000.00"), 4, RoundingMode.HALF_UP);
        res.productsCompletedOpsPremium = pcoUnits.multiply(new BigDecimal("1.20")).setScale(2, RoundingMode.HALF_UP);

        res.subtotalPrem = res.salesPremium.add(res.areaPremium).add(res.payrollPremium)
                .add(res.ocpPremium).add(res.liquorLiabilityPremium).add(res.productsCompletedOpsPremium);

        // Taxes & state assessments (6%)
        res.stateTaxesAndFees = res.subtotalPrem.multiply(new BigDecimal("0.06")).setScale(2, RoundingMode.HALF_UP);
        res.totalCompositePremium = res.subtotalPrem.add(res.stateTaxesAndFees);

        return res;
    }

    public Map<String, Object> toMap(GLCompositeResult r) {
        Map<String, Object> map = new HashMap<>();
        map.put("policyNumber", r.policyNumber);
        map.put("grossSalesAmount", r.grossSalesAmount);
        map.put("salesPremium", r.salesPremium);
        map.put("squareFootage", r.squareFootage);
        map.put("areaPremium", r.areaPremium);
        map.put("payrollAmount", r.payrollAmount);
        map.put("payrollPremium", r.payrollPremium);
        map.put("ocpLiabilityLimit", r.ocpLiabilityLimit);
        map.put("ocpPremium", r.ocpPremium);
        map.put("liquorHazardTier", r.liquorHazardTier);
        map.put("liquorLiabilityPremium", r.liquorLiabilityPremium);
        map.put("productsCompletedOpsLimit", r.productsCompletedOpsLimit);
        map.put("productsCompletedOpsPremium", r.productsCompletedOpsPremium);
        map.put("subtotalPrem", r.subtotalPrem);
        map.put("stateTaxesAndFees", r.stateTaxesAndFees);
        map.put("totalCompositePremium", r.totalCompositePremium);
        map.put("status", "SUCCESS");
        return map;
    }
}
