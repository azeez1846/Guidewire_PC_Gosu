package com.guidewire.pc.model;

import com.guidewire.pc.orm.EffDatedBean;
import com.guidewire.pc.orm.EffDatedBranch;
import com.guidewire.pc.orm.GosuORMSession;

import java.math.BigDecimal;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cost extends EffDatedBean {
    private static final Logger LOGGER = Logger.getLogger(Cost.class.getName());

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
        LOGGER.log(Level.FINE, "Cloning Cost slice for pattern={0}, editEffDate={1}", new Object[]{this.chargePattern, editEffDate});
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cost cost)) return false;
        return java.util.Objects.equals(getID(), cost.getID());
    }

    @Override
    public int hashCode() {
        return getID() != null ? getID().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Cost{" +
                "id=" + getID() +
                ", pattern='" + chargePattern + '\'' +
                ", amount=" + actualAmount +
                '}';
    }
}
