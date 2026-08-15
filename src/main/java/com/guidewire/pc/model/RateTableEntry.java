package com.guidewire.pc.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RateTableEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RateTableEntry.class.getName());

    private String productCode;
    private String state;
    private String territory;
    private String riskTier;
    private BigDecimal baseRate;
    private double territoryFactor;
    private double riskTierFactor;
    private double deductibleModifier;

    public RateTableEntry() {
        this.territoryFactor = 1.00;
        this.riskTierFactor = 1.00;
        this.deductibleModifier = 1.00;
        LOGGER.log(Level.FINE, "RateTableEntry initialized");
    }

    public RateTableEntry(String productCode, String state, String territory, String riskTier, BigDecimal baseRate, double territoryFactor, double riskTierFactor) {
        this();
        this.productCode = productCode;
        this.state = state;
        this.territory = territory;
        this.riskTier = riskTier;
        this.baseRate = baseRate;
        this.territoryFactor = territoryFactor;
        this.riskTierFactor = riskTierFactor;
    }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getTerritory() { return territory; }
    public void setTerritory(String territory) { this.territory = territory; }

    public String getRiskTier() { return riskTier; }
    public void setRiskTier(String riskTier) { this.riskTier = riskTier; }

    public BigDecimal getBaseRate() { return baseRate; }
    public void setBaseRate(BigDecimal baseRate) { this.baseRate = baseRate; }

    public double getTerritoryFactor() { return territoryFactor; }
    public void setTerritoryFactor(double territoryFactor) { this.territoryFactor = territoryFactor; }

    public double getRiskTierFactor() { return riskTierFactor; }
    public void setRiskTierFactor(double riskTierFactor) { this.riskTierFactor = riskTierFactor; }

    public double getDeductibleModifier() { return deductibleModifier; }
    public void setDeductibleModifier(double deductibleModifier) { this.deductibleModifier = deductibleModifier; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RateTableEntry that)) return false;
        return java.util.Objects.equals(productCode, that.productCode) &&
                java.util.Objects.equals(state, that.state) &&
                java.util.Objects.equals(territory, that.territory) &&
                java.util.Objects.equals(riskTier, that.riskTier);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(productCode, state, territory, riskTier);
    }

    @Override
    public String toString() {
        return "RateTableEntry{" +
                "product='" + productCode + '\'' +
                ", state='" + state + '\'' +
                ", territory='" + territory + '\'' +
                ", tier='" + riskTier + '\'' +
                ", baseRate=" + baseRate +
                '}';
    }
}
