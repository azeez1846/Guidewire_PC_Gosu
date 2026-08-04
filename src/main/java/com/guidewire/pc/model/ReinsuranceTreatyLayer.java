package com.guidewire.pc.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ReinsuranceTreatyLayer implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(ReinsuranceTreatyLayer.class.getName());

    private static final long serialVersionUID = 1L;

    private String treatyNumber;
    private String treatyName;
    private String treatyType; // QuotaShare, SurplusShare, ExcessOfLoss, CatastropheXOL
    private String reinsurerName; // Swiss Re, Munich Re, Lloyd's of London, Hannover Re
    private BigDecimal attachmentPoint;
    private BigDecimal layerLimit;
    private double cessionPercentage; // e.g. 0.30 for 30%

    public ReinsuranceTreatyLayer() {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.ReinsuranceTreatyLayer");
        this.attachmentPoint = BigDecimal.ZERO;
        this.layerLimit = new BigDecimal("5000000.00");
        this.cessionPercentage = 0.25;
    }

    public ReinsuranceTreatyLayer(String treatyNumber, String treatyName, String treatyType, String reinsurerName, BigDecimal attachmentPoint, BigDecimal layerLimit, double cessionPercentage) {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.ReinsuranceTreatyLayer");
        this.treatyNumber = treatyNumber;
        this.treatyName = treatyName;
        this.treatyType = treatyType;
        this.reinsurerName = reinsurerName;
        this.attachmentPoint = attachmentPoint;
        this.layerLimit = layerLimit;
        this.cessionPercentage = cessionPercentage;
    }

    public String getTreatyNumber() {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.getTreatyNumber"); return treatyNumber; }
    public void setTreatyNumber(String treatyNumber) {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.setTreatyNumber"); this.treatyNumber = treatyNumber; }

    public String getTreatyName() {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.getTreatyName"); return treatyName; }
    public void setTreatyName(String treatyName) {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.setTreatyName"); this.treatyName = treatyName; }

    public String getTreatyType() {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.getTreatyType"); return treatyType; }
    public void setTreatyType(String treatyType) {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.setTreatyType"); this.treatyType = treatyType; }

    public String getReinsurerName() {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.getReinsurerName"); return reinsurerName; }
    public void setReinsurerName(String reinsurerName) {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.setReinsurerName"); this.reinsurerName = reinsurerName; }

    public BigDecimal getAttachmentPoint() {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.getAttachmentPoint"); return attachmentPoint; }
    public void setAttachmentPoint(BigDecimal attachmentPoint) {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.setAttachmentPoint"); this.attachmentPoint = attachmentPoint; }

    public BigDecimal getLayerLimit() {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.getLayerLimit"); return layerLimit; }
    public void setLayerLimit(BigDecimal layerLimit) {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.setLayerLimit"); this.layerLimit = layerLimit; }

    public double getCessionPercentage() {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.getCessionPercentage"); return cessionPercentage; }
    public void setCessionPercentage(double cessionPercentage) {
        LOGGER.log(Level.FINE, "→ ReinsuranceTreatyLayer.setCessionPercentage"); this.cessionPercentage = cessionPercentage; }
}
