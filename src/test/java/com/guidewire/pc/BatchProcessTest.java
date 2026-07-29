package com.guidewire.pc;

import com.guidewire.pc.batch.BatchProcessManager;
import com.guidewire.pc.batch.BatchProcessResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BatchProcessTest {

    private BatchProcessManager manager;

    @BeforeEach
    public void setUp() {
        manager = BatchProcessManager.getInstance();
    }

    @Test
    public void testPolicyRenewalBatchProcess() {
        BatchProcessResult result = manager.runProcess("PolicyRenewal");
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("PolicyRenewal", result.getProcessType());
    }

    @Test
    public void testActivityEscalationBatchProcess() {
        BatchProcessResult result = manager.runProcess("ActivityEscalation");
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("ActivityEscalation", result.getProcessType());
    }
}
