package com.guidewire.ig.vehicledetails.dto;

public class VehicleSpecs {
    private String vin;
    private String bodyClass;
    private String engineDisplacement;
    private String fuelType;
    private Integer grossVehicleWeightRating;
    private String antiTheftDeviceType;
    private Integer safetyRatingStars;
    private Boolean activeSafetyBraking;
    private Boolean laneDepartureWarning;

    public VehicleSpecs() {}

    public VehicleSpecs(String vin, String bodyClass, String engineDisplacement, String fuelType, Integer grossVehicleWeightRating, String antiTheftDeviceType, Integer safetyRatingStars, Boolean activeSafetyBraking, Boolean laneDepartureWarning) {
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

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getBodyClass() { return bodyClass; }
    public void setBodyClass(String bodyClass) { this.bodyClass = bodyClass; }

    public String getEngineDisplacement() { return engineDisplacement; }
    public void setEngineDisplacement(String engineDisplacement) { this.engineDisplacement = engineDisplacement; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public Integer getGrossVehicleWeightRating() { return grossVehicleWeightRating; }
    public void setGrossVehicleWeightRating(Integer grossVehicleWeightRating) { this.grossVehicleWeightRating = grossVehicleWeightRating; }

    public String getAntiTheftDeviceType() { return antiTheftDeviceType; }
    public void setAntiTheftDeviceType(String antiTheftDeviceType) { this.antiTheftDeviceType = antiTheftDeviceType; }

    public Integer getSafetyRatingStars() { return safetyRatingStars; }
    public void setSafetyRatingStars(Integer safetyRatingStars) { this.safetyRatingStars = safetyRatingStars; }

    public Boolean getActiveSafetyBraking() { return activeSafetyBraking; }
    public void setActiveSafetyBraking(Boolean activeSafetyBraking) { this.activeSafetyBraking = activeSafetyBraking; }

    public Boolean getLaneDepartureWarning() { return laneDepartureWarning; }
    public void setLaneDepartureWarning(Boolean laneDepartureWarning) { this.laneDepartureWarning = laneDepartureWarning; }
}
