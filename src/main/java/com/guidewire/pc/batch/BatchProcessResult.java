package com.guidewire.pc.batch;

import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

public class BatchProcessResult {
    private static final Logger LOGGER = Logger.getLogger(BatchProcessResult.class.getName());

    private final String processType;
    private final boolean success;
    private final int itemsProcessed;
    private final String message;
    private final Date executionTime;

    public BatchProcessResult(String processType, boolean success, int itemsProcessed, String message) {
        LOGGER.log(Level.FINE, "→ BatchProcessResult.BatchProcessResult");
        this.processType = processType;
        this.success = success;
        this.itemsProcessed = itemsProcessed;
        this.message = message;
        this.executionTime = new Date();
    }

    public String getProcessType() {
        LOGGER.log(Level.FINE, "→ BatchProcessResult.getProcessType"); return processType; }
    public boolean isSuccess() {
        LOGGER.log(Level.FINE, "→ BatchProcessResult.isSuccess"); return success; }
    public int getItemsProcessed() {
        LOGGER.log(Level.FINE, "→ BatchProcessResult.getItemsProcessed"); return itemsProcessed; }
    public String getMessage() {
        LOGGER.log(Level.FINE, "→ BatchProcessResult.getMessage"); return message; }
    public Date getExecutionTime() {
        LOGGER.log(Level.FINE, "→ BatchProcessResult.getExecutionTime"); return executionTime; }
}
