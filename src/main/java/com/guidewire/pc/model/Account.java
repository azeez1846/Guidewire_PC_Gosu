package com.guidewire.pc.model;

import com.guidewire.pc.constants.PCConstants;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Account {
    private static final Logger LOGGER = Logger.getLogger(Account.class.getName());

    private String accountNumber;
    private String accountHolderName;
    private String accountHolderType; // Individual or Company
    private String fein;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String phone;
    private String email;
    private String accountStatus; // Active, Pending, Suspended
    private String producerCode;
    private String industryCode;
    private String orgType;
    private String createTime;

    public Account() {
        LOGGER.log(Level.FINE, "→ Account.Account");
        this.accountStatus = PCConstants.ACCOUNT_STATUS_ACTIVE;
        this.accountHolderType = "Company";
    }

    public String getAccountNumber() {
        LOGGER.log(Level.FINE, "→ Account.getAccountNumber"); return accountNumber; }
    public void setAccountNumber(String accountNumber) {
        LOGGER.log(Level.FINE, "→ Account.setAccountNumber"); this.accountNumber = accountNumber; }

    public String getAccountHolderName() {
        LOGGER.log(Level.FINE, "→ Account.getAccountHolderName"); return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) {
        LOGGER.log(Level.FINE, "→ Account.setAccountHolderName"); this.accountHolderName = accountHolderName; }

    public String getAccountHolderType() {
        LOGGER.log(Level.FINE, "→ Account.getAccountHolderType"); return accountHolderType; }
    public void setAccountHolderType(String accountHolderType) {
        LOGGER.log(Level.FINE, "→ Account.setAccountHolderType"); this.accountHolderType = accountHolderType; }

    public String getFein() {
        LOGGER.log(Level.FINE, "→ Account.getFein"); return fein; }
    public void setFein(String fein) {
        LOGGER.log(Level.FINE, "→ Account.setFein"); this.fein = fein; }
    public String getMaskedFein() {
        LOGGER.log(Level.FINE, "→ Account.getMaskedFein"); return com.guidewire.pc.security.SecurityUtils.maskFein(fein); }

    public String getAddressLine1() {
        LOGGER.log(Level.FINE, "→ Account.getAddressLine1"); return addressLine1; }
    public void setAddressLine1(String addressLine1) {
        LOGGER.log(Level.FINE, "→ Account.setAddressLine1"); this.addressLine1 = addressLine1; }

    public String getAddressLine2() {
        LOGGER.log(Level.FINE, "→ Account.getAddressLine2"); return addressLine2; }
    public void setAddressLine2(String addressLine2) {
        LOGGER.log(Level.FINE, "→ Account.setAddressLine2"); this.addressLine2 = addressLine2; }

    public String getCity() {
        LOGGER.log(Level.FINE, "→ Account.getCity"); return city; }
    public void setCity(String city) {
        LOGGER.log(Level.FINE, "→ Account.setCity"); this.city = city; }

    public String getState() {
        LOGGER.log(Level.FINE, "→ Account.getState"); return state; }
    public void setState(String state) {
        LOGGER.log(Level.FINE, "→ Account.setState"); this.state = state; }

    public String getPostalCode() {
        LOGGER.log(Level.FINE, "→ Account.getPostalCode"); return postalCode; }
    public void setPostalCode(String postalCode) {
        LOGGER.log(Level.FINE, "→ Account.setPostalCode"); this.postalCode = postalCode; }

    public String getPhone() {
        LOGGER.log(Level.FINE, "→ Account.getPhone"); return phone; }
    public void setPhone(String phone) {
        LOGGER.log(Level.FINE, "→ Account.setPhone"); this.phone = phone; }

    public String getEmail() {
        LOGGER.log(Level.FINE, "→ Account.getEmail"); return email; }
    public void setEmail(String email) {
        LOGGER.log(Level.FINE, "→ Account.setEmail"); this.email = email; }

    public String getAccountStatus() {
        LOGGER.log(Level.FINE, "→ Account.getAccountStatus"); return accountStatus; }
    public void setAccountStatus(String accountStatus) {
        LOGGER.log(Level.FINE, "→ Account.setAccountStatus"); this.accountStatus = accountStatus; }

    public String getProducerCode() {
        LOGGER.log(Level.FINE, "→ Account.getProducerCode"); return producerCode; }
    public void setProducerCode(String producerCode) {
        LOGGER.log(Level.FINE, "→ Account.setProducerCode"); this.producerCode = producerCode; }

    public String getIndustryCode() {
        LOGGER.log(Level.FINE, "→ Account.getIndustryCode"); return industryCode; }
    public void setIndustryCode(String industryCode) {
        LOGGER.log(Level.FINE, "→ Account.setIndustryCode"); this.industryCode = industryCode; }

    public String getOrgType() {
        LOGGER.log(Level.FINE, "→ Account.getOrgType"); return orgType; }
    public void setOrgType(String orgType) {
        LOGGER.log(Level.FINE, "→ Account.setOrgType"); this.orgType = orgType; }

    public String getCreateTime() {
        LOGGER.log(Level.FINE, "→ Account.getCreateTime"); return createTime; }
    public void setCreateTime(String createTime) {
        LOGGER.log(Level.FINE, "→ Account.setCreateTime"); this.createTime = createTime; }

    public String getFormattedAddress() {
        LOGGER.log(Level.FINE, "→ Account.getFormattedAddress");
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1);
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) sb.append(", ").append(addressLine2);
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(", ").append(state);
        if (postalCode != null) sb.append(" ").append(postalCode);
        return sb.toString();
    }
}
