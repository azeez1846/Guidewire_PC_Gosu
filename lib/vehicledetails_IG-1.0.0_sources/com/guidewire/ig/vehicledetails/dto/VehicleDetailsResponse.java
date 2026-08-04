package com.guidewire.ig.vehicledetails.dto;

import java.math.BigDecimal;

public class VehicleDetailsResponse {
    private String transactionId;
    private String status; // SUCCESS, VENDOR_TIMEOUT, REJECTED
    private VehicleSpecs vehicleSpecs;
    private MVRRecord mvrRecord;
    private BigDecimal estimatedActualCashValue;
    private Double recommendedTierDiscountSurchargePct;
    private String underwritingRecommendation; // PREFERRED, STANDARD, SUBSTANDARD_SURCHARGE, REFER_TO_UW
    private String gatewayMetadata;

    public VehicleDetailsResponse() {}

    public VehicleDetailsResponse(String transactionId, String status, VehicleSpecs vehicleSpecs, MVRRecord mvrRecord, BigDecimal estimatedActualCashValue, Double recommendedTierDiscountSurchargePct, String underwritingRecommendation, String gatewayMetadata) {
        this.transactionId = transactionId;
        this.status = status;
        this.vehicleSpecs = vehicleSpecs;
        this.mvrRecord = mvrRecord;
        this.estimatedActualCashValue = estimatedActualCashValue;
        this.recommendedTierDiscountSurchargePct = recommendedTierDiscountSurchargePct;
        this.underwritingRecommendation = underwritingRecommendation;
        this.gatewayMetadata = gatewayMetadata;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public VehicleSpecs getVehicleSpecs() { return vehicleSpecs; }
    public void setVehicleSpecs(VehicleSpecs vehicleSpecs) { this.vehicleSpecs = vehicleSpecs; }

    public MVRRecord getMvrRecord() { return mvrRecord; }
    public void setMvrRecord(MVRRecord mvrRecord) { this.mvrRecord = mvrRecord; }

    public BigDecimal getEstimatedActualCashValue() { return estimatedActualCashValue; }
    public void setEstimatedActualCashValue(BigDecimal estimatedActualCashValue) { this.estimatedActualCashValue = estimatedActualCashValue; }

    public Double getRecommendedTierDiscountSurchargePct() { return recommendedTierDiscountSurchargePct; }
    public void setRecommendedTierDiscountSurchargePct(Double recommendedTierDiscountSurchargePct) { this.recommendedTierDiscountSurchargePct = recommendedTierDiscountSurchargePct; }

    public String getUnderwritingRecommendation() { return underwritingRecommendation; }
    public void setUnderwritingRecommendation(String underwritingRecommendation) { this.underwritingRecommendation = underwritingRecommendation; }

    public String getGatewayMetadata() { return gatewayMetadata; }
    public void setGatewayMetadata(String gatewayMetadata) { this.gatewayMetadata = gatewayMetadata; }
}
