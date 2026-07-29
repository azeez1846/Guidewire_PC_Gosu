package com.guidewire.pc.batch;

import java.util.Date;

public class BatchProcessResult {
    private final String processType;
    private final boolean success;
    private final int itemsProcessed;
    private final String message;
    private final Date executionTime;

    public BatchProcessResult(String processType, boolean success, int itemsProcessed, String message) {
        this.processType = processType;
        this.success = success;
        this.itemsProcessed = itemsProcessed;
        this.message = message;
        this.executionTime = new Date();
    }

    public String getProcessType() { return processType; }
    public boolean isSuccess() { return success; }
    public int getItemsProcessed() { return itemsProcessed; }
    public String getMessage() { return message; }
    public Date getExecutionTime() { return executionTime; }
}
