package com.guidewire.ig.telematics.dto;

public class TelematicsLookupRequest {
    private String fleetId;
    private String accountNumber;
    private Integer activeVehiclesCount;

    public TelematicsLookupRequest() {}

    public TelematicsLookupRequest(String fleetId, String accountNumber, Integer activeVehiclesCount) {
        this.fleetId = fleetId;
        this.accountNumber = accountNumber;
        this.activeVehiclesCount = activeVehiclesCount;
    }

    public String getFleetId() { return fleetId; }
    public void setFleetId(String fleetId) { this.fleetId = fleetId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public Integer getActiveVehiclesCount() { return activeVehiclesCount; }
    public void setActiveVehiclesCount(Integer activeVehiclesCount) { this.activeVehiclesCount = activeVehiclesCount; }
}
