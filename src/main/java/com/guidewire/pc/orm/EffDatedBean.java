package com.guidewire.pc.orm;

import java.util.Date;

public abstract class EffDatedBean implements KeyableBean {
    private Long id;
    private FixedId<?> fixedId;
    private EffDatedBranch branch;
    private Date effectiveDate;
    private Date expirationDate;
    private String changeType; // ADD, CHANGE, REMOVE, UNCHANGED

    public EffDatedBean() {
        this.changeType = "ADD";
    }

    @Override
    public Long getID() { return id; }

    @Override
    public void setID(Long id) { this.id = id; }

    @Override
    public boolean isNew() { return id == null; }

    public FixedId<?> getFixedId() { return fixedId; }
    public void setFixedId(FixedId<?> fixedId) { this.fixedId = fixedId; }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public EffDatedBranch getBranch() { return branch; }
    public void setBranch(EffDatedBranch branch) { this.branch = branch; }

    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }

    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public boolean isEffectiveAt(Date date) {
        if (date == null) return false;
        if (effectiveDate != null && date.before(effectiveDate)) return false;
        return expirationDate == null || date.before(expirationDate);
    }

    public EffDatedBean getSlice(Date asOfDate) {
        if (isEffectiveAt(asOfDate)) {
            return this;
        }
        return null;
    }

    public EffDatedBean getUntypedSlice(Date asOfDate) {
        return getSlice(asOfDate);
    }

    public abstract EffDatedBean cloneSlice(EffDatedBranch targetBranch, Date sliceEffectiveDate);
}
