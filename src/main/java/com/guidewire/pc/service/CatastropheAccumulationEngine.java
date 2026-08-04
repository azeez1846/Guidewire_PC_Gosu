package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CatastropheAccumulationEngine {
    private static final Logger LOGGER = Logger.getLogger(CatastropheAccumulationEngine.class.getName());
    private static final CatastropheAccumulationEngine instance = new CatastropheAccumulationEngine();

    private final BigDecimal MAX_POSTAL_CODE_AGGREGATION_LIMIT = new BigDecimal("10000000.00"); // $10M

    private CatastropheAccumulationEngine() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.CatastropheAccumulationEngine");}

    public static CatastropheAccumulationEngine getInstance() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.getInstance");
        return instance;
    }

    public CatAccumulationResult evaluateRiskAccumulation(PolicyPeriod period, String postalCode, String perilZone, BigDecimal buildingLimit) {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.evaluateRiskAccumulation");
        CatAccumulationResult result = new CatAccumulationResult();
        if (buildingLimit == null) buildingLimit = new BigDecimal("1000000.00");

        result.setPolicyNumber(period != null ? period.getPolicyNumber() : "POL-CAT-1001");
        result.setPostalCode(postalCode != null ? postalCode : "90210");
        result.setPerilZone(perilZone != null ? perilZone : "Wildfire_High");
        result.setBuildingLimit(buildingLimit);

        // Calculate Probable Maximum Loss (PML - 70% of limit in High Peril Zone)
        double pmlFactor = "Wildfire_High".equalsIgnoreCase(perilZone) || "Hurricane_Cat5".equalsIgnoreCase(perilZone) ? 0.70 : 0.40;
        BigDecimal pml = buildingLimit.multiply(BigDecimal.valueOf(pmlFactor)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal aal = buildingLimit.multiply(new BigDecimal("0.025")).setScale(2, RoundingMode.HALF_UP); // 2.5% AAL

        result.setPmlAmount(pml);
        result.setAalAmount(aal);

        // Simulated current aggregation for postal code ($7.5M existing)
        BigDecimal currentPostalCodeTiv = new BigDecimal("7500000.00").add(buildingLimit);
        result.setTotalPostalCodeExposure(currentPostalCodeTiv);

        if (currentPostalCodeTiv.compareTo(MAX_POSTAL_CODE_AGGREGATION_LIMIT) > 0) {
            result.setAggregationCapExceeded(true);
            result.setUnderwritingAction("UW_APPROVAL_REQUIRED: CAT Aggregation Limit Exceeded ($10M Cap)");
        } else {
            result.setAggregationCapExceeded(false);
            result.setUnderwritingAction("PASSED: Within CAT Aggregation Limits");
        }

        LOGGER.log(Level.INFO, "Catastrophe Risk evaluated for policy {0} (PostalCode={1}, Peril={2}): PML=${3}, CapExceeded={4}",
                new Object[]{result.getPolicyNumber(), result.getPostalCode(), result.getPerilZone(), pml, result.isAggregationCapExceeded()});

        return result;
    }

    public static class CatAccumulationResult {
        private String policyNumber;
        private String postalCode;
        private String perilZone;
        private BigDecimal buildingLimit = BigDecimal.ZERO;
        private BigDecimal pmlAmount = BigDecimal.ZERO;
        private BigDecimal aalAmount = BigDecimal.ZERO;
        private BigDecimal totalPostalCodeExposure = BigDecimal.ZERO;
        private boolean aggregationCapExceeded;
        private String underwritingAction;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public String getPostalCode() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.getPostalCode"); return postalCode; }
        public void setPostalCode(String postalCode) {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.setPostalCode"); this.postalCode = postalCode; }

        public String getPerilZone() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.getPerilZone"); return perilZone; }
        public void setPerilZone(String perilZone) {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.setPerilZone"); this.perilZone = perilZone; }

        public BigDecimal getBuildingLimit() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.getBuildingLimit"); return buildingLimit; }
        public void setBuildingLimit(BigDecimal buildingLimit) {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.setBuildingLimit"); this.buildingLimit = buildingLimit; }

        public BigDecimal getPmlAmount() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.getPmlAmount"); return pmlAmount; }
        public void setPmlAmount(BigDecimal pmlAmount) {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.setPmlAmount"); this.pmlAmount = pmlAmount; }

        public BigDecimal getAalAmount() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.getAalAmount"); return aalAmount; }
        public void setAalAmount(BigDecimal aalAmount) {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.setAalAmount"); this.aalAmount = aalAmount; }

        public BigDecimal getTotalPostalCodeExposure() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.getTotalPostalCodeExposure"); return totalPostalCodeExposure; }
        public void setTotalPostalCodeExposure(BigDecimal totalPostalCodeExposure) {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.setTotalPostalCodeExposure"); this.totalPostalCodeExposure = totalPostalCodeExposure; }

        public boolean isAggregationCapExceeded() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.isAggregationCapExceeded"); return aggregationCapExceeded; }
        public void setAggregationCapExceeded(boolean aggregationCapExceeded) {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.setAggregationCapExceeded"); this.aggregationCapExceeded = aggregationCapExceeded; }

        public String getUnderwritingAction() {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.getUnderwritingAction"); return underwritingAction; }
        public void setUnderwritingAction(String underwritingAction) {
        LOGGER.log(Level.FINE, "→ CatastropheAccumulationEngine.setUnderwritingAction"); this.underwritingAction = underwritingAction; }
    }
}
