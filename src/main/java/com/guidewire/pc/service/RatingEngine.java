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
    private static final RatingEngine instance = new RatingEngine();

    private RatingEngine() {
        LOGGER.log(Level.FINE, "→ RatingEngine.RatingEngine");}

    public static RatingEngine getInstance() {
        LOGGER.log(Level.FINE, "→ RatingEngine.getInstance");
        return instance;
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

        // 1. Base Premium Cost
        double baseRate = 500.0;
        if (PCConstants.PRODUCT_PERSONAL_AUTO.equalsIgnoreCase(period.getProductCode())) baseRate = 650.0;
        else if (PCConstants.PRODUCT_COMMERCIAL_AUTO.equalsIgnoreCase(period.getProductCode())) baseRate = 1250.0;
        else if (PCConstants.PRODUCT_COMMERCIAL_PROPERTY.equalsIgnoreCase(period.getProductCode())) baseRate = 2100.0;
        else if (PCConstants.PRODUCT_GENERAL_LIABILITY.equalsIgnoreCase(period.getProductCode())) baseRate = 1800.0;

        if (period.getTermMonths() == 12) baseRate *= 1.9;

        BigDecimal baseCostAmt = BigDecimal.valueOf(baseRate).setScale(2, RoundingMode.HALF_UP);
        Cost baseCost = new Cost(PCConstants.CHARGE_BASE_PREMIUM, "Base Policy Premium for " + period.getProductCode(), baseCostAmt);
        costs.add(baseCost);

        // 2. Bodily Injury Coverage Cost
        double biAmount = 0.0;
        if ("$500k/$500k".equals(period.getBodilyInjuryLimit())) biAmount = 250.0;
        else if ("$1M/$1M".equals(period.getBodilyInjuryLimit())) biAmount = 500.0;
        if (biAmount > 0) {
            BigDecimal biCostAmt = BigDecimal.valueOf(biAmount).setScale(2, RoundingMode.HALF_UP);
            Cost biCost = new Cost(PCConstants.CHARGE_BODILY_INJURY, "Bodily Injury Limit (" + period.getBodilyInjuryLimit() + ")", biCostAmt);
            costs.add(biCost);
        }

        // 3. Property Damage Coverage Cost
        double pdAmount = 0.0;
        if ("$250k".equals(period.getPropertyDamageLimit())) pdAmount = 150.0;
        else if ("$500k".equals(period.getPropertyDamageLimit())) pdAmount = 300.0;
        if (pdAmount > 0) {
            BigDecimal pdCostAmt = BigDecimal.valueOf(pdAmount).setScale(2, RoundingMode.HALF_UP);
            Cost pdCost = new Cost("PropertyDamageCoverage", "Property Damage Limit (" + period.getPropertyDamageLimit() + ")", pdCostAmt);
            costs.add(pdCost);
        }

        // Mid-term proration for PolicyChange / Cancellation
        if (PCConstants.JOB_TYPE_POLICY_CHANGE.equalsIgnoreCase(period.getJobType())) {
            for (Cost c : costs) {
                c.setActualAmount(c.getActualAmount().multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP));
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
