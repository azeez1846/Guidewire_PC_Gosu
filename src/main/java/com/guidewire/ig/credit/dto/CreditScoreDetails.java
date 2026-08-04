package com.guidewire.ig.credit.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CreditScoreDetails {
    private static final Logger LOGGER = Logger.getLogger(CreditScoreDetails.class.getName());

    private Integer creditScore;
    private String creditTier; // EXCELLENT, GOOD, FAIR, POOR
    private Boolean ofacSanctionClear;
    private String fraudRiskCategory; // LOW_RISK, MEDIUM_RISK, HIGH_RISK
    private Double recommendedDiscountOrSurchargePct; // e.g. -0.15 (15% discount) or +0.20 (20% surcharge)

    public CreditScoreDetails() {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.CreditScoreDetails");}

    public CreditScoreDetails(Integer creditScore, String creditTier, Boolean ofacSanctionClear, String fraudRiskCategory, Double recommendedDiscountOrSurchargePct) {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.CreditScoreDetails");
        this.creditScore = creditScore;
        this.creditTier = creditTier;
        this.ofacSanctionClear = ofacSanctionClear;
        this.fraudRiskCategory = fraudRiskCategory;
        this.recommendedDiscountOrSurchargePct = recommendedDiscountOrSurchargePct;
    }

    public Integer getCreditScore() {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.getCreditScore"); return creditScore; }
    public void setCreditScore(Integer creditScore) {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.setCreditScore"); this.creditScore = creditScore; }

    public String getCreditTier() {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.getCreditTier"); return creditTier; }
    public void setCreditTier(String creditTier) {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.setCreditTier"); this.creditTier = creditTier; }

    public Boolean getOfacSanctionClear() {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.getOfacSanctionClear"); return ofacSanctionClear; }
    public void setOfacSanctionClear(Boolean ofacSanctionClear) {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.setOfacSanctionClear"); this.ofacSanctionClear = ofacSanctionClear; }

    public String getFraudRiskCategory() {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.getFraudRiskCategory"); return fraudRiskCategory; }
    public void setFraudRiskCategory(String fraudRiskCategory) {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.setFraudRiskCategory"); this.fraudRiskCategory = fraudRiskCategory; }

    public Double getRecommendedDiscountOrSurchargePct() {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.getRecommendedDiscountOrSurchargePct"); return recommendedDiscountOrSurchargePct; }
    public void setRecommendedDiscountOrSurchargePct(Double recommendedDiscountOrSurchargePct) {
        LOGGER.log(Level.FINE, "→ CreditScoreDetails.setRecommendedDiscountOrSurchargePct"); this.recommendedDiscountOrSurchargePct = recommendedDiscountOrSurchargePct; }
}
