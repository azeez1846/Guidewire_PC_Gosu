package com.guidewire.pc.service;

import com.guidewire.pc.model.Cost;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RatingEngine {
    private static final Logger LOGGER = Logger.getLogger(RatingEngine.class.getName());
    private static final RatingEngine instance = new RatingEngine();

    private RatingEngine() {}

    public static RatingEngine getInstance() {
        return instance;
    }

    public List<Cost> rate(PolicyPeriod period) {
        LOGGER.info("Executing Guidewire Rating Engine for PolicyPeriod: " + period.getJobNumber());
        List<Cost> costs = new ArrayList<>();

        // 1. Base Premium Cost
        double baseRate = 500.0;
        if ("PersonalAuto".equalsIgnoreCase(period.getProductCode())) baseRate = 650.0;
        else if ("CommercialAuto".equalsIgnoreCase(period.getProductCode())) baseRate = 1250.0;
        else if ("CommercialProperty".equalsIgnoreCase(period.getProductCode())) baseRate = 2100.0;
        else if ("GeneralLiability".equalsIgnoreCase(period.getProductCode())) baseRate = 1800.0;

        if (period.getTermMonths() == 12) baseRate *= 1.9;

        BigDecimal baseCostAmt = BigDecimal.valueOf(baseRate).setScale(2, RoundingMode.HALF_UP);
        Cost baseCost = new Cost("BasePremium", "Base Policy Premium for " + period.getProductCode(), baseCostAmt);
        costs.add(baseCost);

        // 2. Bodily Injury Coverage Cost
        double biAmount = 0.0;
        if ("$500k/$500k".equals(period.getBodilyInjuryLimit())) biAmount = 250.0;
        else if ("$1M/$1M".equals(period.getBodilyInjuryLimit())) biAmount = 500.0;
        if (biAmount > 0) {
            BigDecimal biCostAmt = BigDecimal.valueOf(biAmount).setScale(2, RoundingMode.HALF_UP);
            Cost biCost = new Cost("BodilyInjuryCoverage", "Bodily Injury Limit (" + period.getBodilyInjuryLimit() + ")", biCostAmt);
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
        if ("PolicyChange".equalsIgnoreCase(period.getJobType())) {
            for (Cost c : costs) {
                c.setActualAmount(c.getActualAmount().multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP));
            }
        }

        // Calculate Subtotal Base Premium
        BigDecimal netBasePremium = BigDecimal.ZERO;
        for (Cost c : costs) {
            netBasePremium = netBasePremium.add(c.getActualAmount());
        }

        // 4. Taxes & Fees Costs
        BigDecimal taxAmt = netBasePremium.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        Cost taxCost = new Cost("StateTax", "State Statutory Tax (8%)", taxAmt);
        costs.add(taxCost);

        BigDecimal feeAmt = new BigDecimal("25.00");
        Cost feeCost = new Cost("PolicyFee", "Standard Policy Issuance Fee", feeAmt);
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
        List<Transaction> txns = new ArrayList<>();
        for (Cost c : costs) {
            Transaction tx = new Transaction(c, period.getJobNumber(), c.getActualAmount(), "PremiumCharge");
            txns.add(tx);
        }
        return txns;
    }
}
