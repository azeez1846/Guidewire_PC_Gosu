package com.guidewire.ig.address.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AddressLookupRequest {
    private static final Logger LOGGER = Logger.getLogger(AddressLookupRequest.class.getName());

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public AddressLookupRequest() {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.AddressLookupRequest");}

    public AddressLookupRequest(String addressLine1, String addressLine2, String city, String state, String postalCode, String country) {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.AddressLookupRequest");
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getAddressLine1() {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.getAddressLine1"); return addressLine1; }
    public void setAddressLine1(String addressLine1) {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.setAddressLine1"); this.addressLine1 = addressLine1; }

    public String getAddressLine2() {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.getAddressLine2"); return addressLine2; }
    public void setAddressLine2(String addressLine2) {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.setAddressLine2"); this.addressLine2 = addressLine2; }

    public String getCity() {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.getCity"); return city; }
    public void setCity(String city) {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.setCity"); this.city = city; }

    public String getState() {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.getState"); return state; }
    public void setState(String state) {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.setState"); this.state = state; }

    public String getPostalCode() {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.getPostalCode"); return postalCode; }
    public void setPostalCode(String postalCode) {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.setPostalCode"); this.postalCode = postalCode; }

    public String getCountry() {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.getCountry"); return country; }
    public void setCountry(String country) {
        LOGGER.log(Level.FINE, "→ AddressLookupRequest.setCountry"); this.country = country; }
}
