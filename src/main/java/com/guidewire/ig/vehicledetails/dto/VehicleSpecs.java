package com.guidewire.ig.vehicledetails.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class VehicleSpecs {
    private static final Logger LOGGER = Logger.getLogger(VehicleSpecs.class.getName());

    private String vin;
    private String bodyClass;
    private String engineDisplacement;
    private String fuelType;
    private Integer grossVehicleWeightRating;
    private String antiTheftDeviceType;
    private Integer safetyRatingStars;
    private Boolean activeSafetyBraking;
    private Boolean laneDepartureWarning;

    public VehicleSpecs() {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.VehicleSpecs");}

    public VehicleSpecs(String vin, String bodyClass, String engineDisplacement, String fuelType, Integer grossVehicleWeightRating, String antiTheftDeviceType, Integer safetyRatingStars, Boolean activeSafetyBraking, Boolean laneDepartureWarning) {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.VehicleSpecs");
        this.vin = vin;
        this.bodyClass = bodyClass;
        this.engineDisplacement = engineDisplacement;
        this.fuelType = fuelType;
        this.grossVehicleWeightRating = grossVehicleWeightRating;
        this.antiTheftDeviceType = antiTheftDeviceType;
        this.safetyRatingStars = safetyRatingStars;
        this.activeSafetyBraking = activeSafetyBraking;
        this.laneDepartureWarning = laneDepartureWarning;
    }

    public String getVin() {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.getVin"); return vin; }
    public void setVin(String vin) {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.setVin"); this.vin = vin; }

    public String getBodyClass() {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.getBodyClass"); return bodyClass; }
    public void setBodyClass(String bodyClass) {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.setBodyClass"); this.bodyClass = bodyClass; }

    public String getEngineDisplacement() {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.getEngineDisplacement"); return engineDisplacement; }
    public void setEngineDisplacement(String engineDisplacement) {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.setEngineDisplacement"); this.engineDisplacement = engineDisplacement; }

    public String getFuelType() {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.getFuelType"); return fuelType; }
    public void setFuelType(String fuelType) {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.setFuelType"); this.fuelType = fuelType; }

    public Integer getGrossVehicleWeightRating() {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.getGrossVehicleWeightRating"); return grossVehicleWeightRating; }
    public void setGrossVehicleWeightRating(Integer grossVehicleWeightRating) {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.setGrossVehicleWeightRating"); this.grossVehicleWeightRating = grossVehicleWeightRating; }

    public String getAntiTheftDeviceType() {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.getAntiTheftDeviceType"); return antiTheftDeviceType; }
    public void setAntiTheftDeviceType(String antiTheftDeviceType) {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.setAntiTheftDeviceType"); this.antiTheftDeviceType = antiTheftDeviceType; }

    public Integer getSafetyRatingStars() {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.getSafetyRatingStars"); return safetyRatingStars; }
    public void setSafetyRatingStars(Integer safetyRatingStars) {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.setSafetyRatingStars"); this.safetyRatingStars = safetyRatingStars; }

    public Boolean getActiveSafetyBraking() {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.getActiveSafetyBraking"); return activeSafetyBraking; }
    public void setActiveSafetyBraking(Boolean activeSafetyBraking) {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.setActiveSafetyBraking"); this.activeSafetyBraking = activeSafetyBraking; }

    public Boolean getLaneDepartureWarning() {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.getLaneDepartureWarning"); return laneDepartureWarning; }
    public void setLaneDepartureWarning(Boolean laneDepartureWarning) {
        LOGGER.log(Level.FINE, "→ VehicleSpecs.setLaneDepartureWarning"); this.laneDepartureWarning = laneDepartureWarning; }
}
