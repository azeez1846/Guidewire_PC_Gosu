package com.guidewire.ig.telematics.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FleetSafetyScore {
    private static final Logger LOGGER = Logger.getLogger(FleetSafetyScore.class.getName());

    private Integer overallSafetyScore; // 0 - 100
    private Integer hardBrakingEventsPer1000Miles;
    private Integer rapidAccelerationsPer1000Miles;
    private Integer speedingViolationsCount;
    private Double averageMonthlyMilesPerVehicle;
    private Double usageBasedDiscountOrSurchargePct; // e.g. -0.12 (12% discount)

    public FleetSafetyScore() {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.FleetSafetyScore");}

    public FleetSafetyScore(Integer overallSafetyScore, Integer hardBrakingEventsPer1000Miles, Integer rapidAccelerationsPer1000Miles, Integer speedingViolationsCount, Double averageMonthlyMilesPerVehicle, Double usageBasedDiscountOrSurchargePct) {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.FleetSafetyScore");
        this.overallSafetyScore = overallSafetyScore;
        this.hardBrakingEventsPer1000Miles = hardBrakingEventsPer1000Miles;
        this.rapidAccelerationsPer1000Miles = rapidAccelerationsPer1000Miles;
        this.speedingViolationsCount = speedingViolationsCount;
        this.averageMonthlyMilesPerVehicle = averageMonthlyMilesPerVehicle;
        this.usageBasedDiscountOrSurchargePct = usageBasedDiscountOrSurchargePct;
    }

    public Integer getOverallSafetyScore() {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.getOverallSafetyScore"); return overallSafetyScore; }
    public void setOverallSafetyScore(Integer overallSafetyScore) {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.setOverallSafetyScore"); this.overallSafetyScore = overallSafetyScore; }

    public Integer getHardBrakingEventsPer1000Miles() {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.getHardBrakingEventsPer1000Miles"); return hardBrakingEventsPer1000Miles; }
    public void setHardBrakingEventsPer1000Miles(Integer hardBrakingEventsPer1000Miles) {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.setHardBrakingEventsPer1000Miles"); this.hardBrakingEventsPer1000Miles = hardBrakingEventsPer1000Miles; }

    public Integer getRapidAccelerationsPer1000Miles() {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.getRapidAccelerationsPer1000Miles"); return rapidAccelerationsPer1000Miles; }
    public void setRapidAccelerationsPer1000Miles(Integer rapidAccelerationsPer1000Miles) {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.setRapidAccelerationsPer1000Miles"); this.rapidAccelerationsPer1000Miles = rapidAccelerationsPer1000Miles; }

    public Integer getSpeedingViolationsCount() {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.getSpeedingViolationsCount"); return speedingViolationsCount; }
    public void setSpeedingViolationsCount(Integer speedingViolationsCount) {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.setSpeedingViolationsCount"); this.speedingViolationsCount = speedingViolationsCount; }

    public Double getAverageMonthlyMilesPerVehicle() {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.getAverageMonthlyMilesPerVehicle"); return averageMonthlyMilesPerVehicle; }
    public void setAverageMonthlyMilesPerVehicle(Double averageMonthlyMilesPerVehicle) {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.setAverageMonthlyMilesPerVehicle"); this.averageMonthlyMilesPerVehicle = averageMonthlyMilesPerVehicle; }

    public Double getUsageBasedDiscountOrSurchargePct() {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.getUsageBasedDiscountOrSurchargePct"); return usageBasedDiscountOrSurchargePct; }
    public void setUsageBasedDiscountOrSurchargePct(Double usageBasedDiscountOrSurchargePct) {
        LOGGER.log(Level.FINE, "→ FleetSafetyScore.setUsageBasedDiscountOrSurchargePct"); this.usageBasedDiscountOrSurchargePct = usageBasedDiscountOrSurchargePct; }
}
