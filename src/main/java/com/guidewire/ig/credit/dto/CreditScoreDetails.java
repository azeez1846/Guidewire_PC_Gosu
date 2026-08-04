package com.guidewire.ig.credit.dto;

public class CreditScoreDetails {
    private Integer creditScore;
    private String creditTier; // EXCELLENT, GOOD, FAIR, POOR
    private Boolean ofacSanctionClear;
    private String fraudRiskCategory; // LOW_RISK, MEDIUM_RISK, HIGH_RISK
    private Double recommendedDiscountOrSurchargePct; // e.g. -0.15 (15% discount) or +0.20 (20% surcharge)

    public CreditScoreDetails() {}

    public CreditScoreDetails(Integer creditScore, String creditTier, Boolean ofacSanctionClear, String fraudRiskCategory, Double recommendedDiscountOrSurchargePct) {
        this.creditScore = creditScore;
        this.creditTier = creditTier;
        this.ofacSanctionClear = ofacSanctionClear;
        this.fraudRiskCategory = fraudRiskCategory;
        this.recommendedDiscountOrSurchargePct = recommendedDiscountOrSurchargePct;
    }

    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }

    public String getCreditTier() { return creditTier; }
    public void setCreditTier(String creditTier) { this.creditTier = creditTier; }

    public Boolean getOfacSanctionClear() { return ofacSanctionClear; }
    public void setOfacSanctionClear(Boolean ofacSanctionClear) { this.ofacSanctionClear = ofacSanctionClear; }

    public String getFraudRiskCategory() { return fraudRiskCategory; }
    public void setFraudRiskCategory(String fraudRiskCategory) { this.fraudRiskCategory = fraudRiskCategory; }

    public Double getRecommendedDiscountOrSurchargePct() { return recommendedDiscountOrSurchargePct; }
    public void setRecommendedDiscountOrSurchargePct(Double recommendedDiscountOrSurchargePct) { this.recommendedDiscountOrSurchargePct = recommendedDiscountOrSurchargePct; }
}
