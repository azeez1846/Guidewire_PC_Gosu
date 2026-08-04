package com.guidewire.ig.address.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AddressValidationResponse {
    private static final Logger LOGGER = Logger.getLogger(AddressValidationResponse.class.getName());

    private String transactionId;
    private String status; // SUCCESS, ADDRESS_NOT_FOUND, TIMEOUT
    private AddressSpecs addressSpecs;
    private String carrierRoute;
    private Boolean isDeliverable;
    private String standardizationStatus; // USPS_STANDARDIZED, MATCHED_EXACT, MATCHED_APPROXIMATE
    private String gatewayMetadata;

    public AddressValidationResponse() {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.AddressValidationResponse");}

    public AddressValidationResponse(String transactionId, String status, AddressSpecs addressSpecs, String carrierRoute, Boolean isDeliverable, String standardizationStatus, String gatewayMetadata) {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.AddressValidationResponse");
        this.transactionId = transactionId;
        this.status = status;
        this.addressSpecs = addressSpecs;
        this.carrierRoute = carrierRoute;
        this.isDeliverable = isDeliverable;
        this.standardizationStatus = standardizationStatus;
        this.gatewayMetadata = gatewayMetadata;
    }

    public String getTransactionId() {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.getTransactionId"); return transactionId; }
    public void setTransactionId(String transactionId) {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.setTransactionId"); this.transactionId = transactionId; }

    public String getStatus() {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.getStatus"); return status; }
    public void setStatus(String status) {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.setStatus"); this.status = status; }

    public AddressSpecs getAddressSpecs() {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.getAddressSpecs"); return addressSpecs; }
    public void setAddressSpecs(AddressSpecs addressSpecs) {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.setAddressSpecs"); this.addressSpecs = addressSpecs; }

    public String getCarrierRoute() {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.getCarrierRoute"); return carrierRoute; }
    public void setCarrierRoute(String carrierRoute) {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.setCarrierRoute"); this.carrierRoute = carrierRoute; }

    public Boolean getIsDeliverable() {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.getIsDeliverable"); return isDeliverable; }
    public void setIsDeliverable(Boolean isDeliverable) {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.setIsDeliverable"); this.isDeliverable = isDeliverable; }

    public String getStandardizationStatus() {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.getStandardizationStatus"); return standardizationStatus; }
    public void setStandardizationStatus(String standardizationStatus) {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.setStandardizationStatus"); this.standardizationStatus = standardizationStatus; }

    public String getGatewayMetadata() {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.getGatewayMetadata"); return gatewayMetadata; }
    public void setGatewayMetadata(String gatewayMetadata) {
        LOGGER.log(Level.FINE, "→ AddressValidationResponse.setGatewayMetadata"); this.gatewayMetadata = gatewayMetadata; }
}
