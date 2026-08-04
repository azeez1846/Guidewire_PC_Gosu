package com.guidewire.ig.credit.dto;

public class CreditFraudResponse {
    private String transactionId;
    private String status;
    private CreditScoreDetails scoreDetails;
    private String underwritingAction; // APPROVE_PREFERRED, APPROVE_STANDARD, MANDATORY_UW_REVIEW, DECLINE_OFAC
    private String gatewayMetadata;

    public CreditFraudResponse() {}

    public CreditFraudResponse(String transactionId, String status, CreditScoreDetails scoreDetails, String underwritingAction, String gatewayMetadata) {
        this.transactionId = transactionId;
        this.status = status;
        this.scoreDetails = scoreDetails;
        this.underwritingAction = underwritingAction;
        this.gatewayMetadata = gatewayMetadata;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public CreditScoreDetails getScoreDetails() { return scoreDetails; }
    public void setScoreDetails(CreditScoreDetails scoreDetails) { this.scoreDetails = scoreDetails; }

    public String getUnderwritingAction() { return underwritingAction; }
    public void setUnderwritingAction(String underwritingAction) { this.underwritingAction = underwritingAction; }

    public String getGatewayMetadata() { return gatewayMetadata; }
    public void setGatewayMetadata(String gatewayMetadata) { this.gatewayMetadata = gatewayMetadata; }
}
