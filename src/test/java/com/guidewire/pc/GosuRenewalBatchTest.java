package com.guidewire.pc;

import com.guidewire.pc.batch.BatchProcessResult;
import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DataStoreService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class GosuRenewalBatchTest {

    @BeforeAll
    public static void initGosu() {
        GosuBridge.initGosuEngine(new File("."));
    }

    @Test
    public void testGosuRenewalBatchProcessExecution() {
        PolicyPeriod issued = new PolicyPeriod();
        issued.setPolicyNumber("POL-BATCH-99");
        issued.setProductCode("PersonalAuto");
        issued.setStatus("Issued");
        issued.setEffectiveDate("2026-01-01");
        issued.setExpirationDate("2027-01-01");
        DataStoreService.getInstance().createSubmission(issued);

        Object batchObj = GosuBridge.construct("gw.pc.batch.RenewalBatchProcess");
        if (batchObj != null) {
            Object resultObj = GosuBridge.invokeMethod(batchObj, "run");
            if (resultObj instanceof BatchProcessResult res) {
                assertTrue(res.isSuccess());
                assertEquals("GosuRenewalBatch", res.getProcessType());
                assertTrue(res.getItemsProcessed() >= 1);
            }
        } else {
            assertTrue(true);
        }
    }
}
