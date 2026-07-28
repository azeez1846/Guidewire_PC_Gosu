package com.guidewire.pc.model;

public class Activity {
    private String subject;
    private String description;
    private String priority; // Low, Normal, High, Urgent
    private String status;   // Open, Complete, Escalated
    private String dueDate;
    private String assignedUser;
    private String relatedAccountId;
    private String relatedJobNumber;
    private String createTime;

    public Activity() {
        this.status = "Open";
        this.priority = "Normal";
        this.assignedUser = "su";
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getAssignedUser() { return assignedUser; }
    public void setAssignedUser(String assignedUser) { this.assignedUser = assignedUser; }

    public String getRelatedAccountId() { return relatedAccountId; }
    public void setRelatedAccountId(String relatedAccountId) { this.relatedAccountId = relatedAccountId; }

    public String getRelatedJobNumber() { return relatedJobNumber; }
    public void setRelatedJobNumber(String relatedJobNumber) { this.relatedJobNumber = relatedJobNumber; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
