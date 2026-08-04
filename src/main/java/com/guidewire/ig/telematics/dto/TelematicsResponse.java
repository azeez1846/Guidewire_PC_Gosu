package com.guidewire.ig.telematics.dto;

public class TelematicsResponse {
    private String transactionId;
    private String status;
    private String fleetId;
    private FleetSafetyScore safetyScore;
    private String tierRecommendation; // OPTIMAL_FLEET_DISCOUNT, STANDARD_FLEET, HIGH_RISK_SURCHARGE
    private String gatewayMetadata;

    public TelematicsResponse() {}

    public TelematicsResponse(String transactionId, String status, String fleetId, FleetSafetyScore safetyScore, String tierRecommendation, String gatewayMetadata) {
        this.transactionId = transactionId;
        this.status = status;
        this.fleetId = fleetId;
        this.safetyScore = safetyScore;
        this.tierRecommendation = tierRecommendation;
        this.gatewayMetadata = gatewayMetadata;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFleetId() { return fleetId; }
    public void setFleetId(String fleetId) { this.fleetId = fleetId; }

    public FleetSafetyScore getSafetyScore() { return safetyScore; }
    public void setSafetyScore(FleetSafetyScore safetyScore) { this.safetyScore = safetyScore; }

    public String getTierRecommendation() { return tierRecommendation; }
    public void setTierRecommendation(String tierRecommendation) { this.tierRecommendation = tierRecommendation; }

    public String getGatewayMetadata() { return gatewayMetadata; }
    public void setGatewayMetadata(String gatewayMetadata) { this.gatewayMetadata = gatewayMetadata; }
}
