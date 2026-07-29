package com.guidewire.pc.model;

import com.guidewire.pc.orm.EffDatedBean;
import com.guidewire.pc.orm.EffDatedBranch;
import com.guidewire.pc.orm.GosuORMSession;

import java.math.BigDecimal;
import java.util.Date;

public class Cost extends EffDatedBean {
    private String chargePattern; // BasePremium, BodilyInjuryCoverage, PropertyDamageCoverage, StateTax, PolicyFee
    private String description;
    private BigDecimal actualAmount = BigDecimal.ZERO;
    private BigDecimal actualTermAmount = BigDecimal.ZERO;

    public Cost() {
        super();
        setID(GosuORMSession.getInstance().nextID());
    }

    public Cost(String chargePattern, String description, BigDecimal actualAmount) {
        this();
        this.chargePattern = chargePattern;
        this.description = description;
        this.actualAmount = actualAmount;
        this.actualTermAmount = actualAmount;
    }

    @Override
    public EffDatedBean cloneSlice(EffDatedBranch newBranch, Date editEffDate) {
        Cost cloned = new Cost();
        cloned.setFixedId(getFixedId());
        cloned.setBranch(newBranch);
        cloned.setEffectiveDate(editEffDate);
        cloned.setExpirationDate(getExpirationDate());
        cloned.setChargePattern(this.chargePattern);
        cloned.setDescription(this.description);
        cloned.setActualAmount(this.actualAmount);
        cloned.setActualTermAmount(this.actualTermAmount);
        return cloned;
    }

    public String getChargePattern() { return chargePattern; }
    public void setChargePattern(String chargePattern) { this.chargePattern = chargePattern; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }

    public BigDecimal getActualTermAmount() { return actualTermAmount; }
    public void setActualTermAmount(BigDecimal actualTermAmount) { this.actualTermAmount = actualTermAmount; }
}
