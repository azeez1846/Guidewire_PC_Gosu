package com.guidewire.ig.address.dto;

public class AddressSpecs {
    private String rawAddress;
    private String standardizedAddressLine1;
    private String standardizedAddressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String postalCodePlus4;
    private String county;
    private Double latitude;
    private Double longitude;
    private String deliveryPointValidationDPV; // CONFIRMED_DELIVERABLE, UNCONFIRMED, INVALID

    public AddressSpecs() {}

    public AddressSpecs(String rawAddress, String standardizedAddressLine1, String standardizedAddressLine2, String city, String state, String postalCode, String postalCodePlus4, String county, Double latitude, Double longitude, String deliveryPointValidationDPV) {
        this.rawAddress = rawAddress;
        this.standardizedAddressLine1 = standardizedAddressLine1;
        this.standardizedAddressLine2 = standardizedAddressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.postalCodePlus4 = postalCodePlus4;
        this.county = county;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deliveryPointValidationDPV = deliveryPointValidationDPV;
    }

    public String getRawAddress() { return rawAddress; }
    public void setRawAddress(String rawAddress) { this.rawAddress = rawAddress; }

    public String getStandardizedAddressLine1() { return standardizedAddressLine1; }
    public void setStandardizedAddressLine1(String standardizedAddressLine1) { this.standardizedAddressLine1 = standardizedAddressLine1; }

    public String getStandardizedAddressLine2() { return standardizedAddressLine2; }
    public void setStandardizedAddressLine2(String standardizedAddressLine2) { this.standardizedAddressLine2 = standardizedAddressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPostalCodePlus4() { return postalCodePlus4; }
    public void setPostalCodePlus4(String postalCodePlus4) { this.postalCodePlus4 = postalCodePlus4; }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getDeliveryPointValidationDPV() { return deliveryPointValidationDPV; }
    public void setDeliveryPointValidationDPV(String deliveryPointValidationDPV) { this.deliveryPointValidationDPV = deliveryPointValidationDPV; }
}
