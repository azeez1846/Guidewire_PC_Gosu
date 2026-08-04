package com.guidewire.ig.credit.dto;

public class CreditLookupRequest {
    private String accountHolderName;
    private String feinOrSsn;
    private String orgType;
    private String state;

    public CreditLookupRequest() {}

    public CreditLookupRequest(String accountHolderName, String feinOrSsn, String orgType, String state) {
        this.accountHolderName = accountHolderName;
        this.feinOrSsn = feinOrSsn;
        this.orgType = orgType;
        this.state = state;
    }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getFeinOrSsn() { return feinOrSsn; }
    public void setFeinOrSsn(String feinOrSsn) { this.feinOrSsn = feinOrSsn; }

    public String getOrgType() { return orgType; }
    public void setOrgType(String orgType) { this.orgType = orgType; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
