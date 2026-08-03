package com.guidewire.pc.model;

import com.guidewire.pc.constants.PCConstants;

import java.io.Serializable;
import java.util.Date;

public class UWIssue implements Serializable {
    private static final long serialVersionUID = 1L;

    private String issueKey;
    private String issueCode;
    private String shortDescription;
    private String longDescription;
    private String severity; // BlockingQuote, BlockingBind, Informational
    private String status;   // Open, Approved, Rejected
    private String requiredAuthorityLevel; // Underwriter, SeniorUnderwriter, ExecutiveUnderwriter
    private String approvedBy;
    private String approvalReason;
    private Date createTime;
    private Date updateTime;

    public UWIssue() {
        this.status = PCConstants.UW_STATUS_OPEN;
        this.severity = PCConstants.UW_SEVERITY_BLOCKING_BIND;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public UWIssue(String issueCode, String shortDescription, String longDescription, String severity, String requiredAuthorityLevel) {
        this();
        this.issueKey = "UW-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        this.issueCode = issueCode;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.severity = severity != null ? severity : PCConstants.UW_SEVERITY_BLOCKING_BIND;
        this.requiredAuthorityLevel = requiredAuthorityLevel != null ? requiredAuthorityLevel : "Underwriter";
    }

    public boolean isOpen() {
        return PCConstants.UW_STATUS_OPEN.equalsIgnoreCase(this.status);
    }

    public boolean isBlockingQuote() {
        return isOpen() && PCConstants.UW_SEVERITY_BLOCKING_QUOTE.equalsIgnoreCase(this.severity);
    }

    public boolean isBlockingBind() {
        return isOpen() && (PCConstants.UW_SEVERITY_BLOCKING_QUOTE.equalsIgnoreCase(this.severity) ||
                            PCConstants.UW_SEVERITY_BLOCKING_BIND.equalsIgnoreCase(this.severity));
    }

    public void approve(String approvedBy, String approvalReason) {
        this.status = PCConstants.UW_STATUS_APPROVED;
        this.approvedBy = approvedBy;
        this.approvalReason = approvalReason;
        this.updateTime = new Date();
    }

    public void reject(String rejectedBy, String rejectReason) {
        this.status = PCConstants.UW_STATUS_REJECTED;
        this.approvedBy = rejectedBy;
        this.approvalReason = rejectReason;
        this.updateTime = new Date();
    }

    // Getters and Setters
    public String getIssueKey() { return issueKey; }
    public void setIssueKey(String issueKey) { this.issueKey = issueKey; }

    public String getIssueCode() { return issueCode; }
    public void setIssueCode(String issueCode) { this.issueCode = issueCode; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getLongDescription() { return longDescription; }
    public void setLongDescription(String longDescription) { this.longDescription = longDescription; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequiredAuthorityLevel() { return requiredAuthorityLevel; }
    public void setRequiredAuthorityLevel(String requiredAuthorityLevel) { this.requiredAuthorityLevel = requiredAuthorityLevel; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getApprovalReason() { return approvalReason; }
    public void setApprovalReason(String approvalReason) { this.approvalReason = approvalReason; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
