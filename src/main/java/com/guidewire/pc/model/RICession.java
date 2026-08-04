package com.guidewire.pc.model;

import com.guidewire.pc.orm.KeyableBean;
import com.guidewire.pc.orm.GosuORMSession;
import java.math.BigDecimal;
import java.util.logging.Logger;
import java.util.logging.Level;

public class RICession implements KeyableBean {
    private static final Logger LOGGER = Logger.getLogger(RICession.class.getName());

    private Long id;
    private String cessionNumber;
    private BigDecimal grossRiskExposure = BigDecimal.ZERO;
    private BigDecimal retainedExposure = BigDecimal.ZERO;
    private BigDecimal cededExposure = BigDecimal.ZERO;
    private BigDecimal grossWrittenPremium = BigDecimal.ZERO;
    private BigDecimal cededPremium = BigDecimal.ZERO;
    private BigDecimal cedingCommission = BigDecimal.ZERO;
    private boolean requiresFacultative = false;

    public RICession() {
        LOGGER.log(Level.FINE, "→ RICession.RICession");
        this.id = GosuORMSession.getInstance().nextID();
        this.cessionNumber = "CES-" + System.currentTimeMillis();
    }

    @Override
    public Long getID() {
        LOGGER.log(Level.FINE, "→ RICession.getID"); return id; }
    @Override
    public void setID(Long id) {
        LOGGER.log(Level.FINE, "→ RICession.setID"); this.id = id; }
    @Override
    public boolean isNew() {
        LOGGER.log(Level.FINE, "→ RICession.isNew"); return id == null; }

    public String getCessionNumber() {
        LOGGER.log(Level.FINE, "→ RICession.getCessionNumber"); return cessionNumber; }
    public void setCessionNumber(String cessionNumber) {
        LOGGER.log(Level.FINE, "→ RICession.setCessionNumber"); this.cessionNumber = cessionNumber; }

    public BigDecimal getGrossRiskExposure() {
        LOGGER.log(Level.FINE, "→ RICession.getGrossRiskExposure"); return grossRiskExposure; }
    public void setGrossRiskExposure(BigDecimal grossRiskExposure) {
        LOGGER.log(Level.FINE, "→ RICession.setGrossRiskExposure"); this.grossRiskExposure = grossRiskExposure; }

    public BigDecimal getRetainedExposure() {
        LOGGER.log(Level.FINE, "→ RICession.getRetainedExposure"); return retainedExposure; }
    public void setRetainedExposure(BigDecimal retainedExposure) {
        LOGGER.log(Level.FINE, "→ RICession.setRetainedExposure"); this.retainedExposure = retainedExposure; }

    public BigDecimal getCededExposure() {
        LOGGER.log(Level.FINE, "→ RICession.getCededExposure"); return cededExposure; }
    public void setCededExposure(BigDecimal cededExposure) {
        LOGGER.log(Level.FINE, "→ RICession.setCededExposure"); this.cededExposure = cededExposure; }

    public BigDecimal getGrossWrittenPremium() {
        LOGGER.log(Level.FINE, "→ RICession.getGrossWrittenPremium"); return grossWrittenPremium; }
    public void setGrossWrittenPremium(BigDecimal grossWrittenPremium) {
        LOGGER.log(Level.FINE, "→ RICession.setGrossWrittenPremium"); this.grossWrittenPremium = grossWrittenPremium; }

    public BigDecimal getCededPremium() {
        LOGGER.log(Level.FINE, "→ RICession.getCededPremium"); return cededPremium; }
    public void setCededPremium(BigDecimal cededPremium) {
        LOGGER.log(Level.FINE, "→ RICession.setCededPremium"); this.cededPremium = cededPremium; }

    public BigDecimal getCedingCommission() {
        LOGGER.log(Level.FINE, "→ RICession.getCedingCommission"); return cedingCommission; }
    public void setCedingCommission(BigDecimal cedingCommission) {
        LOGGER.log(Level.FINE, "→ RICession.setCedingCommission"); this.cedingCommission = cedingCommission; }

    public boolean isRequiresFacultative() {
        LOGGER.log(Level.FINE, "→ RICession.isRequiresFacultative"); return requiresFacultative; }
    public void setRequiresFacultative(boolean requiresFacultative) {
        LOGGER.log(Level.FINE, "→ RICession.setRequiresFacultative"); this.requiresFacultative = requiresFacultative; }
}
