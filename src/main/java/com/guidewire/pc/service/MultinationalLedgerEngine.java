package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultinationalLedgerEngine {
    private static final Logger LOGGER = Logger.getLogger(MultinationalLedgerEngine.class.getName());
    private static final MultinationalLedgerEngine instance = new MultinationalLedgerEngine();

    private final Map<String, Double> exchangeRates = new HashMap<>();
    private final Map<String, Double> localTaxRates = new HashMap<>();

    private MultinationalLedgerEngine() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.MultinationalLedgerEngine");
        // Exchange rates relative to USD (1 USD = X Local Currency)
        exchangeRates.put("GBP", 0.78);  // UK Pound
        exchangeRates.put("EUR", 0.92);  // Euro
        exchangeRates.put("JPY", 149.50); // Japanese Yen
        exchangeRates.put("CAD", 1.36);  // Canadian Dollar

        // Local Insurance Premium Tax (IPT) / VAT Rates
        localTaxRates.put("GBP", 0.12); // UK 12% IPT
        localTaxRates.put("EUR", 0.19); // Germany 19% VersSt
        localTaxRates.put("JPY", 0.10); // Japan 10% Local Tax
        localTaxRates.put("CAD", 0.08); // Canada 8% Tax
    }

    public static MultinationalLedgerEngine getInstance() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.getInstance");
        return instance;
    }

    public List<LocalFrontingPolicyResult> generateMultinationalLedger(PolicyPeriod globalMasterPolicy) {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.generateMultinationalLedger");
        List<LocalFrontingPolicyResult> frontingPolicies = new ArrayList<>();
        if (globalMasterPolicy == null || globalMasterPolicy.getTotalPremium() == null) return frontingPolicies;

        BigDecimal masterPremUSD = globalMasterPolicy.getTotalPremium();

        for (Map.Entry<String, Double> entry : exchangeRates.entrySet()) {
            String currency = entry.getKey();
            double fxRate = entry.getValue();
            double taxRate = localTaxRates.getOrDefault(currency, 0.10);

            BigDecimal localPrem = masterPremUSD.multiply(BigDecimal.valueOf(fxRate)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal localTax = localPrem.multiply(BigDecimal.valueOf(taxRate)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalLocalCost = localPrem.add(localTax);

            LocalFrontingPolicyResult fronting = new LocalFrontingPolicyResult();
            fronting.setMasterPolicyNumber(globalMasterPolicy.getPolicyNumber());
            fronting.setFrontingPolicyNumber("FP-" + currency + "-" + (globalMasterPolicy.getPolicyNumber() != null ? globalMasterPolicy.getPolicyNumber() : "1001"));
            fronting.setJurisdiction(currency);
            fronting.setCurrencyCode(currency);
            fronting.setFxRateToUSD(fxRate);
            fronting.setLocalPremium(localPrem);
            fronting.setLocalTaxAmount(localTax);
            fronting.setTotalLocalCost(totalLocalCost);
            fronting.setMasterPremiumUSD(masterPremUSD);

            frontingPolicies.add(fronting);
        }

        LOGGER.log(Level.INFO, "Generated Multinational Ledger for Master Policy {0}: {1} local fronting policies",
                new Object[]{globalMasterPolicy.getPolicyNumber(), frontingPolicies.size()});

        return frontingPolicies;
    }

    public static class LocalFrontingPolicyResult {
        private String masterPolicyNumber;
        private String frontingPolicyNumber;
        private String jurisdiction;
        private String currencyCode;
        private double fxRateToUSD;
        private BigDecimal localPremium = BigDecimal.ZERO;
        private BigDecimal localTaxAmount = BigDecimal.ZERO;
        private BigDecimal totalLocalCost = BigDecimal.ZERO;
        private BigDecimal masterPremiumUSD = BigDecimal.ZERO;

        public String getMasterPolicyNumber() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.getMasterPolicyNumber"); return masterPolicyNumber; }
        public void setMasterPolicyNumber(String masterPolicyNumber) {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.setMasterPolicyNumber"); this.masterPolicyNumber = masterPolicyNumber; }

        public String getFrontingPolicyNumber() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.getFrontingPolicyNumber"); return frontingPolicyNumber; }
        public void setFrontingPolicyNumber(String frontingPolicyNumber) {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.setFrontingPolicyNumber"); this.frontingPolicyNumber = frontingPolicyNumber; }

        public String getJurisdiction() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.getJurisdiction"); return jurisdiction; }
        public void setJurisdiction(String jurisdiction) {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.setJurisdiction"); this.jurisdiction = jurisdiction; }

        public String getCurrencyCode() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.getCurrencyCode"); return currencyCode; }
        public void setCurrencyCode(String currencyCode) {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.setCurrencyCode"); this.currencyCode = currencyCode; }

        public double getFxRateToUSD() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.getFxRateToUSD"); return fxRateToUSD; }
        public void setFxRateToUSD(double fxRateToUSD) {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.setFxRateToUSD"); this.fxRateToUSD = fxRateToUSD; }

        public BigDecimal getLocalPremium() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.getLocalPremium"); return localPremium; }
        public void setLocalPremium(BigDecimal localPremium) {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.setLocalPremium"); this.localPremium = localPremium; }

        public BigDecimal getLocalTaxAmount() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.getLocalTaxAmount"); return localTaxAmount; }
        public void setLocalTaxAmount(BigDecimal localTaxAmount) {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.setLocalTaxAmount"); this.localTaxAmount = localTaxAmount; }

        public BigDecimal getTotalLocalCost() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.getTotalLocalCost"); return totalLocalCost; }
        public void setTotalLocalCost(BigDecimal totalLocalCost) {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.setTotalLocalCost"); this.totalLocalCost = totalLocalCost; }

        public BigDecimal getMasterPremiumUSD() {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.getMasterPremiumUSD"); return masterPremiumUSD; }
        public void setMasterPremiumUSD(BigDecimal masterPremiumUSD) {
        LOGGER.log(Level.FINE, "→ MultinationalLedgerEngine.setMasterPremiumUSD"); this.masterPremiumUSD = masterPremiumUSD; }
    }
}
