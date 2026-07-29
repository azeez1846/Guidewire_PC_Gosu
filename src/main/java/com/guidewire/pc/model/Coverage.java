package com.guidewire.pc.model;

import com.guidewire.pc.orm.EffDatedBean;
import com.guidewire.pc.orm.EffDatedBranch;
import com.guidewire.pc.orm.GosuORMSession;

import java.math.BigDecimal;
import java.util.Date;

public class Coverage extends EffDatedBean {
    private String patternCode;
    private String coverageName;
    private BigDecimal directLimit = BigDecimal.ZERO;
    private BigDecimal deductible = BigDecimal.ZERO;

    public Coverage() {}

    public Coverage(String patternCode, String coverageName, BigDecimal directLimit, BigDecimal deductible) {
        this.patternCode = patternCode;
        this.coverageName = coverageName;
        this.directLimit = directLimit;
        this.deductible = deductible;
    }

    public String getPatternCode() { return patternCode; }
    public void setPatternCode(String patternCode) { this.patternCode = patternCode; }

    public String getCoverageName() { return coverageName; }
    public String getPatternName() { return coverageName; }
    public void setCoverageName(String coverageName) { this.coverageName = coverageName; }

    public BigDecimal getDirectLimit() { return directLimit; }
    public void setDirectLimit(BigDecimal directLimit) { this.directLimit = directLimit; }

    public BigDecimal getDeductible() { return deductible; }
    public void setDeductible(BigDecimal deductible) { this.deductible = deductible; }

    @Override
    public EffDatedBean cloneSlice(EffDatedBranch targetBranch, Date sliceEffectiveDate) {
        Coverage clone = new Coverage();
        clone.setID(GosuORMSession.getInstance().nextID());
        clone.setFixedId(this.getFixedId()); // KEEP SAME FIXEDID ACROSS SLICES!
        clone.setBranch(targetBranch);
        clone.setPatternCode(this.patternCode);
        clone.setCoverageName(this.coverageName);
        clone.setDirectLimit(this.directLimit);
        clone.setDeductible(this.deductible);
        clone.setEffectiveDate(sliceEffectiveDate);
        clone.setExpirationDate(targetBranch != null ? targetBranch.getPeriodEnd() : this.getExpirationDate());
        clone.setChangeType("CHANGE");
        return clone;
    }
}
