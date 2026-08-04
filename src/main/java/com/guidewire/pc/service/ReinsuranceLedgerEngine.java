package com.guidewire.pc.service;

import com.guidewire.pc.model.CessionLedgerEntry;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.ReinsuranceTreatyLayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReinsuranceLedgerEngine {
    private static final Logger LOGGER = Logger.getLogger(ReinsuranceLedgerEngine.class.getName());
    private static final ReinsuranceLedgerEngine instance = new ReinsuranceLedgerEngine();

    private final List<ReinsuranceTreatyLayer> activeTreaties = new ArrayList<>();

    private ReinsuranceLedgerEngine() {
        LOGGER.log(Level.FINE, "→ ReinsuranceLedgerEngine.ReinsuranceLedgerEngine");
        seedTreaties();
    }

    public static ReinsuranceLedgerEngine getInstance() {
        LOGGER.log(Level.FINE, "→ ReinsuranceLedgerEngine.getInstance");
        return instance;
    }

    private void seedTreaties() {
        LOGGER.log(Level.FINE, "→ ReinsuranceLedgerEngine.seedTreaties");
        activeTreaties.add(new ReinsuranceTreatyLayer(
                "TR-QS-1001", "Global Quota Share Treaty", "QuotaShare", "Swiss Re",
                BigDecimal.ZERO, new BigDecimal("10000000.00"), 0.30
        ));
        activeTreaties.add(new ReinsuranceTreatyLayer(
                "TR-SUR-2002", "Commercial Surplus Treaty", "SurplusShare", "Munich Re",
                new BigDecimal("1000000.00"), new BigDecimal("15000000.00"), 0.20
        ));
        activeTreaties.add(new ReinsuranceTreatyLayer(
                "TR-XOL-3003", "Commercial Catastrophe XOL", "ExcessOfLoss", "Lloyd's of London",
                new BigDecimal("500000.00"), new BigDecimal("5000000.00"), 0.50
        ));
    }

    public List<ReinsuranceTreatyLayer> getActiveTreaties() {
        LOGGER.log(Level.FINE, "→ ReinsuranceLedgerEngine.getActiveTreaties");
        return activeTreaties;
    }

    /**
     * Generate Reinsurance Cession Ledger Entries for a Bound Policy
     */
    public List<CessionLedgerEntry> generateCessionLedger(PolicyPeriod period) {
        LOGGER.log(Level.FINE, "→ ReinsuranceLedgerEngine.generateCessionLedger");
        List<CessionLedgerEntry> ledger = new ArrayList<>();
        if (period == null || period.getTotalPremium() == null) return ledger;

        BigDecimal grossPrem = period.getTotalPremium();
        BigDecimal totalCeded = BigDecimal.ZERO;

        for (ReinsuranceTreatyLayer treaty : activeTreaties) {
            BigDecimal ceded = grossPrem.multiply(BigDecimal.valueOf(treaty.getCessionPercentage())).setScale(2, RoundingMode.HALF_UP);
            BigDecimal comm = ceded.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP); // 20% ceding commission
            BigDecimal netRetained = grossPrem.subtract(ceded).add(comm);

            CessionLedgerEntry entry = new CessionLedgerEntry(
                    period.getPolicyNumber(),
                    treaty.getTreatyNumber(),
                    treaty.getReinsurerName(),
                    grossPrem,
                    ceded,
                    comm,
                    netRetained
            );
            ledger.add(entry);
            totalCeded = totalCeded.add(ceded);
        }

        LOGGER.log(Level.INFO, "Reinsurance Cession Ledger generated for policy {0}: Gross Premium ${1}, Total Ceded ${2}",
                new Object[]{period.getPolicyNumber(), grossPrem, totalCeded});

        return ledger;
    }

    /**
     * Simulate Loss Attachment Recovery across Reinsurance Layers
     */
    public Map<String, Object> simulateClaimLossRecovery(BigDecimal totalClaimLoss) {
        LOGGER.log(Level.FINE, "→ ReinsuranceLedgerEngine.simulateClaimLossRecovery");
        Map<String, Object> recoveryReport = new HashMap<>();
        if (totalClaimLoss == null) totalClaimLoss = BigDecimal.ZERO;

        recoveryReport.put("totalClaimLoss", totalClaimLoss);
        List<Map<String, Object>> layerRecoveries = new ArrayList<>();
        BigDecimal netInsurerLoss;
        BigDecimal totalReinsuranceRecovery = BigDecimal.ZERO;

        for (ReinsuranceTreatyLayer treaty : activeTreaties) {
            Map<String, Object> layerMap = new HashMap<>();
            layerMap.put("treatyNumber", treaty.getTreatyNumber());
            layerMap.put("reinsurerName", treaty.getReinsurerName());
            layerMap.put("treatyType", treaty.getTreatyType());

            BigDecimal recoveryAmount = BigDecimal.ZERO;
            if ("QuotaShare".equalsIgnoreCase(treaty.getTreatyType())) {
                recoveryAmount = totalClaimLoss.multiply(BigDecimal.valueOf(treaty.getCessionPercentage())).setScale(2, RoundingMode.HALF_UP);
            } else if ("ExcessOfLoss".equalsIgnoreCase(treaty.getTreatyType())) {
                if (totalClaimLoss.compareTo(treaty.getAttachmentPoint()) > 0) {
                    BigDecimal excess = totalClaimLoss.subtract(treaty.getAttachmentPoint());
                    BigDecimal maxRecovery = excess.min(treaty.getLayerLimit());
                    recoveryAmount = maxRecovery.multiply(BigDecimal.valueOf(treaty.getCessionPercentage())).setScale(2, RoundingMode.HALF_UP);
                }
            }

            layerMap.put("recoveryAmount", recoveryAmount);
            layerRecoveries.add(layerMap);
            totalReinsuranceRecovery = totalReinsuranceRecovery.add(recoveryAmount);
        }

        netInsurerLoss = totalClaimLoss.subtract(totalReinsuranceRecovery);
        if (netInsurerLoss.compareTo(BigDecimal.ZERO) < 0) netInsurerLoss = BigDecimal.ZERO;

        recoveryReport.put("totalReinsuranceRecovery", totalReinsuranceRecovery);
        recoveryReport.put("netInsurerRetainedLoss", netInsurerLoss);
        recoveryReport.put("layerRecoveries", layerRecoveries);

        LOGGER.log(Level.INFO, "Reinsurance Loss Recovery Simulated for ${0}: Reinsurance Recovers ${1}, Net Insurer Loss ${2}",
                new Object[]{totalClaimLoss, totalReinsuranceRecovery, netInsurerLoss});

        return recoveryReport;
    }
}
