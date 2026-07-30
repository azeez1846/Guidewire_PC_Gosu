package com.guidewire.pc.batch;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BatchProcessManager {
    private static final Logger LOGGER = Logger.getLogger(BatchProcessManager.class.getName());
    private static final BatchProcessManager instance = new BatchProcessManager();

    private final Map<String, BatchProcess> batchProcesses = new HashMap<>();

    private BatchProcessManager() {
        registerProcess(new PolicyRenewalBatchProcess());
        registerProcess(new ActivityEscalationBatchProcess());
    }

    public static BatchProcessManager getInstance() {
        return instance;
    }

    public final void registerProcess(BatchProcess process) {
        batchProcesses.put(process.getType().toLowerCase(), process);
    }

    public BatchProcessResult runProcess(String type) {
        BatchProcess process = batchProcesses.get(type.toLowerCase());
        if (process == null) {
            LOGGER.log(java.util.logging.Level.WARNING, "Unknown batch process type requested: {0}", type);
            return new BatchProcessResult(type, false, 0, "Process type not registered");
        }
        return process.run();
    }

    public Map<String, BatchProcess> getRegisteredProcesses() {
        return batchProcesses;
    }
}
