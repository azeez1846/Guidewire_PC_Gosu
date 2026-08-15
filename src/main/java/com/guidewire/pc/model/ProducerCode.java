package com.guidewire.pc.model;

import com.guidewire.pc.orm.KeyableBean;
import com.guidewire.pc.orm.GosuORMSession;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProducerCode implements KeyableBean {
    private static final Logger LOGGER = Logger.getLogger(ProducerCode.class.getName());

    private Long id;
    private String code;
    private String producerStatus = "Active";
    private String tier = "Gold";
    private BigDecimal newBusinessCommissionRate = new BigDecimal("15.00"); // 15%
    private BigDecimal renewalCommissionRate = new BigDecimal("10.00"); // 10%
    private String licensedStates = "TX,FL,CA,NY,IL";

    public ProducerCode() {
        this.id = GosuORMSession.getInstance().nextID();
        LOGGER.log(Level.FINE, "ProducerCode instantiated with ID={0}", this.id);
    }

    public ProducerCode(String code, String producerStatus, String tier, BigDecimal newBusinessCommissionRate, BigDecimal renewalCommissionRate, String licensedStates) {
        this();
        this.code = code;
        this.producerStatus = producerStatus != null ? producerStatus : "Active";
        this.tier = tier != null ? tier : "Gold";
        this.newBusinessCommissionRate = newBusinessCommissionRate != null ? newBusinessCommissionRate : new BigDecimal("15.00");
        this.renewalCommissionRate = renewalCommissionRate != null ? renewalCommissionRate : new BigDecimal("10.00");
        this.licensedStates = licensedStates != null ? licensedStates : "TX,FL,CA,NY,IL";
    }

    @Override
    public Long getID() { return id; }
    @Override
    public void setID(Long id) { this.id = id; }
    @Override
    public boolean isNew() { return id == null; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getProducerStatus() { return producerStatus; }
    public void setProducerStatus(String producerStatus) { this.producerStatus = producerStatus; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public BigDecimal getNewBusinessCommissionRate() { return newBusinessCommissionRate; }
    public void setNewBusinessCommissionRate(BigDecimal newBusinessCommissionRate) { this.newBusinessCommissionRate = newBusinessCommissionRate; }

    public BigDecimal getRenewalCommissionRate() { return renewalCommissionRate; }
    public void setRenewalCommissionRate(BigDecimal renewalCommissionRate) { this.renewalCommissionRate = renewalCommissionRate; }

    public String getLicensedStates() { return licensedStates; }
    public void setLicensedStates(String licensedStates) { this.licensedStates = licensedStates; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProducerCode that)) return false;
        return java.util.Objects.equals(id, that.id) ||
                (code != null && code.equalsIgnoreCase(that.code));
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : (code != null ? code.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "ProducerCode{" +
                "code='" + code + '\'' +
                ", tier='" + tier + '\'' +
                ", status='" + producerStatus + '\'' +
                '}';
    }
}
