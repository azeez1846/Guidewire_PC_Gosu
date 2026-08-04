package com.guidewire.ig.telematics.dto;

public class FleetSafetyScore {
    private Integer overallSafetyScore; // 0 - 100
    private Integer hardBrakingEventsPer1000Miles;
    private Integer rapidAccelerationsPer1000Miles;
    private Integer speedingViolationsCount;
    private Double averageMonthlyMilesPerVehicle;
    private Double usageBasedDiscountOrSurchargePct; // e.g. -0.12 (12% discount)

    public FleetSafetyScore() {}

    public FleetSafetyScore(Integer overallSafetyScore, Integer hardBrakingEventsPer1000Miles, Integer rapidAccelerationsPer1000Miles, Integer speedingViolationsCount, Double averageMonthlyMilesPerVehicle, Double usageBasedDiscountOrSurchargePct) {
        this.overallSafetyScore = overallSafetyScore;
        this.hardBrakingEventsPer1000Miles = hardBrakingEventsPer1000Miles;
        this.rapidAccelerationsPer1000Miles = rapidAccelerationsPer1000Miles;
        this.speedingViolationsCount = speedingViolationsCount;
        this.averageMonthlyMilesPerVehicle = averageMonthlyMilesPerVehicle;
        this.usageBasedDiscountOrSurchargePct = usageBasedDiscountOrSurchargePct;
    }

    public Integer getOverallSafetyScore() { return overallSafetyScore; }
    public void setOverallSafetyScore(Integer overallSafetyScore) { this.overallSafetyScore = overallSafetyScore; }

    public Integer getHardBrakingEventsPer1000Miles() { return hardBrakingEventsPer1000Miles; }
    public void setHardBrakingEventsPer1000Miles(Integer hardBrakingEventsPer1000Miles) { this.hardBrakingEventsPer1000Miles = hardBrakingEventsPer1000Miles; }

    public Integer getRapidAccelerationsPer1000Miles() { return rapidAccelerationsPer1000Miles; }
    public void setRapidAccelerationsPer1000Miles(Integer rapidAccelerationsPer1000Miles) { this.rapidAccelerationsPer1000Miles = rapidAccelerationsPer1000Miles; }

    public Integer getSpeedingViolationsCount() { return speedingViolationsCount; }
    public void setSpeedingViolationsCount(Integer speedingViolationsCount) { this.speedingViolationsCount = speedingViolationsCount; }

    public Double getAverageMonthlyMilesPerVehicle() { return averageMonthlyMilesPerVehicle; }
    public void setAverageMonthlyMilesPerVehicle(Double averageMonthlyMilesPerVehicle) { this.averageMonthlyMilesPerVehicle = averageMonthlyMilesPerVehicle; }

    public Double getUsageBasedDiscountOrSurchargePct() { return usageBasedDiscountOrSurchargePct; }
    public void setUsageBasedDiscountOrSurchargePct(Double usageBasedDiscountOrSurchargePct) { this.usageBasedDiscountOrSurchargePct = usageBasedDiscountOrSurchargePct; }
}
