package com.guidewire.ig.vehicledetails.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class VehicleLookupRequest {
    private static final Logger LOGGER = Logger.getLogger(VehicleLookupRequest.class.getName());

    private String vin;
    private Integer vehicleYear;
    private String vehicleMake;
    private String vehicleModel;
    private String driverLicenseNumber;
    private String driverState;
    private String policyType; // PersonalAuto, CommercialAuto

    public VehicleLookupRequest() {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.VehicleLookupRequest");}

    public VehicleLookupRequest(String vin, Integer vehicleYear, String vehicleMake, String vehicleModel, String driverLicenseNumber, String driverState, String policyType) {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.VehicleLookupRequest");
        this.vin = vin;
        this.vehicleYear = vehicleYear;
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.driverLicenseNumber = driverLicenseNumber;
        this.driverState = driverState;
        this.policyType = policyType;
    }

    public String getVin() {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.getVin"); return vin; }
    public void setVin(String vin) {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.setVin"); this.vin = vin; }

    public Integer getVehicleYear() {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.getVehicleYear"); return vehicleYear; }
    public void setVehicleYear(Integer vehicleYear) {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.setVehicleYear"); this.vehicleYear = vehicleYear; }

    public String getVehicleMake() {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.getVehicleMake"); return vehicleMake; }
    public void setVehicleMake(String vehicleMake) {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.setVehicleMake"); this.vehicleMake = vehicleMake; }

    public String getVehicleModel() {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.getVehicleModel"); return vehicleModel; }
    public void setVehicleModel(String vehicleModel) {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.setVehicleModel"); this.vehicleModel = vehicleModel; }

    public String getDriverLicenseNumber() {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.getDriverLicenseNumber"); return driverLicenseNumber; }
    public void setDriverLicenseNumber(String driverLicenseNumber) {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.setDriverLicenseNumber"); this.driverLicenseNumber = driverLicenseNumber; }

    public String getDriverState() {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.getDriverState"); return driverState; }
    public void setDriverState(String driverState) {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.setDriverState"); this.driverState = driverState; }

    public String getPolicyType() {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.getPolicyType"); return policyType; }
    public void setPolicyType(String policyType) {
        LOGGER.log(Level.FINE, "→ VehicleLookupRequest.setPolicyType"); this.policyType = policyType; }
}
