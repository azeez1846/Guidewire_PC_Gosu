package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AutoFleetRadiusHazmatEngine {
    private static final Logger LOGGER = Logger.getLogger(AutoFleetRadiusHazmatEngine.class.getName());
    private static final AutoFleetRadiusHazmatEngine INSTANCE = new AutoFleetRadiusHazmatEngine();

    public static AutoFleetRadiusHazmatEngine getInstance() {
        return INSTANCE;
    }

    public static class FleetRatingResult {
        public String policyNumber;
        public int vehicleCount;
        public BigDecimal baseFleetLiabilityPremium;
        public String operatingRadiusClass; // LOCAL, INTERMEDIATE, LONG_DISTANCE
        public BigDecimal radiusMultiplier;
        public BigDecimal radiusAdjustedPremium;
        public String dotHazmatClass; // NON_HAZARDOUS, CLASS_3_FLAMMABLE, CLASS_1_EXPLOSIVES, CLASS_8_CORROSIVE
        public BigDecimal hazmatSurchargePct;
        public BigDecimal hazmatSurchargeAmount;
        public boolean attachCa9948PollutionEndorsement;
        public BigDecimal pollutionEndorsementPremium;
        public BigDecimal totalCommercialAutoFleetPremium;
    }

    public FleetRatingResult rateFleetRadiusAndHazmat(PolicyPeriod period, int vehicles, BigDecimal basePremPerVehicle,
                                                      String radiusClass, String hazmatClass, boolean attachPollution) {
        LOGGER.log(Level.FINE, "→ AutoFleetRadiusHazmatEngine.rateFleetRadiusAndHazmat");
        FleetRatingResult res = new FleetRatingResult();
        res.policyNumber = period != null && period.getPolicyNumber() != null ? period.getPolicyNumber() : "POL-CA-FLEET-6001";
        res.vehicleCount = vehicles > 0 ? vehicles : 10;

        BigDecimal basePerUnit = basePremPerVehicle != null ? basePremPerVehicle : new BigDecimal("2200.00");
        res.baseFleetLiabilityPremium = basePerUnit.multiply(new BigDecimal(res.vehicleCount)).setScale(2, RoundingMode.HALF_UP);

        res.operatingRadiusClass = radiusClass != null ? radiusClass.toUpperCase() : "LOCAL";
        res.radiusMultiplier = switch (res.operatingRadiusClass) {
            case "LONG_DISTANCE" -> new BigDecimal("1.60"); // > 200 miles
            case "INTERMEDIATE" -> new BigDecimal("1.25");  // 50-200 miles
            default -> new BigDecimal("1.00");              // Local < 50 miles
        };
        res.radiusAdjustedPremium = res.baseFleetLiabilityPremium.multiply(res.radiusMultiplier).setScale(2, RoundingMode.HALF_UP);

        res.dotHazmatClass = hazmatClass != null ? hazmatClass.toUpperCase() : "NON_HAZARDOUS";
        res.hazmatSurchargePct = switch (res.dotHazmatClass) {
            case "CLASS_1_EXPLOSIVES", "CLASS_7_RADIOACTIVE" -> new BigDecimal("0.85");
            case "CLASS_8_CORROSIVE", "CLASS_2_GASES" -> new BigDecimal("0.50");
            case "CLASS_3_FLAMMABLE" -> new BigDecimal("0.40");
            default -> BigDecimal.ZERO;
        };

        res.hazmatSurchargeAmount = res.radiusAdjustedPremium.multiply(res.hazmatSurchargePct).setScale(2, RoundingMode.HALF_UP);

        res.attachCa9948PollutionEndorsement = attachPollution;
        if (res.attachCa9948PollutionEndorsement) {
            // CA 99 48 Pollution Liability Broadened Coverage for Autos ($1,250 endorsement base)
            res.pollutionEndorsementPremium = new BigDecimal("1250.00");
        } else {
            res.pollutionEndorsementPremium = BigDecimal.ZERO;
        }

        res.totalCommercialAutoFleetPremium = res.radiusAdjustedPremium.add(res.hazmatSurchargeAmount).add(res.pollutionEndorsementPremium);

        return res;
    }

    public Map<String, Object> toMap(FleetRatingResult r) {
        Map<String, Object> map = new HashMap<>();
        map.put("policyNumber", r.policyNumber);
        map.put("vehicleCount", r.vehicleCount);
        map.put("baseFleetLiabilityPremium", r.baseFleetLiabilityPremium);
        map.put("operatingRadiusClass", r.operatingRadiusClass);
        map.put("radiusMultiplier", r.radiusMultiplier);
        map.put("radiusAdjustedPremium", r.radiusAdjustedPremium);
        map.put("dotHazmatClass", r.dotHazmatClass);
        map.put("hazmatSurchargePct", r.hazmatSurchargePct);
        map.put("hazmatSurchargeAmount", r.hazmatSurchargeAmount);
        map.put("attachCa9948PollutionEndorsement", r.attachCa9948PollutionEndorsement);
        map.put("pollutionEndorsementPremium", r.pollutionEndorsementPremium);
        map.put("totalCommercialAutoFleetPremium", r.totalCommercialAutoFleetPremium);
        map.put("status", "SUCCESS");
        return map;
    }
}
