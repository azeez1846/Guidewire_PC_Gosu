package com.guidewire.ig.vehicledetails.dto;

import java.math.BigDecimal;
import java.util.logging.Logger;
import java.util.logging.Level;

public class VehicleDetailsResponse {
    private static final Logger LOGGER = Logger.getLogger(VehicleDetailsResponse.class.getName());

    private String transactionId;
    private String status; // SUCCESS, VENDOR_TIMEOUT, REJECTED
    private VehicleSpecs vehicleSpecs;
    private MVRRecord mvrRecord;
    private BigDecimal estimatedActualCashValue;
    private Double recommendedTierDiscountSurchargePct;
    private String underwritingRecommendation; // PREFERRED, STANDARD, SUBSTANDARD_SURCHARGE, REFER_TO_UW
    private String gatewayMetadata;

    public VehicleDetailsResponse() {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.VehicleDetailsResponse");}

    public VehicleDetailsResponse(String transactionId, String status, VehicleSpecs vehicleSpecs, MVRRecord mvrRecord, BigDecimal estimatedActualCashValue, Double recommendedTierDiscountSurchargePct, String underwritingRecommendation, String gatewayMetadata) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.VehicleDetailsResponse");
        this.transactionId = transactionId;
        this.status = status;
        this.vehicleSpecs = vehicleSpecs;
        this.mvrRecord = mvrRecord;
        this.estimatedActualCashValue = estimatedActualCashValue;
        this.recommendedTierDiscountSurchargePct = recommendedTierDiscountSurchargePct;
        this.underwritingRecommendation = underwritingRecommendation;
        this.gatewayMetadata = gatewayMetadata;
    }

    public String getTransactionId() {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.getTransactionId"); return transactionId; }
    public void setTransactionId(String transactionId) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.setTransactionId"); this.transactionId = transactionId; }

    public String getStatus() {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.getStatus"); return status; }
    public void setStatus(String status) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.setStatus"); this.status = status; }

    public VehicleSpecs getVehicleSpecs() {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.getVehicleSpecs"); return vehicleSpecs; }
    public void setVehicleSpecs(VehicleSpecs vehicleSpecs) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.setVehicleSpecs"); this.vehicleSpecs = vehicleSpecs; }

    public MVRRecord getMvrRecord() {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.getMvrRecord"); return mvrRecord; }
    public void setMvrRecord(MVRRecord mvrRecord) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.setMvrRecord"); this.mvrRecord = mvrRecord; }

    public BigDecimal getEstimatedActualCashValue() {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.getEstimatedActualCashValue"); return estimatedActualCashValue; }
    public void setEstimatedActualCashValue(BigDecimal estimatedActualCashValue) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.setEstimatedActualCashValue"); this.estimatedActualCashValue = estimatedActualCashValue; }

    public Double getRecommendedTierDiscountSurchargePct() {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.getRecommendedTierDiscountSurchargePct"); return recommendedTierDiscountSurchargePct; }
    public void setRecommendedTierDiscountSurchargePct(Double recommendedTierDiscountSurchargePct) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.setRecommendedTierDiscountSurchargePct"); this.recommendedTierDiscountSurchargePct = recommendedTierDiscountSurchargePct; }

    public String getUnderwritingRecommendation() {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.getUnderwritingRecommendation"); return underwritingRecommendation; }
    public void setUnderwritingRecommendation(String underwritingRecommendation) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.setUnderwritingRecommendation"); this.underwritingRecommendation = underwritingRecommendation; }

    public String getGatewayMetadata() {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.getGatewayMetadata"); return gatewayMetadata; }
    public void setGatewayMetadata(String gatewayMetadata) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsResponse.setGatewayMetadata"); this.gatewayMetadata = gatewayMetadata; }
}
