package com.guidewire.pc.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScheduledEquipmentItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ScheduledEquipmentItem.class.getName());

    private int itemNumber;
    private String equipmentType; // HeavyMachinery, MobileTools, TransitCargo, MedicalEquipment, SolarPanels
    private String description;
    private String serialNumber;
    private String makeAndModel;
    private BigDecimal statedValue;
    private BigDecimal deductible;
    private double coinsurancePercentage = 0.80; // Default 80%

    public ScheduledEquipmentItem() {
        this.statedValue = BigDecimal.ZERO;
        this.deductible = new BigDecimal("500.00");
        LOGGER.log(Level.FINE, "ScheduledEquipmentItem initialized");
    }

    public ScheduledEquipmentItem(int itemNumber, String equipmentType, String description, String serialNumber, BigDecimal statedValue) {
        this();
        this.itemNumber = itemNumber;
        this.equipmentType = equipmentType;
        this.description = description;
        this.serialNumber = serialNumber;
        this.statedValue = statedValue != null ? statedValue : BigDecimal.ZERO;
    }

    // Getters and Setters
    public int getItemNumber() { return itemNumber; }
    public void setItemNumber(int itemNumber) { this.itemNumber = itemNumber; }

    public String getEquipmentType() { return equipmentType; }
    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getMakeAndModel() { return makeAndModel; }
    public void setMakeAndModel(String makeAndModel) { this.makeAndModel = makeAndModel; }

    public BigDecimal getStatedValue() { return statedValue; }
    public void setStatedValue(BigDecimal statedValue) { this.statedValue = statedValue; }

    public BigDecimal getDeductible() { return deductible; }
    public void setDeductible(BigDecimal deductible) { this.deductible = deductible; }

    public double getCoinsurancePercentage() { return coinsurancePercentage; }
    public void setCoinsurancePercentage(double coinsurancePercentage) { this.coinsurancePercentage = coinsurancePercentage; }
}
