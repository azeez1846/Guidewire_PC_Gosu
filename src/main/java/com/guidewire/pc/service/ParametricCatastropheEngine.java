package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parametric Climate & Catastrophe Payout Engine.
 * Evaluates real-time weather and seismic triggers (e.g. Sustained Wind Speed, Earthquake Magnitude)
 * to issue instant parametric claims settlements without physical loss adjustment.
 */
public class ParametricCatastropheEngine {
    private static final Logger LOGGER = Logger.getLogger(ParametricCatastropheEngine.class.getName());
    private static final ParametricCatastropheEngine instance = new ParametricCatastropheEngine();

    // Trigger Threshold Defaults
    private final double WIND_SPEED_TRIGGER_KNOTS = 120.0; // Category 4 Hurricane
    private final double SEISMIC_MAGNITUDE_TRIGGER = 6.8;   // Richter Scale Magnitude

    private ParametricCatastropheEngine() {
        LOGGER.log(Level.FINE, "ParametricCatastropheEngine initialized");
    }

    public static ParametricCatastropheEngine getInstance() {
        return instance;
    }

    public ParametricEvaluationResult evaluateWindspeedTrigger(PolicyPeriod period, String postalCode, double recordedWindSpeedKnots, BigDecimal policyLimit) {
        if (policyLimit == null) policyLimit = new BigDecimal("500000.00");

        ParametricEvaluationResult result = new ParametricEvaluationResult();
        result.setPolicyNumber(period != null ? period.getPolicyNumber() : "POL-PARAM-3001");
        result.setPerilType("HURRICANE_WIND");
        result.setPostalCode(postalCode != null ? postalCode : "33101");
        result.setRecordedMetricValue(recordedWindSpeedKnots);
        result.setTriggerThresholdValue(WIND_SPEED_TRIGGER_KNOTS);
        result.setPolicyLimit(policyLimit);

        if (recordedWindSpeedKnots >= WIND_SPEED_TRIGGER_KNOTS) {
            result.setTriggered(true);

            // Payout Tier Calculation (100% payout at >= 140 knots, 75% at >= 120 knots)
            double payoutFactor = recordedWindSpeedKnots >= 140.0 ? 1.00 : 0.75;
            BigDecimal payoutAmount = policyLimit.multiply(BigDecimal.valueOf(payoutFactor)).setScale(2, RoundingMode.HALF_UP);

            result.setPayoutFactor(payoutFactor);
            result.setCalculatedPayoutAmount(payoutAmount);
            result.setStatus("TRIGGERED_CLAIM_PAYOUT_INITIATED");
            result.setPayoutReference("PAYOUT-PARAM-" + System.currentTimeMillis());
        } else {
            result.setTriggered(false);
            result.setPayoutFactor(0.0);
            result.setCalculatedPayoutAmount(BigDecimal.ZERO.setScale(2));
            result.setStatus("THRESHOLD_NOT_MET");
            result.setPayoutReference("N/A");
        }

        LOGGER.log(Level.INFO, "Parametric Hurricane Wind evaluation for {0} (PostalCode={1}): Recorded={2} knots, Triggered={3}, Payout=${4}",
                new Object[]{result.getPolicyNumber(), result.getPostalCode(), recordedWindSpeedKnots, result.isTriggered(), result.getCalculatedPayoutAmount()});

        return result;
    }

    public ParametricEvaluationResult evaluateEarthquakeTrigger(PolicyPeriod period, String postalCode, double recordedMagnitude, BigDecimal policyLimit) {
        if (policyLimit == null) policyLimit = new BigDecimal("1000000.00");

        ParametricEvaluationResult result = new ParametricEvaluationResult();
        result.setPolicyNumber(period != null ? period.getPolicyNumber() : "POL-PARAM-3002");
        result.setPerilType("EARTHQUAKE_SEISMIC");
        result.setPostalCode(postalCode != null ? postalCode : "94102");
        result.setRecordedMetricValue(recordedMagnitude);
        result.setTriggerThresholdValue(SEISMIC_MAGNITUDE_TRIGGER);
        result.setPolicyLimit(policyLimit);

        if (recordedMagnitude >= SEISMIC_MAGNITUDE_TRIGGER) {
            result.setTriggered(true);
            double payoutFactor = recordedMagnitude >= 7.5 ? 1.00 : 0.80;
            BigDecimal payoutAmount = policyLimit.multiply(BigDecimal.valueOf(payoutFactor)).setScale(2, RoundingMode.HALF_UP);

            result.setPayoutFactor(payoutFactor);
            result.setCalculatedPayoutAmount(payoutAmount);
            result.setStatus("TRIGGERED_CLAIM_PAYOUT_INITIATED");
            result.setPayoutReference("PAYOUT-PARAM-EQ-" + System.currentTimeMillis());
        } else {
            result.setTriggered(false);
            result.setPayoutFactor(0.0);
            result.setCalculatedPayoutAmount(BigDecimal.ZERO);
            result.setStatus("THRESHOLD_NOT_MET");
            result.setPayoutReference("N/A");
        }

        LOGGER.log(Level.INFO, "Parametric Earthquake evaluation for {0} (PostalCode={1}): Recorded Mag={2}, Triggered={3}, Payout=${4}",
                new Object[]{result.getPolicyNumber(), result.getPostalCode(), recordedMagnitude, result.isTriggered(), result.getCalculatedPayoutAmount()});

        return result;
    }

    public static class ParametricEvaluationResult {
        private String policyNumber;
        private String perilType;
        private String postalCode;
        private double recordedMetricValue;
        private double triggerThresholdValue;
        private BigDecimal policyLimit;
        private boolean triggered;
        private double payoutFactor;
        private BigDecimal calculatedPayoutAmount;
        private String status;
        private String payoutReference;

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public String getPerilType() { return perilType; }
        public void setPerilType(String perilType) { this.perilType = perilType; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        public double getRecordedMetricValue() { return recordedMetricValue; }
        public void setRecordedMetricValue(double recordedMetricValue) { this.recordedMetricValue = recordedMetricValue; }

        public double getTriggerThresholdValue() { return triggerThresholdValue; }
        public void setTriggerThresholdValue(double triggerThresholdValue) { this.triggerThresholdValue = triggerThresholdValue; }

        public BigDecimal getPolicyLimit() { return policyLimit; }
        public void setPolicyLimit(BigDecimal policyLimit) { this.policyLimit = policyLimit; }

        public boolean isTriggered() { return triggered; }
        public void setTriggered(boolean triggered) { this.triggered = triggered; }

        public double getPayoutFactor() { return payoutFactor; }
        public void setPayoutFactor(double payoutFactor) { this.payoutFactor = payoutFactor; }

        public BigDecimal getCalculatedPayoutAmount() { return calculatedPayoutAmount; }
        public void setCalculatedPayoutAmount(BigDecimal calculatedPayoutAmount) { this.calculatedPayoutAmount = calculatedPayoutAmount; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getPayoutReference() { return payoutReference; }
        public void setPayoutReference(String payoutReference) { this.payoutReference = payoutReference; }
    }
}
