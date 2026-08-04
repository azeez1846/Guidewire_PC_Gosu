package com.guidewire.pc.model;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Activity {
    private static final Logger LOGGER = Logger.getLogger(Activity.class.getName());

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
        LOGGER.log(Level.FINE, "→ Activity.Activity");
        this.status = "Open";
        this.priority = "Normal";
        this.assignedUser = "su";
    }

    public String getSubject() {
        LOGGER.log(Level.FINE, "→ Activity.getSubject"); return subject; }
    public void setSubject(String subject) {
        LOGGER.log(Level.FINE, "→ Activity.setSubject"); this.subject = subject; }

    public String getDescription() {
        LOGGER.log(Level.FINE, "→ Activity.getDescription"); return description; }
    public void setDescription(String description) {
        LOGGER.log(Level.FINE, "→ Activity.setDescription"); this.description = description; }

    public String getPriority() {
        LOGGER.log(Level.FINE, "→ Activity.getPriority"); return priority; }
    public void setPriority(String priority) {
        LOGGER.log(Level.FINE, "→ Activity.setPriority"); this.priority = priority; }

    public String getStatus() {
        LOGGER.log(Level.FINE, "→ Activity.getStatus"); return status; }
    public void setStatus(String status) {
        LOGGER.log(Level.FINE, "→ Activity.setStatus"); this.status = status; }

    public String getDueDate() {
        LOGGER.log(Level.FINE, "→ Activity.getDueDate"); return dueDate; }
    public void setDueDate(String dueDate) {
        LOGGER.log(Level.FINE, "→ Activity.setDueDate"); this.dueDate = dueDate; }

    public String getAssignedUser() {
        LOGGER.log(Level.FINE, "→ Activity.getAssignedUser"); return assignedUser; }
    public void setAssignedUser(String assignedUser) {
        LOGGER.log(Level.FINE, "→ Activity.setAssignedUser"); this.assignedUser = assignedUser; }

    public String getRelatedAccountId() {
        LOGGER.log(Level.FINE, "→ Activity.getRelatedAccountId"); return relatedAccountId; }
    public void setRelatedAccountId(String relatedAccountId) {
        LOGGER.log(Level.FINE, "→ Activity.setRelatedAccountId"); this.relatedAccountId = relatedAccountId; }

    public String getRelatedJobNumber() {
        LOGGER.log(Level.FINE, "→ Activity.getRelatedJobNumber"); return relatedJobNumber; }
    public void setRelatedJobNumber(String relatedJobNumber) {
        LOGGER.log(Level.FINE, "→ Activity.setRelatedJobNumber"); this.relatedJobNumber = relatedJobNumber; }

    public String getCreateTime() {
        LOGGER.log(Level.FINE, "→ Activity.getCreateTime"); return createTime; }
    public void setCreateTime(String createTime) {
        LOGGER.log(Level.FINE, "→ Activity.setCreateTime"); this.createTime = createTime; }
}
