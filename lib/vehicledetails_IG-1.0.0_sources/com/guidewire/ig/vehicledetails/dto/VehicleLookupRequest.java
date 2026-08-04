package com.guidewire.ig.vehicledetails.dto;

public class VehicleLookupRequest {
    private String vin;
    private Integer vehicleYear;
    private String vehicleMake;
    private String vehicleModel;
    private String driverLicenseNumber;
    private String driverState;
    private String policyType; // PersonalAuto, CommercialAuto

    public VehicleLookupRequest() {}

    public VehicleLookupRequest(String vin, Integer vehicleYear, String vehicleMake, String vehicleModel, String driverLicenseNumber, String driverState, String policyType) {
        this.vin = vin;
        this.vehicleYear = vehicleYear;
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.driverLicenseNumber = driverLicenseNumber;
        this.driverState = driverState;
        this.policyType = policyType;
    }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public Integer getVehicleYear() { return vehicleYear; }
    public void setVehicleYear(Integer vehicleYear) { this.vehicleYear = vehicleYear; }

    public String getVehicleMake() { return vehicleMake; }
    public void setVehicleMake(String vehicleMake) { this.vehicleMake = vehicleMake; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getDriverLicenseNumber() { return driverLicenseNumber; }
    public void setDriverLicenseNumber(String driverLicenseNumber) { this.driverLicenseNumber = driverLicenseNumber; }

    public String getDriverState() { return driverState; }
    public void setDriverState(String driverState) { this.driverState = driverState; }

    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }
}
