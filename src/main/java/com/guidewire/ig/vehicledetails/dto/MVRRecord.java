package com.guidewire.ig.vehicledetails.dto;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MVRRecord {
    private static final Logger LOGGER = Logger.getLogger(MVRRecord.class.getName());

    private String driverLicenseNumber;
    private String driverState;
    private String licenseStatus; // VALID, SUSPENDED, EXPIRED
    private Integer activeViolationPoints;
    private List<String> movingViolations;
    private Integer accidentsCount3Years;
    private Boolean majorDuiConviction;
    private String mvrStatus; // CLEAR, CAUTION, HIGH_RISK

    public MVRRecord() {
        LOGGER.log(Level.FINE, "→ MVRRecord.MVRRecord");}

    public MVRRecord(String driverLicenseNumber, String driverState, String licenseStatus, Integer activeViolationPoints, List<String> movingViolations, Integer accidentsCount3Years, Boolean majorDuiConviction, String mvrStatus) {
        LOGGER.log(Level.FINE, "→ MVRRecord.MVRRecord");
        this.driverLicenseNumber = driverLicenseNumber;
        this.driverState = driverState;
        this.licenseStatus = licenseStatus;
        this.activeViolationPoints = activeViolationPoints;
        this.movingViolations = movingViolations;
        this.accidentsCount3Years = accidentsCount3Years;
        this.majorDuiConviction = majorDuiConviction;
        this.mvrStatus = mvrStatus;
    }

    public String getDriverLicenseNumber() {
        LOGGER.log(Level.FINE, "→ MVRRecord.getDriverLicenseNumber"); return driverLicenseNumber; }
    public void setDriverLicenseNumber(String driverLicenseNumber) {
        LOGGER.log(Level.FINE, "→ MVRRecord.setDriverLicenseNumber"); this.driverLicenseNumber = driverLicenseNumber; }

    public String getDriverState() {
        LOGGER.log(Level.FINE, "→ MVRRecord.getDriverState"); return driverState; }
    public void setDriverState(String driverState) {
        LOGGER.log(Level.FINE, "→ MVRRecord.setDriverState"); this.driverState = driverState; }

    public String getLicenseStatus() {
        LOGGER.log(Level.FINE, "→ MVRRecord.getLicenseStatus"); return licenseStatus; }
    public void setLicenseStatus(String licenseStatus) {
        LOGGER.log(Level.FINE, "→ MVRRecord.setLicenseStatus"); this.licenseStatus = licenseStatus; }

    public Integer getActiveViolationPoints() {
        LOGGER.log(Level.FINE, "→ MVRRecord.getActiveViolationPoints"); return activeViolationPoints; }
    public void setActiveViolationPoints(Integer activeViolationPoints) {
        LOGGER.log(Level.FINE, "→ MVRRecord.setActiveViolationPoints"); this.activeViolationPoints = activeViolationPoints; }

    public List<String> getMovingViolations() {
        LOGGER.log(Level.FINE, "→ MVRRecord.getMovingViolations"); return movingViolations; }
    public void setMovingViolations(List<String> movingViolations) {
        LOGGER.log(Level.FINE, "→ MVRRecord.setMovingViolations"); this.movingViolations = movingViolations; }

    public Integer getAccidentsCount3Years() {
        LOGGER.log(Level.FINE, "→ MVRRecord.getAccidentsCount3Years"); return accidentsCount3Years; }
    public void setAccidentsCount3Years(Integer accidentsCount3Years) {
        LOGGER.log(Level.FINE, "→ MVRRecord.setAccidentsCount3Years"); this.accidentsCount3Years = accidentsCount3Years; }

    public Boolean getMajorDuiConviction() {
        LOGGER.log(Level.FINE, "→ MVRRecord.getMajorDuiConviction"); return majorDuiConviction; }
    public void setMajorDuiConviction(Boolean majorDuiConviction) {
        LOGGER.log(Level.FINE, "→ MVRRecord.setMajorDuiConviction"); this.majorDuiConviction = majorDuiConviction; }

    public String getMvrStatus() {
        LOGGER.log(Level.FINE, "→ MVRRecord.getMvrStatus"); return mvrStatus; }
    public void setMvrStatus(String mvrStatus) {
        LOGGER.log(Level.FINE, "→ MVRRecord.setMvrStatus"); this.mvrStatus = mvrStatus; }
}
