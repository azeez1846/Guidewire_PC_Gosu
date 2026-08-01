package com.guidewire.pc.model;

import com.guidewire.pc.orm.KeyableBean;
import com.guidewire.pc.orm.GosuORMSession;
import java.math.BigDecimal;

public class RICession implements KeyableBean {
    private Long id;
    private String cessionNumber;
    private BigDecimal grossRiskExposure = BigDecimal.ZERO;
    private BigDecimal retainedExposure = BigDecimal.ZERO;
    private BigDecimal cededExposure = BigDecimal.ZERO;
    private BigDecimal grossWrittenPremium = BigDecimal.ZERO;
    private BigDecimal cededPremium = BigDecimal.ZERO;
    private boolean requiresFacultative = false;

    public RICession() {
        this.id = GosuORMSession.getInstance().nextID();
        this.cessionNumber = "CES-" + System.currentTimeMillis();
    }

    @Override
    public Long getID() { return id; }
    @Override
    public void setID(Long id) { this.id = id; }
    @Override
    public boolean isNew() { return id == null; }

    public String getCessionNumber() { return cessionNumber; }
    public void setCessionNumber(String cessionNumber) { this.cessionNumber = cessionNumber; }

    public BigDecimal getGrossRiskExposure() { return grossRiskExposure; }
    public void setGrossRiskExposure(BigDecimal grossRiskExposure) { this.grossRiskExposure = grossRiskExposure; }

    public BigDecimal getRetainedExposure() { return retainedExposure; }
    public void setRetainedExposure(BigDecimal retainedExposure) { this.retainedExposure = retainedExposure; }

    public BigDecimal getCededExposure() { return cededExposure; }
    public void setCededExposure(BigDecimal cededExposure) { this.cededExposure = cededExposure; }

    public BigDecimal getGrossWrittenPremium() { return grossWrittenPremium; }
    public void setGrossWrittenPremium(BigDecimal grossWrittenPremium) { this.grossWrittenPremium = grossWrittenPremium; }

    public BigDecimal getCededPremium() { return cededPremium; }
    public void setCededPremium(BigDecimal cededPremium) { this.cededPremium = cededPremium; }

    public boolean isRequiresFacultative() { return requiresFacultative; }
    public void setRequiresFacultative(boolean requiresFacultative) { this.requiresFacultative = requiresFacultative; }
}
