package com.guidewire.pc.model;

import java.math.BigDecimal;

public class PolicyPeriod {
    private String jobNumber;
    private String policyNumber;
    private String productCode; // PersonalAuto, CommercialAuto, CommercialProperty, GeneralLiability
    private String status; // Draft, Quoted, Bound, Issued
    private String effectiveDate;
    private String expirationDate;
    private int termMonths = 12;
    private String baseState = "CA";
    private String producerCode;
    private Account account;

    // Coverage details
    private String bodilyInjuryLimit = "$500k/$500k";
    private String propertyDamageLimit = "$250k";
    private String comprehensiveDeductible = "$500";
    private String collisionDeductible = "$1000";

    // Financials
    private BigDecimal basePremium = BigDecimal.ZERO;
    private BigDecimal taxesAndFees = BigDecimal.ZERO;
    private BigDecimal totalPremium = BigDecimal.ZERO;
    private String createTime;

    public PolicyPeriod() {
        this.status = "Draft";
        this.productCode = "CommercialAuto";
    }

    public String getJobNumber() { return jobNumber; }
    public void setJobNumber(String jobNumber) { this.jobNumber = jobNumber; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }

    public String getBaseState() { return baseState; }
    public void setBaseState(String baseState) { this.baseState = baseState; }

    public String getProducerCode() { return producerCode; }
    public void setProducerCode(String producerCode) { this.producerCode = producerCode; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public String getBodilyInjuryLimit() { return bodilyInjuryLimit; }
    public void setBodilyInjuryLimit(String bodilyInjuryLimit) { this.bodilyInjuryLimit = bodilyInjuryLimit; }

    public String getPropertyDamageLimit() { return propertyDamageLimit; }
    public void setPropertyDamageLimit(String propertyDamageLimit) { this.propertyDamageLimit = propertyDamageLimit; }

    public String getComprehensiveDeductible() { return comprehensiveDeductible; }
    public void setComprehensiveDeductible(String comprehensiveDeductible) { this.comprehensiveDeductible = comprehensiveDeductible; }

    public String getCollisionDeductible() { return collisionDeductible; }
    public void setCollisionDeductible(String collisionDeductible) { this.collisionDeductible = collisionDeductible; }

    public BigDecimal getBasePremium() { return basePremium; }
    public void setBasePremium(BigDecimal basePremium) { this.basePremium = basePremium; }

    public BigDecimal getTaxesAndFees() { return taxesAndFees; }
    public void setTaxesAndFees(BigDecimal taxesAndFees) { this.taxesAndFees = taxesAndFees; }

    public BigDecimal getTotalPremium() { return totalPremium; }
    public void setTotalPremium(BigDecimal totalPremium) { this.totalPremium = totalPremium; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getFormattedStatus() {
        if ("Issued".equalsIgnoreCase(status)) return "In Force (Issued)";
        if ("Bound".equalsIgnoreCase(status)) return "Bound";
        if ("Quoted".equalsIgnoreCase(status)) return "Quoted";
        return "Draft";
    }

    public BigDecimal calculatePremium() {
        double rate = 500.0;
        if ("PersonalAuto".equalsIgnoreCase(productCode)) rate = 650.0;
        else if ("CommercialAuto".equalsIgnoreCase(productCode)) rate = 1250.0;
        else if ("CommercialProperty".equalsIgnoreCase(productCode)) rate = 2100.0;
        else if ("GeneralLiability".equalsIgnoreCase(productCode)) rate = 1800.0;

        if (termMonths == 12) rate *= 1.9;

        if ("$500k/$500k".equals(bodilyInjuryLimit)) rate += 250.0;
        else if ("$1M/$1M".equals(bodilyInjuryLimit)) rate += 500.0;

        if ("$250k".equals(propertyDamageLimit)) rate += 150.0;
        else if ("$500k".equals(propertyDamageLimit)) rate += 300.0;

        double tax = rate * 0.08;
        this.basePremium = BigDecimal.valueOf(rate).setScale(2, java.math.RoundingMode.HALF_UP);
        this.taxesAndFees = BigDecimal.valueOf(tax).setScale(2, java.math.RoundingMode.HALF_UP);
        this.totalPremium = this.basePremium.add(this.taxesAndFees);

        return this.totalPremium;
    }
}
