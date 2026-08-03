package com.guidewire.pc.service;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.RateTable;
import com.guidewire.pc.model.RateTableEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RateRoutineEngine {
    private static final Logger LOGGER = Logger.getLogger(RateRoutineEngine.class.getName());
    private static final RateRoutineEngine instance = new RateRoutineEngine();

    private final Map<String, RateTable> rateTables = new HashMap<>();

    private RateRoutineEngine() {
        seedRateTables();
    }

    public static RateRoutineEngine getInstance() {
        return instance;
    }

    private void seedRateTables() {
        RateTable autoTable = new RateTable("Commercial Auto Rate Matrix", "RT_COMM_AUTO", PCConstants.PRODUCT_COMMERCIAL_AUTO);
        autoTable.addEntry(new RateTableEntry(PCConstants.PRODUCT_COMMERCIAL_AUTO, "CA", "Territory_01", "Standard", new BigDecimal("1200.00"), 1.15, 1.00));
        autoTable.addEntry(new RateTableEntry(PCConstants.PRODUCT_COMMERCIAL_AUTO, "CA", "Territory_02", "HighRisk", new BigDecimal("1500.00"), 1.25, 1.20));
        autoTable.addEntry(new RateTableEntry(PCConstants.PRODUCT_COMMERCIAL_AUTO, "NY", "Territory_01", "Standard", new BigDecimal("1400.00"), 1.30, 1.05));
        autoTable.addEntry(new RateTableEntry(PCConstants.PRODUCT_COMMERCIAL_AUTO, "FL", "Territory_01", "HighRisk", new BigDecimal("1600.00"), 1.40, 1.25));
        rateTables.put(PCConstants.PRODUCT_COMMERCIAL_AUTO, autoTable);

        RateTable propTable = new RateTable("Commercial Property Rate Matrix", "RT_COMM_PROP", PCConstants.PRODUCT_COMMERCIAL_PROPERTY);
        propTable.addEntry(new RateTableEntry(PCConstants.PRODUCT_COMMERCIAL_PROPERTY, "CA", "Territory_01", "Standard", new BigDecimal("2000.00"), 1.10, 1.00));
        propTable.addEntry(new RateTableEntry(PCConstants.PRODUCT_COMMERCIAL_PROPERTY, "FL", "Territory_01", "HighRisk", new BigDecimal("2800.00"), 1.50, 1.30));
        rateTables.put(PCConstants.PRODUCT_COMMERCIAL_PROPERTY, propTable);
    }

    public RateRoutineResult executeRateRoutine(PolicyPeriod period) {
        RateRoutineResult result = new RateRoutineResult();
        if (period == null) return result;

        String productCode = period.getProductCode() != null ? period.getProductCode() : PCConstants.PRODUCT_COMMERCIAL_AUTO;
        String state = period.getBaseState() != null ? period.getBaseState() : "CA";
        String territory = "Territory_01";
        String riskTier = "Standard";

        result.addStep("Step 1: Locate Rate Table for Product " + productCode);
        RateTable table = rateTables.get(productCode);
        RateTableEntry entry = null;

        if (table != null) {
            for (RateTableEntry e : table.getEntries()) {
                if (state.equalsIgnoreCase(e.getState())) {
                    entry = e;
                    break;
                }
            }
        }

        if (entry == null) {
            result.addStep("Step 1a: Matrix miss. Fallback to default base rate $1,000.00");
            entry = new RateTableEntry(productCode, state, territory, riskTier, new BigDecimal("1000.00"), 1.10, 1.00);
        }

        BigDecimal baseRate = entry.getBaseRate();
        result.addStep("Step 2: Base Rate Matrix Lookup -> $" + baseRate);

        BigDecimal territoryApplied = baseRate.multiply(BigDecimal.valueOf(entry.getTerritoryFactor())).setScale(2, RoundingMode.HALF_UP);
        result.addStep("Step 3: Apply Territory Factor (" + entry.getTerritoryFactor() + "x) -> $" + territoryApplied);

        BigDecimal riskTierApplied = territoryApplied.multiply(BigDecimal.valueOf(entry.getRiskTierFactor())).setScale(2, RoundingMode.HALF_UP);
        result.addStep("Step 4: Apply Risk Tier Multiplier (" + entry.getRiskTierFactor() + "x) -> $" + riskTierApplied);

        // Step 5: Deductible Modifier
        double dedMod = 1.00;
        if ("$1000".equals(period.getCollisionDeductible()) || "$1000".equals(period.getComprehensiveDeductible())) {
            dedMod = 0.92; // 8% discount for $1000 deductible
        }
        BigDecimal dedApplied = riskTierApplied.multiply(BigDecimal.valueOf(dedMod)).setScale(2, RoundingMode.HALF_UP);
        result.addStep("Step 5: Apply Deductible Discount (" + dedMod + "x) -> $" + dedApplied);

        // Step 6: Statutory Min/Max Cap Check
        BigDecimal finalPrem = dedApplied;
        if (finalPrem.compareTo(new BigDecimal("250.00")) < 0) {
            finalPrem = new BigDecimal("250.00");
            result.addStep("Step 6: Enforced Statutory Minimum Premium Cap -> $250.00");
        } else {
            result.addStep("Step 6: Final Rate Routine Output -> $" + finalPrem);
        }

        result.setFinalPremium(finalPrem);
        LOGGER.log(Level.INFO, "Rate Routine executed for policy {0}: ${1}", new Object[]{period.getPolicyNumber(), finalPrem});

        return result;
    }

    public static class RateRoutineResult {
        private BigDecimal finalPremium = BigDecimal.ZERO;
        private final List<String> executionSteps = new ArrayList<>();

        public void addStep(String step) { this.executionSteps.add(step); }
        public BigDecimal getFinalPremium() { return finalPremium; }
        public void setFinalPremium(BigDecimal finalPremium) { this.finalPremium = finalPremium; }
        public List<String> getExecutionSteps() { return executionSteps; }
    }
}
