package com.guidewire.pc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CatReinsuranceReinstatementEngine {
    private static final Logger LOGGER = Logger.getLogger(CatReinsuranceReinstatementEngine.class.getName());
    private static final CatReinsuranceReinstatementEngine INSTANCE = new CatReinsuranceReinstatementEngine();

    public static CatReinsuranceReinstatementEngine getInstance() {
        return INSTANCE;
    }

    public static class ReinstatementCalculationResult {
        public String treatyReferenceNumber;
        public String treatyLayerDescription; // e.g. $50M xs $25M Catastrophe Excess of Loss
        public BigDecimal treatyLayerLimit;
        public BigDecimal treatyAnnualCededPremium;
        public BigDecimal catastrophicLossAmount;
        public String dateOfLoss;
        public String treatyExpirationDate;
        public long totalTreatyDays;
        public long unexpiredTreatyDays;
        public BigDecimal amountFractionConsumed;
        public BigDecimal timeFractionRemaining;
        public double reinstatementRatePct; // e.g. 100%
        public BigDecimal reinstatementPremiumDue;
        public BigDecimal restoredTreatyCapacity;
        public String summaryAccountingNote;
    }

    public ReinstatementCalculationResult calculateCatReinstatement(String treatyRef, String layerDesc, BigDecimal layerLimit,
                                                                   BigDecimal annualPrem, BigDecimal catLoss,
                                                                   String treatyEffectiveDate, String lossDate, String expDate,
                                                                   double reinstRatePct) {
        LOGGER.log(Level.FINE, "→ CatReinsuranceReinstatementEngine.calculateCatReinstatement");
        ReinstatementCalculationResult res = new ReinstatementCalculationResult();
        res.treatyReferenceNumber = treatyRef != null ? treatyRef : "TREATY-CAT-2026-LAYER2";
        res.treatyLayerDescription = layerDesc != null ? layerDesc : "$50,000,000 xs $25,000,000 Coastal Hurricane CAT XOL Layer";

        res.treatyLayerLimit = layerLimit != null ? layerLimit : new BigDecimal("50000000.00");
        res.treatyAnnualCededPremium = annualPrem != null ? annualPrem : new BigDecimal("4000000.00");
        res.catastrophicLossAmount = catLoss != null ? catLoss : new BigDecimal("30000000.00");
        res.dateOfLoss = lossDate != null ? lossDate : "2026-07-01";
        res.treatyExpirationDate = expDate != null ? expDate : "2027-01-01";
        res.reinstatementRatePct = reinstRatePct > 0 ? reinstRatePct : 100.0;

        try {
            LocalDate eff = LocalDate.parse(treatyEffectiveDate != null ? treatyEffectiveDate : "2026-01-01");
            LocalDate loss = LocalDate.parse(res.dateOfLoss);
            LocalDate exp = LocalDate.parse(res.treatyExpirationDate);

            res.totalTreatyDays = ChronoUnit.DAYS.between(eff, exp);
            if (res.totalTreatyDays <= 0) res.totalTreatyDays = 365;

            long elapsed = ChronoUnit.DAYS.between(eff, loss);
            res.unexpiredTreatyDays = Math.max(0, res.totalTreatyDays - elapsed);

            // Amount fraction = Loss / Layer Limit
            res.amountFractionConsumed = res.catastrophicLossAmount.divide(res.treatyLayerLimit, 6, RoundingMode.HALF_UP);
            // Time fraction = Unexpired Days / Total Days
            res.timeFractionRemaining = new BigDecimal(res.unexpiredTreatyDays).divide(new BigDecimal(res.totalTreatyDays), 6, RoundingMode.HALF_UP);

            BigDecimal rateMultiplier = new BigDecimal(res.reinstatementRatePct / 100.0);

            // Reinstatement Premium = Annual Prem * Amount Fraction * Time Fraction * Reinstatement Rate %
            res.reinstatementPremiumDue = res.treatyAnnualCededPremium
                    .multiply(res.amountFractionConsumed)
                    .multiply(res.timeFractionRemaining)
                    .multiply(rateMultiplier)
                    .setScale(2, RoundingMode.HALF_UP);

            res.restoredTreatyCapacity = res.catastrophicLossAmount;
            res.summaryAccountingNote = "Catastrophe loss of $" + res.catastrophicLossAmount + " consumed " +
                    (res.amountFractionConsumed.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP)) +
                    "% of treaty layer. Payment of $" + res.reinstatementPremiumDue + " reinstatement premium restores 100% capacity for remaining " +
                    res.unexpiredTreatyDays + " days.";

        } catch (Exception e) {
            res.reinstatementPremiumDue = BigDecimal.ZERO;
            res.summaryAccountingNote = "Error calculating reinstatement: " + e.getMessage();
        }

        return res;
    }

    public Map<String, Object> toMap(ReinstatementCalculationResult r) {
        Map<String, Object> map = new HashMap<>();
        map.put("treatyReferenceNumber", r.treatyReferenceNumber);
        map.put("treatyLayerDescription", r.treatyLayerDescription);
        map.put("treatyLayerLimit", r.treatyLayerLimit);
        map.put("treatyAnnualCededPremium", r.treatyAnnualCededPremium);
        map.put("catastrophicLossAmount", r.catastrophicLossAmount);
        map.put("dateOfLoss", r.dateOfLoss);
        map.put("treatyExpirationDate", r.treatyExpirationDate);
        map.put("totalTreatyDays", r.totalTreatyDays);
        map.put("unexpiredTreatyDays", r.unexpiredTreatyDays);
        map.put("amountFractionConsumed", r.amountFractionConsumed);
        map.put("timeFractionRemaining", r.timeFractionRemaining);
        map.put("reinstatementRatePct", r.reinstatementRatePct);
        map.put("reinstatementPremiumDue", r.reinstatementPremiumDue);
        map.put("restoredTreatyCapacity", r.restoredTreatyCapacity);
        map.put("summaryAccountingNote", r.summaryAccountingNote);
        map.put("status", "SUCCESS");
        return map;
    }
}
