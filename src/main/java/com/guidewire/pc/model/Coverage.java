package com.guidewire.pc.model;

import com.guidewire.pc.orm.EffDatedBean;
import com.guidewire.pc.orm.EffDatedBranch;
import com.guidewire.pc.orm.GosuORMSession;

import java.math.BigDecimal;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Coverage extends EffDatedBean {
    private static final Logger LOGGER = Logger.getLogger(Coverage.class.getName());

    private String patternCode;
    private String coverageName;
    private BigDecimal directLimit = BigDecimal.ZERO;
    private BigDecimal deductible = BigDecimal.ZERO;

    public Coverage() {
        LOGGER.log(Level.FINE, "→ Coverage.Coverage");}

    public Coverage(String patternCode, String coverageName, BigDecimal directLimit, BigDecimal deductible) {
        LOGGER.log(Level.FINE, "→ Coverage.Coverage");
        this.patternCode = patternCode;
        this.coverageName = coverageName;
        this.directLimit = directLimit;
        this.deductible = deductible;
    }

    public String getPatternCode() {
        LOGGER.log(Level.FINE, "→ Coverage.getPatternCode"); return patternCode; }
    public void setPatternCode(String patternCode) {
        LOGGER.log(Level.FINE, "→ Coverage.setPatternCode"); this.patternCode = patternCode; }

    public String getCoverageName() {
        LOGGER.log(Level.FINE, "→ Coverage.getCoverageName"); return coverageName; }
    public String getPatternName() {
        LOGGER.log(Level.FINE, "→ Coverage.getPatternName"); return coverageName; }
    public void setCoverageName(String coverageName) {
        LOGGER.log(Level.FINE, "→ Coverage.setCoverageName"); this.coverageName = coverageName; }

    public BigDecimal getDirectLimit() {
        LOGGER.log(Level.FINE, "→ Coverage.getDirectLimit"); return directLimit; }
    public void setDirectLimit(BigDecimal directLimit) {
        LOGGER.log(Level.FINE, "→ Coverage.setDirectLimit"); this.directLimit = directLimit; }

    public BigDecimal getDeductible() {
        LOGGER.log(Level.FINE, "→ Coverage.getDeductible"); return deductible; }
    public void setDeductible(BigDecimal deductible) {
        LOGGER.log(Level.FINE, "→ Coverage.setDeductible"); this.deductible = deductible; }

    @Override
    public EffDatedBean cloneSlice(EffDatedBranch targetBranch, Date sliceEffectiveDate) {
        LOGGER.log(Level.FINE, "→ Coverage.cloneSlice");
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
