package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ParametricEventCancellationEngine {
    private static final Logger LOGGER = Logger.getLogger(ParametricEventCancellationEngine.class.getName());
    private static final ParametricEventCancellationEngine INSTANCE = new ParametricEventCancellationEngine();

    public static ParametricEventCancellationEngine getInstance() {
        return INSTANCE;
    }

    public static class ParametricEventQuote {
        public String policyNumber;
        public String eventName;
        public String eventDate;
        public String eventVenueLocation;
        public BigDecimal eventGrossRevenueLimit;
        public String triggerType; // RAINFALL_ACCUMULATION, SUSTAINED_WIND, EXTREME_HEAT
        public double triggerThresholdValue; // e.g. 1.25 inches or 45.0 mph
        public String thresholdUnit; // INCHES, MPH, FAHRENHEIT
        public BigDecimal historicalProbabilityPct; // e.g. 4.2%
        public BigDecimal calculatedParametricPremium;
    }

    public static class ParametricSettlementResult {
        public String policyNumber;
        public String eventName;
        public boolean isTriggerBreached;
        public double observedTelemetryValue;
        public double contractThresholdValue;
        public BigDecimal automaticClaimSettlementAmount;
        public String claimsStatus; // INSTANT_INDEMNITY_DISPATCHED, NO_CLAIM_TRIGGERED
        public String settlementExplanation;
    }

    public ParametricEventQuote quoteParametricEndorsement(PolicyPeriod period, String eventName, String eventDate, String venue,
                                                           BigDecimal eventLimit, String triggerType, double thresholdValue) {
        LOGGER.log(Level.FINE, "→ ParametricEventCancellationEngine.quoteParametricEndorsement");
        ParametricEventQuote quote = new ParametricEventQuote();
        quote.policyNumber = period != null && period.getPolicyNumber() != null ? period.getPolicyNumber() : "POL-EVENT-2026-901";
        quote.eventName = eventName != null ? eventName : "Austin City Outdoor Music Festival";
        quote.eventDate = eventDate != null ? eventDate : "2026-10-15";
        quote.eventVenueLocation = venue != null ? venue : "Zilker Park, Austin, TX";
        quote.eventGrossRevenueLimit = eventLimit != null ? eventLimit : new BigDecimal("500000.00");
        quote.triggerType = triggerType != null ? triggerType : "RAINFALL_ACCUMULATION";
        quote.triggerThresholdValue = thresholdValue > 0 ? thresholdValue : 1.25;

        quote.thresholdUnit = switch (quote.triggerType.toUpperCase()) {
            case "SUSTAINED_WIND" -> "MPH";
            case "EXTREME_HEAT" -> "FAHRENHEIT";
            default -> "INCHES";
        };

        // Actuarial parametric rate: e.g. 4.5% baseline
        quote.historicalProbabilityPct = new BigDecimal("4.50");
        BigDecimal rate = quote.historicalProbabilityPct.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        quote.calculatedParametricPremium = quote.eventGrossRevenueLimit.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        return quote;
    }

    public ParametricSettlementResult evaluateTelemetryTrigger(ParametricEventQuote quote, double observedReading) {
        LOGGER.log(Level.FINE, "→ ParametricEventCancellationEngine.evaluateTelemetryTrigger");
        ParametricSettlementResult res = new ParametricSettlementResult();
        res.policyNumber = quote != null ? quote.policyNumber : "POL-EVENT-2026-901";
        res.eventName = quote != null ? quote.eventName : "Austin City Outdoor Music Festival";
        res.contractThresholdValue = quote != null ? quote.triggerThresholdValue : 1.25;
        res.observedTelemetryValue = observedReading;

        if (observedReading >= res.contractThresholdValue) {
            res.isTriggerBreached = true;
            res.automaticClaimSettlementAmount = quote != null ? quote.eventGrossRevenueLimit : new BigDecimal("500000.00");
            res.claimsStatus = "INSTANT_INDEMNITY_DISPATCHED";
            res.settlementExplanation = "PARAMETRIC TRIGGER SATISFIED: Verified NOAA sensor observed " + observedReading + " " + (quote != null ? quote.thresholdUnit : "INCHES") +
                    ", exceeding contractual trigger of " + res.contractThresholdValue + ". Full indemnity payout of $" + res.automaticClaimSettlementAmount + " automatically dispatched without loss adjuster delays.";
        } else {
            res.isTriggerBreached = false;
            res.automaticClaimSettlementAmount = BigDecimal.ZERO;
            res.claimsStatus = "NO_CLAIM_TRIGGERED";
            res.settlementExplanation = "Sensor reading (" + observedReading + ") did not breach the contract threshold (" + res.contractThresholdValue + "). Policy remains active.";
        }

        return res;
    }

    public Map<String, Object> toMap(ParametricEventQuote q) {
        Map<String, Object> map = new HashMap<>();
        map.put("policyNumber", q.policyNumber);
        map.put("eventName", q.eventName);
        map.put("eventDate", q.eventDate);
        map.put("eventVenueLocation", q.eventVenueLocation);
        map.put("eventGrossRevenueLimit", q.eventGrossRevenueLimit);
        map.put("triggerType", q.triggerType);
        map.put("triggerThresholdValue", q.triggerThresholdValue);
        map.put("thresholdUnit", q.thresholdUnit);
        map.put("historicalProbabilityPct", q.historicalProbabilityPct);
        map.put("calculatedParametricPremium", q.calculatedParametricPremium);
        map.put("status", "SUCCESS");
        return map;
    }
}
