package com.guidewire.pc.model;

public class Account {
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
        this.accountStatus = "Active";
        this.accountHolderType = "Company";
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getAccountHolderType() { return accountHolderType; }
    public void setAccountHolderType(String accountHolderType) { this.accountHolderType = accountHolderType; }

    public String getFein() { return fein; }
    public void setFein(String fein) { this.fein = fein; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getProducerCode() { return producerCode; }
    public void setProducerCode(String producerCode) { this.producerCode = producerCode; }

    public String getIndustryCode() { return industryCode; }
    public void setIndustryCode(String industryCode) { this.industryCode = industryCode; }

    public String getOrgType() { return orgType; }
    public void setOrgType(String orgType) { this.orgType = orgType; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1);
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) sb.append(", ").append(addressLine2);
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(", ").append(state);
        if (postalCode != null) sb.append(" ").append(postalCode);
        return sb.toString();
    }
}
