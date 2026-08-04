package com.guidewire.ig.credit.dto;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CreditFraudResponse {
    private static final Logger LOGGER = Logger.getLogger(CreditFraudResponse.class.getName());

    private String transactionId;
    private String status;
    private CreditScoreDetails scoreDetails;
    private String underwritingAction; // APPROVE_PREFERRED, APPROVE_STANDARD, MANDATORY_UW_REVIEW, DECLINE_OFAC
    private String gatewayMetadata;

    public CreditFraudResponse() {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.CreditFraudResponse");}

    public CreditFraudResponse(String transactionId, String status, CreditScoreDetails scoreDetails, String underwritingAction, String gatewayMetadata) {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.CreditFraudResponse");
        this.transactionId = transactionId;
        this.status = status;
        this.scoreDetails = scoreDetails;
        this.underwritingAction = underwritingAction;
        this.gatewayMetadata = gatewayMetadata;
    }

    public String getTransactionId() {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.getTransactionId"); return transactionId; }
    public void setTransactionId(String transactionId) {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.setTransactionId"); this.transactionId = transactionId; }

    public String getStatus() {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.getStatus"); return status; }
    public void setStatus(String status) {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.setStatus"); this.status = status; }

    public CreditScoreDetails getScoreDetails() {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.getScoreDetails"); return scoreDetails; }
    public void setScoreDetails(CreditScoreDetails scoreDetails) {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.setScoreDetails"); this.scoreDetails = scoreDetails; }

    public String getUnderwritingAction() {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.getUnderwritingAction"); return underwritingAction; }
    public void setUnderwritingAction(String underwritingAction) {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.setUnderwritingAction"); this.underwritingAction = underwritingAction; }

    public String getGatewayMetadata() {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.getGatewayMetadata"); return gatewayMetadata; }
    public void setGatewayMetadata(String gatewayMetadata) {
        LOGGER.log(Level.FINE, "→ CreditFraudResponse.setGatewayMetadata"); this.gatewayMetadata = gatewayMetadata; }
}
