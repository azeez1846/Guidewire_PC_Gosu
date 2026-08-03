package com.guidewire.pc.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ReinsuranceTreatyLayer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String treatyNumber;
    private String treatyName;
    private String treatyType; // QuotaShare, SurplusShare, ExcessOfLoss, CatastropheXOL
    private String reinsurerName; // Swiss Re, Munich Re, Lloyd's of London, Hannover Re
    private BigDecimal attachmentPoint;
    private BigDecimal layerLimit;
    private double cessionPercentage; // e.g. 0.30 for 30%

    public ReinsuranceTreatyLayer() {
        this.attachmentPoint = BigDecimal.ZERO;
        this.layerLimit = new BigDecimal("5000000.00");
        this.cessionPercentage = 0.25;
    }

    public ReinsuranceTreatyLayer(String treatyNumber, String treatyName, String treatyType, String reinsurerName, BigDecimal attachmentPoint, BigDecimal layerLimit, double cessionPercentage) {
        this.treatyNumber = treatyNumber;
        this.treatyName = treatyName;
        this.treatyType = treatyType;
        this.reinsurerName = reinsurerName;
        this.attachmentPoint = attachmentPoint;
        this.layerLimit = layerLimit;
        this.cessionPercentage = cessionPercentage;
    }

    public String getTreatyNumber() { return treatyNumber; }
    public void setTreatyNumber(String treatyNumber) { this.treatyNumber = treatyNumber; }

    public String getTreatyName() { return treatyName; }
    public void setTreatyName(String treatyName) { this.treatyName = treatyName; }

    public String getTreatyType() { return treatyType; }
    public void setTreatyType(String treatyType) { this.treatyType = treatyType; }

    public String getReinsurerName() { return reinsurerName; }
    public void setReinsurerName(String reinsurerName) { this.reinsurerName = reinsurerName; }

    public BigDecimal getAttachmentPoint() { return attachmentPoint; }
    public void setAttachmentPoint(BigDecimal attachmentPoint) { this.attachmentPoint = attachmentPoint; }

    public BigDecimal getLayerLimit() { return layerLimit; }
    public void setLayerLimit(BigDecimal layerLimit) { this.layerLimit = layerLimit; }

    public double getCessionPercentage() { return cessionPercentage; }
    public void setCessionPercentage(double cessionPercentage) { this.cessionPercentage = cessionPercentage; }
}
