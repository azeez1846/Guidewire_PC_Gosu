package com.guidewire.pc.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CessionLedgerEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CessionLedgerEntry.class.getName());

    private String policyNumber;
    private String treatyNumber;
    private String reinsurerName;
    private BigDecimal grossWrittenPremium;
    private BigDecimal cededPremium;
    private BigDecimal cedingCommission;
    private BigDecimal netRetainedPremium;
    private Date transactionDate;

    public CessionLedgerEntry() {
        this.transactionDate = new Date();
        LOGGER.log(Level.FINE, "CessionLedgerEntry instantiated at {0}", this.transactionDate);
    }

    public CessionLedgerEntry(String policyNumber, String treatyNumber, String reinsurerName, BigDecimal grossWrittenPremium, BigDecimal cededPremium, BigDecimal cedingCommission, BigDecimal netRetainedPremium) {
        this();
        this.policyNumber = policyNumber;
        this.treatyNumber = treatyNumber;
        this.reinsurerName = reinsurerName;
        this.grossWrittenPremium = grossWrittenPremium;
        this.cededPremium = cededPremium;
        this.cedingCommission = cedingCommission;
        this.netRetainedPremium = netRetainedPremium;
    }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getTreatyNumber() { return treatyNumber; }
    public void setTreatyNumber(String treatyNumber) { this.treatyNumber = treatyNumber; }

    public String getReinsurerName() { return reinsurerName; }
    public void setReinsurerName(String reinsurerName) { this.reinsurerName = reinsurerName; }

    public BigDecimal getGrossWrittenPremium() { return grossWrittenPremium; }
    public void setGrossWrittenPremium(BigDecimal grossWrittenPremium) { this.grossWrittenPremium = grossWrittenPremium; }

    public BigDecimal getCededPremium() { return cededPremium; }
    public void setCededPremium(BigDecimal cededPremium) { this.cededPremium = cededPremium; }

    public BigDecimal getCedingCommission() { return cedingCommission; }
    public void setCedingCommission(BigDecimal cedingCommission) { this.cedingCommission = cedingCommission; }

    public BigDecimal getNetRetainedPremium() { return netRetainedPremium; }
    public void setNetRetainedPremium(BigDecimal netRetainedPremium) { this.netRetainedPremium = netRetainedPremium; }

    public Date getTransactionDate() { return transactionDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CessionLedgerEntry that)) return false;
        return java.util.Objects.equals(policyNumber, that.policyNumber) &&
                java.util.Objects.equals(treatyNumber, that.treatyNumber) &&
                java.util.Objects.equals(transactionDate, that.transactionDate);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(policyNumber, treatyNumber, transactionDate);
    }

    @Override
    public String toString() {
        return "CessionLedgerEntry{" +
                "policyNumber='" + policyNumber + '\'' +
                ", treatyNumber='" + treatyNumber + '\'' +
                ", ceded=" + cededPremium +
                ", retained=" + netRetainedPremium +
                '}';
    }
}
