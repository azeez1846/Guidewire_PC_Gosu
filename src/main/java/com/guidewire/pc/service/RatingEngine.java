package com.guidewire.pc.service;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.Cost;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RatingEngine {
    private static final Logger LOGGER = Logger.getLogger(RatingEngine.class.getName());
    private static final RatingEngine INSTANCE = new RatingEngine();

    private RatingEngine() {
        LOGGER.log(Level.FINE, "RatingEngine initialized");
    }

    public static RatingEngine getInstance() {
        return INSTANCE;
    }

    public List<Cost> rate(PolicyPeriod period) {
        LOGGER.log(Level.FINE, "→ RatingEngine.rate");
        LOGGER.log(Level.INFO, "Executing Guidewire Rating Engine for PolicyPeriod: {0}", period != null ? period.getJobNumber() : "null");
        List<Cost> costs = new ArrayList<>();
        if (period == null) return costs;

        if (PCConstants.PRODUCT_INLAND_MARINE.equalsIgnoreCase(period.getProductCode())) {
            IMRatingService.getInstance().rateInlandMarine(period);
            Cost baseCost = new Cost(PCConstants.CHARGE_BASE_PREMIUM, "Inland Marine Base Premium", period.getBasePremium());
            Cost taxCost = new Cost(PCConstants.CHARGE_STATE_TAX, "State Tax & Fees", period.getTaxesAndFees());
            costs.add(baseCost);
            costs.add(taxCost);
            for (Cost c : costs) {
                period.addEffDatedBean(c);
            }
            return costs;
        }

        // 1. Base Premium Cost (Pure BigDecimal arithmetic)
        BigDecimal baseRate = new BigDecimal("500.00");
        if (PCConstants.PRODUCT_PERSONAL_AUTO.equalsIgnoreCase(period.getProductCode())) {
            baseRate = new BigDecimal("650.00");
        } else if (PCConstants.PRODUCT_COMMERCIAL_AUTO.equalsIgnoreCase(period.getProductCode())) {
            baseRate = new BigDecimal("1250.00");
        } else if (PCConstants.PRODUCT_COMMERCIAL_PROPERTY.equalsIgnoreCase(period.getProductCode())) {
            baseRate = new BigDecimal("2100.00");
        } else if (PCConstants.PRODUCT_GENERAL_LIABILITY.equalsIgnoreCase(period.getProductCode())) {
            baseRate = new BigDecimal("1800.00");
        }

        if (period.getTermMonths() == 12) {
            baseRate = baseRate.multiply(new BigDecimal("1.90")).setScale(2, RoundingMode.HALF_UP);
        }

        Cost baseCost = new Cost(PCConstants.CHARGE_BASE_PREMIUM, "Base Policy Premium for " + period.getProductCode(), baseRate);
        costs.add(baseCost);

        // 2. Bodily Injury Coverage Cost
        BigDecimal biCostAmt = BigDecimal.ZERO;
        if ("$500k/$500k".equals(period.getBodilyInjuryLimit())) {
            biCostAmt = new BigDecimal("250.00");
        } else if ("$1M/$1M".equals(period.getBodilyInjuryLimit())) {
            biCostAmt = new BigDecimal("500.00");
        }
        if (biCostAmt.compareTo(BigDecimal.ZERO) > 0) {
            Cost biCost = new Cost(PCConstants.CHARGE_BODILY_INJURY, "Bodily Injury Limit (" + period.getBodilyInjuryLimit() + ")", biCostAmt);
            costs.add(biCost);
        }

        // 3. Property Damage Coverage Cost
        BigDecimal pdCostAmt = BigDecimal.ZERO;
        if ("$250k".equals(period.getPropertyDamageLimit())) {
            pdCostAmt = new BigDecimal("150.00");
        } else if ("$500k".equals(period.getPropertyDamageLimit())) {
            pdCostAmt = new BigDecimal("300.00");
        }
        if (pdCostAmt.compareTo(BigDecimal.ZERO) > 0) {
            Cost pdCost = new Cost(PCConstants.CHARGE_PROPERTY_DAMAGE, "Property Damage Limit (" + period.getPropertyDamageLimit() + ")", pdCostAmt);
            costs.add(pdCost);
        }

        // Mid-term proration for PolicyChange / Cancellation
        if (PCConstants.JOB_TYPE_POLICY_CHANGE.equalsIgnoreCase(period.getJobType())) {
            for (Cost c : costs) {
                c.setActualAmount(c.getActualAmount().multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP));
            }
        }

        // Calculate Subtotal Base Premium
        BigDecimal netBasePremium = BigDecimal.ZERO;
        for (Cost c : costs) {
            netBasePremium = netBasePremium.add(c.getActualAmount());
        }

        // Multi-Policy Bundling Discount (15% off base premium if account has multiple policies)
        if (period.getAccount() != null) {
            String accNum = period.getAccount().getAccountNumber();
            long accountPolicyCount = DataStoreService.getInstance().getSubmissions().stream()
                    .filter(p -> p.getAccount() != null && accNum.equalsIgnoreCase(p.getAccount().getAccountNumber()))
                    .count();
            if (accountPolicyCount >= 2) {
                BigDecimal discountAmt = netBasePremium.multiply(BigDecimal.valueOf(-PCConstants.MULTI_POLICY_DISCOUNT_FACTOR)).setScale(2, RoundingMode.HALF_UP);
                Cost discountCost = new Cost(PCConstants.CHARGE_MULTI_POLICY_DISCOUNT, "Multi-Line Bundling Discount (15%)", discountAmt);
                costs.add(discountCost);
                netBasePremium = netBasePremium.add(discountAmt);
            }
        }

        // 4. Taxes & Fees Costs
        BigDecimal taxAmt = netBasePremium.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        Cost taxCost = new Cost(PCConstants.CHARGE_STATE_TAX, "State Statutory Tax (8%)", taxAmt);
        costs.add(taxCost);

        BigDecimal feeAmt = new BigDecimal("25.00");
        Cost feeCost = new Cost(PCConstants.CHARGE_POLICY_FEE, "Standard Policy Issuance Fee", feeAmt);
        costs.add(feeCost);

        // Update PolicyPeriod Financial Summary
        period.setBasePremium(netBasePremium);
        period.setTaxesAndFees(taxAmt.add(feeAmt));
        period.setTotalPremium(netBasePremium.add(taxAmt).add(feeAmt));

        // Add costs to PolicyPeriod EffDated collection
        for (Cost c : costs) {
            period.addEffDatedBean(c);
        }

        return costs;
    }

    public List<Transaction> createTransactions(PolicyPeriod period, List<Cost> costs) {
        LOGGER.log(Level.FINE, "→ RatingEngine.createTransactions");
        List<Transaction> txns = new ArrayList<>();
        for (Cost c : costs) {
            Transaction tx = new Transaction(c, period.getJobNumber(), c.getActualAmount(), "PremiumCharge");
            txns.add(tx);
        }
        return txns;
    }
}
