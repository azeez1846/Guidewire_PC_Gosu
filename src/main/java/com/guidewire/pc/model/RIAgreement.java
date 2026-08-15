package com.guidewire.pc.model;

import com.guidewire.pc.orm.KeyableBean;
import com.guidewire.pc.orm.GosuORMSession;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RIAgreement implements KeyableBean {
    private static final Logger LOGGER = Logger.getLogger(RIAgreement.class.getName());

    private Long id;
    private String agreementNumber;
    private String agreementName;
    private String agreementType = "QuotaShare";
    private BigDecimal grossRetentionLimit = new BigDecimal("1000000.00");
    private BigDecimal attachmentPoint = BigDecimal.ZERO;
    private BigDecimal cedingPercentage = new BigDecimal("20.00"); // 20%
    private BigDecimal cedingCommissionPct = new BigDecimal("20.00"); // 20% ceding commission
    private String effectiveDate = "2026-01-01";
    private String expirationDate = "2027-01-01";

    public RIAgreement() {
        this.id = GosuORMSession.getInstance().nextID();
        LOGGER.log(Level.FINE, "RIAgreement created: ID={0}", this.id);
    }

    public RIAgreement(String agreementNumber, String agreementName, String agreementType, BigDecimal grossRetentionLimit, BigDecimal cedingPercentage) {
        this();
        this.agreementNumber = agreementNumber;
        this.agreementName = agreementName;
        this.agreementType = agreementType != null ? agreementType : "QuotaShare";
        this.grossRetentionLimit = grossRetentionLimit != null ? grossRetentionLimit : new BigDecimal("1000000.00");
        this.cedingPercentage = cedingPercentage != null ? cedingPercentage : new BigDecimal("20.00");
    }

    @Override
    public Long getID() { return id; }
    @Override
    public void setID(Long id) { this.id = id; }
    @Override
    public boolean isNew() { return id == null; }

    public String getAgreementNumber() { return agreementNumber; }
    public void setAgreementNumber(String agreementNumber) { this.agreementNumber = agreementNumber; }

    public String getAgreementName() { return agreementName; }
    public void setAgreementName(String agreementName) { this.agreementName = agreementName; }

    public String getAgreementType() { return agreementType; }
    public void setAgreementType(String agreementType) { this.agreementType = agreementType; }

    public BigDecimal getGrossRetentionLimit() { return grossRetentionLimit; }
    public void setGrossRetentionLimit(BigDecimal grossRetentionLimit) { this.grossRetentionLimit = grossRetentionLimit; }

    public BigDecimal getAttachmentPoint() { return attachmentPoint; }
    public void setAttachmentPoint(BigDecimal attachmentPoint) { this.attachmentPoint = attachmentPoint; }

    public BigDecimal getCedingPercentage() { return cedingPercentage; }
    public void setCedingPercentage(BigDecimal cedingPercentage) { this.cedingPercentage = cedingPercentage; }

    public BigDecimal getCedingCommissionPct() { return cedingCommissionPct; }
    public void setCedingCommissionPct(BigDecimal cedingCommissionPct) { this.cedingCommissionPct = cedingCommissionPct; }

    public String getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
}
