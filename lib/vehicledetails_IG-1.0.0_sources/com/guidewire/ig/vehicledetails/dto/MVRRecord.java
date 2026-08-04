package com.guidewire.ig.vehicledetails.dto;

import java.util.List;

public class MVRRecord {
    private String driverLicenseNumber;
    private String driverState;
    private String licenseStatus; // VALID, SUSPENDED, EXPIRED
    private Integer activeViolationPoints;
    private List<String> movingViolations;
    private Integer accidentsCount3Years;
    private Boolean majorDuiConviction;
    private String mvrStatus; // CLEAR, CAUTION, HIGH_RISK

    public MVRRecord() {}

    public MVRRecord(String driverLicenseNumber, String driverState, String licenseStatus, Integer activeViolationPoints, List<String> movingViolations, Integer accidentsCount3Years, Boolean majorDuiConviction, String mvrStatus) {
        this.driverLicenseNumber = driverLicenseNumber;
        this.driverState = driverState;
        this.licenseStatus = licenseStatus;
        this.activeViolationPoints = activeViolationPoints;
        this.movingViolations = movingViolations;
        this.accidentsCount3Years = accidentsCount3Years;
        this.majorDuiConviction = majorDuiConviction;
        this.mvrStatus = mvrStatus;
    }

    public String getDriverLicenseNumber() { return driverLicenseNumber; }
    public void setDriverLicenseNumber(String driverLicenseNumber) { this.driverLicenseNumber = driverLicenseNumber; }

    public String getDriverState() { return driverState; }
    public void setDriverState(String driverState) { this.driverState = driverState; }

    public String getLicenseStatus() { return licenseStatus; }
    public void setLicenseStatus(String licenseStatus) { this.licenseStatus = licenseStatus; }

    public Integer getActiveViolationPoints() { return activeViolationPoints; }
    public void setActiveViolationPoints(Integer activeViolationPoints) { this.activeViolationPoints = activeViolationPoints; }

    public List<String> getMovingViolations() { return movingViolations; }
    public void setMovingViolations(List<String> movingViolations) { this.movingViolations = movingViolations; }

    public Integer getAccidentsCount3Years() { return accidentsCount3Years; }
    public void setAccidentsCount3Years(Integer accidentsCount3Years) { this.accidentsCount3Years = accidentsCount3Years; }

    public Boolean getMajorDuiConviction() { return majorDuiConviction; }
    public void setMajorDuiConviction(Boolean majorDuiConviction) { this.majorDuiConviction = majorDuiConviction; }

    public String getMvrStatus() { return mvrStatus; }
    public void setMvrStatus(String mvrStatus) { this.mvrStatus = mvrStatus; }
}
