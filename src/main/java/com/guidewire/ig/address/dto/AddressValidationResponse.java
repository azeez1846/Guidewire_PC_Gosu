package com.guidewire.ig.address.dto;

public class AddressValidationResponse {
    private String transactionId;
    private String status; // SUCCESS, ADDRESS_NOT_FOUND, TIMEOUT
    private AddressSpecs addressSpecs;
    private String carrierRoute;
    private Boolean isDeliverable;
    private String standardizationStatus; // USPS_STANDARDIZED, MATCHED_EXACT, MATCHED_APPROXIMATE
    private String gatewayMetadata;

    public AddressValidationResponse() {}

    public AddressValidationResponse(String transactionId, String status, AddressSpecs addressSpecs, String carrierRoute, Boolean isDeliverable, String standardizationStatus, String gatewayMetadata) {
        this.transactionId = transactionId;
        this.status = status;
        this.addressSpecs = addressSpecs;
        this.carrierRoute = carrierRoute;
        this.isDeliverable = isDeliverable;
        this.standardizationStatus = standardizationStatus;
        this.gatewayMetadata = gatewayMetadata;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public AddressSpecs getAddressSpecs() { return addressSpecs; }
    public void setAddressSpecs(AddressSpecs addressSpecs) { this.addressSpecs = addressSpecs; }

    public String getCarrierRoute() { return carrierRoute; }
    public void setCarrierRoute(String carrierRoute) { this.carrierRoute = carrierRoute; }

    public Boolean getIsDeliverable() { return isDeliverable; }
    public void setIsDeliverable(Boolean isDeliverable) { this.isDeliverable = isDeliverable; }

    public String getStandardizationStatus() { return standardizationStatus; }
    public void setStandardizationStatus(String standardizationStatus) { this.standardizationStatus = standardizationStatus; }

    public String getGatewayMetadata() { return gatewayMetadata; }
    public void setGatewayMetadata(String gatewayMetadata) { this.gatewayMetadata = gatewayMetadata; }
}
