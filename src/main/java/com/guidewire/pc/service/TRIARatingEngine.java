package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TRIARatingEngine {
    private static final Logger LOGGER = Logger.getLogger(TRIARatingEngine.class.getName());
    private static final TRIARatingEngine instance = new TRIARatingEngine();

    private TRIARatingEngine() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.TRIARatingEngine");}

    public static TRIARatingEngine getInstance() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.getInstance");
        return instance;
    }

    public TRIAResult evaluateTRIAOption(PolicyPeriod period, boolean optInTerrorismCoverage, double triaRatePct) {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.evaluateTRIAOption");
        TRIAResult result = new TRIAResult();
        if (period == null) return result;

        BigDecimal basePrem = period.getTotalPremium() != null ? period.getTotalPremium() : new BigDecimal("10000.00");
        if (triaRatePct <= 0) triaRatePct = 0.035; // Default 3.5% TRIA Surcharge

        result.setPolicyNumber(period.getPolicyNumber());
        result.setBaseSubjectPremium(basePrem);
        result.setOptInTerrorismCoverage(optInTerrorismCoverage);
        result.setTriaRatePercentage(triaRatePct);

        if (optInTerrorismCoverage) {
            BigDecimal triaPrem = basePrem.multiply(BigDecimal.valueOf(triaRatePct)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalPrem = basePrem.add(triaPrem);

            result.setTriaPremiumSurcharge(triaPrem);
            result.setFinalTotalPremium(totalPrem);
            result.setAttachedEndorsement("TRIA-COV-2026 (Federal Terrorism Coverage Certified)");
            result.setDisclosureFormAttached(true);
            result.setStatusMessage("TRIA Coverage Accepted - $ " + triaPrem + " Surcharge Applied");
        } else {
            result.setTriaPremiumSurcharge(BigDecimal.ZERO);
            result.setFinalTotalPremium(basePrem);
            result.setAttachedEndorsement("TRIA-EXCL-01 (Mandatory Policyholder Terrorism Exclusion)");
            result.setDisclosureFormAttached(true);
            result.setStatusMessage("TRIA Coverage Rejected - Rejection Form Executed");
        }

        LOGGER.log(Level.INFO, "TRIA Option evaluated for policy {0}: OptIn={1}, Surcharge=${2}, FinalPrem=${3}",
                new Object[]{period.getPolicyNumber(), optInTerrorismCoverage, result.getTriaPremiumSurcharge(), result.getFinalTotalPremium()});

        return result;
    }

    public static class TRIAResult {
        private String policyNumber;
        private BigDecimal baseSubjectPremium = BigDecimal.ZERO;
        private boolean optInTerrorismCoverage;
        private double triaRatePercentage;
        private BigDecimal triaPremiumSurcharge = BigDecimal.ZERO;
        private BigDecimal finalTotalPremium = BigDecimal.ZERO;
        private String attachedEndorsement;
        private boolean disclosureFormAttached;
        private String statusMessage;

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public BigDecimal getBaseSubjectPremium() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.getBaseSubjectPremium"); return baseSubjectPremium; }
        public void setBaseSubjectPremium(BigDecimal baseSubjectPremium) {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.setBaseSubjectPremium"); this.baseSubjectPremium = baseSubjectPremium; }

        public boolean isOptInTerrorismCoverage() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.isOptInTerrorismCoverage"); return optInTerrorismCoverage; }
        public void setOptInTerrorismCoverage(boolean optInTerrorismCoverage) {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.setOptInTerrorismCoverage"); this.optInTerrorismCoverage = optInTerrorismCoverage; }

        public double getTriaRatePercentage() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.getTriaRatePercentage"); return triaRatePercentage; }
        public void setTriaRatePercentage(double triaRatePercentage) {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.setTriaRatePercentage"); this.triaRatePercentage = triaRatePercentage; }

        public BigDecimal getTriaPremiumSurcharge() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.getTriaPremiumSurcharge"); return triaPremiumSurcharge; }
        public void setTriaPremiumSurcharge(BigDecimal triaPremiumSurcharge) {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.setTriaPremiumSurcharge"); this.triaPremiumSurcharge = triaPremiumSurcharge; }

        public BigDecimal getFinalTotalPremium() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.getFinalTotalPremium"); return finalTotalPremium; }
        public void setFinalTotalPremium(BigDecimal finalTotalPremium) {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.setFinalTotalPremium"); this.finalTotalPremium = finalTotalPremium; }

        public String getAttachedEndorsement() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.getAttachedEndorsement"); return attachedEndorsement; }
        public void setAttachedEndorsement(String attachedEndorsement) {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.setAttachedEndorsement"); this.attachedEndorsement = attachedEndorsement; }

        public boolean isDisclosureFormAttached() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.isDisclosureFormAttached"); return disclosureFormAttached; }
        public void setDisclosureFormAttached(boolean disclosureFormAttached) {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.setDisclosureFormAttached"); this.disclosureFormAttached = disclosureFormAttached; }

        public String getStatusMessage() {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.getStatusMessage"); return statusMessage; }
        public void setStatusMessage(String statusMessage) {
        LOGGER.log(Level.FINE, "→ TRIARatingEngine.setStatusMessage"); this.statusMessage = statusMessage; }
    }
}
