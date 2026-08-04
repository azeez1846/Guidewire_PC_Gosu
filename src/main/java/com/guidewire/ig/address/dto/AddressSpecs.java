package com.guidewire.ig.address.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AddressSpecs {
    private static final Logger LOGGER = Logger.getLogger(AddressSpecs.class.getName());

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

    public AddressSpecs() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.AddressSpecs");}

    public AddressSpecs(String rawAddress, String standardizedAddressLine1, String standardizedAddressLine2, String city, String state, String postalCode, String postalCodePlus4, String county, Double latitude, Double longitude, String deliveryPointValidationDPV) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.AddressSpecs");
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

    public String getRawAddress() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getRawAddress"); return rawAddress; }
    public void setRawAddress(String rawAddress) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setRawAddress"); this.rawAddress = rawAddress; }

    public String getStandardizedAddressLine1() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getStandardizedAddressLine1"); return standardizedAddressLine1; }
    public void setStandardizedAddressLine1(String standardizedAddressLine1) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setStandardizedAddressLine1"); this.standardizedAddressLine1 = standardizedAddressLine1; }

    public String getStandardizedAddressLine2() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getStandardizedAddressLine2"); return standardizedAddressLine2; }
    public void setStandardizedAddressLine2(String standardizedAddressLine2) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setStandardizedAddressLine2"); this.standardizedAddressLine2 = standardizedAddressLine2; }

    public String getCity() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getCity"); return city; }
    public void setCity(String city) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setCity"); this.city = city; }

    public String getState() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getState"); return state; }
    public void setState(String state) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setState"); this.state = state; }

    public String getPostalCode() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getPostalCode"); return postalCode; }
    public void setPostalCode(String postalCode) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setPostalCode"); this.postalCode = postalCode; }

    public String getPostalCodePlus4() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getPostalCodePlus4"); return postalCodePlus4; }
    public void setPostalCodePlus4(String postalCodePlus4) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setPostalCodePlus4"); this.postalCodePlus4 = postalCodePlus4; }

    public String getCounty() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getCounty"); return county; }
    public void setCounty(String county) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setCounty"); this.county = county; }

    public Double getLatitude() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getLatitude"); return latitude; }
    public void setLatitude(Double latitude) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setLatitude"); this.latitude = latitude; }

    public Double getLongitude() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getLongitude"); return longitude; }
    public void setLongitude(Double longitude) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setLongitude"); this.longitude = longitude; }

    public String getDeliveryPointValidationDPV() {
        LOGGER.log(Level.FINE, "→ AddressSpecs.getDeliveryPointValidationDPV"); return deliveryPointValidationDPV; }
    public void setDeliveryPointValidationDPV(String deliveryPointValidationDPV) {
        LOGGER.log(Level.FINE, "→ AddressSpecs.setDeliveryPointValidationDPV"); this.deliveryPointValidationDPV = deliveryPointValidationDPV; }
}
