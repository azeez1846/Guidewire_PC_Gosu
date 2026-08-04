package com.guidewire.ig.telematics.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TelematicsResponse {
    private static final Logger LOGGER = Logger.getLogger(TelematicsResponse.class.getName());

    private String transactionId;
    private String status;
    private String fleetId;
    private FleetSafetyScore safetyScore;
    private String tierRecommendation; // OPTIMAL_FLEET_DISCOUNT, STANDARD_FLEET, HIGH_RISK_SURCHARGE
    private String gatewayMetadata;

    public TelematicsResponse() {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.TelematicsResponse");}

    public TelematicsResponse(String transactionId, String status, String fleetId, FleetSafetyScore safetyScore, String tierRecommendation, String gatewayMetadata) {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.TelematicsResponse");
        this.transactionId = transactionId;
        this.status = status;
        this.fleetId = fleetId;
        this.safetyScore = safetyScore;
        this.tierRecommendation = tierRecommendation;
        this.gatewayMetadata = gatewayMetadata;
    }

    public String getTransactionId() {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.getTransactionId"); return transactionId; }
    public void setTransactionId(String transactionId) {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.setTransactionId"); this.transactionId = transactionId; }

    public String getStatus() {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.getStatus"); return status; }
    public void setStatus(String status) {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.setStatus"); this.status = status; }

    public String getFleetId() {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.getFleetId"); return fleetId; }
    public void setFleetId(String fleetId) {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.setFleetId"); this.fleetId = fleetId; }

    public FleetSafetyScore getSafetyScore() {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.getSafetyScore"); return safetyScore; }
    public void setSafetyScore(FleetSafetyScore safetyScore) {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.setSafetyScore"); this.safetyScore = safetyScore; }

    public String getTierRecommendation() {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.getTierRecommendation"); return tierRecommendation; }
    public void setTierRecommendation(String tierRecommendation) {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.setTierRecommendation"); this.tierRecommendation = tierRecommendation; }

    public String getGatewayMetadata() {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.getGatewayMetadata"); return gatewayMetadata; }
    public void setGatewayMetadata(String gatewayMetadata) {
        LOGGER.log(Level.FINE, "→ TelematicsResponse.setGatewayMetadata"); this.gatewayMetadata = gatewayMetadata; }
}
