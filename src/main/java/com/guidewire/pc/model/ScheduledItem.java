package com.guidewire.pc.model;

import com.guidewire.pc.orm.KeyableBean;
import com.guidewire.pc.orm.GosuORMSession;
import java.math.BigDecimal;

public class ScheduledItem implements KeyableBean {
    private Long id;
    private Integer itemNumber;
    private String description;
    private String serialNumber;
    private String category;
    private BigDecimal statedValue = BigDecimal.ZERO;
    private BigDecimal itemDeductible = BigDecimal.ZERO;
    private BigDecimal itemPremium = BigDecimal.ZERO;

    public ScheduledItem() {
        this.id = GosuORMSession.getInstance().nextID();
    }

    public ScheduledItem(Integer itemNumber, String description, String serialNumber, String category, BigDecimal statedValue) {
        this();
        this.itemNumber = itemNumber;
        this.description = description;
        this.serialNumber = serialNumber;
        this.category = category;
        this.statedValue = statedValue != null ? statedValue : BigDecimal.ZERO;
    }

    @Override
    public Long getID() { return id; }
    @Override
    public void setID(Long id) { this.id = id; }
    @Override
    public boolean isNew() { return id == null; }

    public Integer getItemNumber() { return itemNumber; }
    public void setItemNumber(Integer itemNumber) { this.itemNumber = itemNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getStatedValue() { return statedValue; }
    public void setStatedValue(BigDecimal statedValue) { this.statedValue = statedValue; }

    public BigDecimal getItemDeductible() { return itemDeductible; }
    public void setItemDeductible(BigDecimal itemDeductible) { this.itemDeductible = itemDeductible; }

    public BigDecimal getItemPremium() { return itemPremium; }
    public void setItemPremium(BigDecimal itemPremium) { this.itemPremium = itemPremium; }
}
