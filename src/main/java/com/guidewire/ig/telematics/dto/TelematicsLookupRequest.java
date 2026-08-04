package com.guidewire.ig.telematics.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TelematicsLookupRequest {
    private static final Logger LOGGER = Logger.getLogger(TelematicsLookupRequest.class.getName());

    private String fleetId;
    private String accountNumber;
    private Integer activeVehiclesCount;

    public TelematicsLookupRequest() {
        LOGGER.log(Level.FINE, "→ TelematicsLookupRequest.TelematicsLookupRequest");}

    public TelematicsLookupRequest(String fleetId, String accountNumber, Integer activeVehiclesCount) {
        LOGGER.log(Level.FINE, "→ TelematicsLookupRequest.TelematicsLookupRequest");
        this.fleetId = fleetId;
        this.accountNumber = accountNumber;
        this.activeVehiclesCount = activeVehiclesCount;
    }

    public String getFleetId() {
        LOGGER.log(Level.FINE, "→ TelematicsLookupRequest.getFleetId"); return fleetId; }
    public void setFleetId(String fleetId) {
        LOGGER.log(Level.FINE, "→ TelematicsLookupRequest.setFleetId"); this.fleetId = fleetId; }

    public String getAccountNumber() {
        LOGGER.log(Level.FINE, "→ TelematicsLookupRequest.getAccountNumber"); return accountNumber; }
    public void setAccountNumber(String accountNumber) {
        LOGGER.log(Level.FINE, "→ TelematicsLookupRequest.setAccountNumber"); this.accountNumber = accountNumber; }

    public Integer getActiveVehiclesCount() {
        LOGGER.log(Level.FINE, "→ TelematicsLookupRequest.getActiveVehiclesCount"); return activeVehiclesCount; }
    public void setActiveVehiclesCount(Integer activeVehiclesCount) {
        LOGGER.log(Level.FINE, "→ TelematicsLookupRequest.setActiveVehiclesCount"); this.activeVehiclesCount = activeVehiclesCount; }
}
