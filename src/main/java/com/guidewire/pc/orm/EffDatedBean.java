package com.guidewire.pc.orm;

import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

public abstract class EffDatedBean implements KeyableBean {
    private static final Logger LOGGER = Logger.getLogger(EffDatedBean.class.getName());

    private Long id;
    private FixedId<?> fixedId;
    private EffDatedBranch branch;
    private Date effectiveDate;
    private Date expirationDate;
    private String changeType; // ADD, CHANGE, REMOVE, UNCHANGED

    public EffDatedBean() {
        LOGGER.log(Level.FINE, "→ EffDatedBean.EffDatedBean");
        this.changeType = "ADD";
    }

    @Override
    public Long getID() {
        LOGGER.log(Level.FINE, "→ EffDatedBean.getID"); return id; }

    @Override
    public void setID(Long id) {
        LOGGER.log(Level.FINE, "→ EffDatedBean.setID"); this.id = id; }

    @Override
    public boolean isNew() {
        LOGGER.log(Level.FINE, "→ EffDatedBean.isNew"); return id == null; }

    public FixedId<?> getFixedId() {
        LOGGER.log(Level.FINE, "→ EffDatedBean.getFixedId"); return fixedId; }
    public void setFixedId(FixedId<?> fixedId) {
        LOGGER.log(Level.FINE, "→ EffDatedBean.setFixedId"); this.fixedId = fixedId; }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public EffDatedBranch getBranch() {
        LOGGER.log(Level.FINE, "→ EffDatedBean.getBranch"); return branch; }
    public void setBranch(EffDatedBranch branch) {
        LOGGER.log(Level.FINE, "→ EffDatedBean.setBranch"); this.branch = branch; }

    public Date getEffectiveDate() {
        LOGGER.log(Level.FINE, "→ EffDatedBean.getEffectiveDate"); return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) {
        LOGGER.log(Level.FINE, "→ EffDatedBean.setEffectiveDate"); this.effectiveDate = effectiveDate; }

    public Date getExpirationDate() {
        LOGGER.log(Level.FINE, "→ EffDatedBean.getExpirationDate"); return expirationDate; }
    public void setExpirationDate(Date expirationDate) {
        LOGGER.log(Level.FINE, "→ EffDatedBean.setExpirationDate"); this.expirationDate = expirationDate; }

    public String getChangeType() {
        LOGGER.log(Level.FINE, "→ EffDatedBean.getChangeType"); return changeType; }
    public void setChangeType(String changeType) {
        LOGGER.log(Level.FINE, "→ EffDatedBean.setChangeType"); this.changeType = changeType; }

    public boolean isEffectiveAt(Date date) {
        LOGGER.log(Level.FINE, "→ EffDatedBean.isEffectiveAt");
        if (date == null) return false;
        if (effectiveDate != null && date.before(effectiveDate)) return false;
        return expirationDate == null || date.before(expirationDate);
    }

    public EffDatedBean getSlice(Date asOfDate) {
        LOGGER.log(Level.FINE, "→ EffDatedBean.getSlice");
        if (isEffectiveAt(asOfDate)) {
            return this;
        }
        return null;
    }

    public EffDatedBean getUntypedSlice(Date asOfDate) {
        LOGGER.log(Level.FINE, "→ EffDatedBean.getUntypedSlice");
        return getSlice(asOfDate);
    }

    public abstract EffDatedBean cloneSlice(EffDatedBranch targetBranch, Date sliceEffectiveDate);
}
