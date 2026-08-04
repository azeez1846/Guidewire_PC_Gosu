package com.guidewire.ig.credit.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CreditLookupRequest {
    private static final Logger LOGGER = Logger.getLogger(CreditLookupRequest.class.getName());

    private String accountHolderName;
    private String feinOrSsn;
    private String orgType;
    private String state;

    public CreditLookupRequest() {
        LOGGER.log(Level.FINE, "→ CreditLookupRequest.CreditLookupRequest");}

    public CreditLookupRequest(String accountHolderName, String feinOrSsn, String orgType, String state) {
        LOGGER.log(Level.FINE, "→ CreditLookupRequest.CreditLookupRequest");
        this.accountHolderName = accountHolderName;
        this.feinOrSsn = feinOrSsn;
        this.orgType = orgType;
        this.state = state;
    }

    public String getAccountHolderName() {
        LOGGER.log(Level.FINE, "→ CreditLookupRequest.getAccountHolderName"); return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) {
        LOGGER.log(Level.FINE, "→ CreditLookupRequest.setAccountHolderName"); this.accountHolderName = accountHolderName; }

    public String getFeinOrSsn() {
        LOGGER.log(Level.FINE, "→ CreditLookupRequest.getFeinOrSsn"); return feinOrSsn; }
    public void setFeinOrSsn(String feinOrSsn) {
        LOGGER.log(Level.FINE, "→ CreditLookupRequest.setFeinOrSsn"); this.feinOrSsn = feinOrSsn; }

    public String getOrgType() {
        LOGGER.log(Level.FINE, "→ CreditLookupRequest.getOrgType"); return orgType; }
    public void setOrgType(String orgType) {
        LOGGER.log(Level.FINE, "→ CreditLookupRequest.setOrgType"); this.orgType = orgType; }

    public String getState() {
        LOGGER.log(Level.FINE, "→ CreditLookupRequest.getState"); return state; }
    public void setState(String state) {
        LOGGER.log(Level.FINE, "→ CreditLookupRequest.setState"); this.state = state; }
}
