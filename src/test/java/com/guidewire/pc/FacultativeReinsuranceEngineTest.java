package com.guidewire.pc;

import com.guidewire.pc.service.FacultativeReinsuranceEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Facultative Reinsurance & Treaty Allocation Unit Tests")
public class FacultativeReinsuranceEngineTest {

    @Test
    @DisplayName("Should Cede Standard Quota Share and Surplus Treaty Within Limits")
    void testStandardTreatyCession() {
        FacultativeReinsuranceEngine engine = FacultativeReinsuranceEngine.getInstance();

        // TIV = $6,000,000, 20% Quota Share ($1.2M), Net = $4.8M
        // Retention = $2.0M, Surplus = $2.8M, Fac Required = $0
        BigDecimal tiv = new BigDecimal("6000000.00");
        BigDecimal qsPct = new BigDecimal("0.20");

        FacultativeReinsuranceEngine.ReinsuranceAllocationResult result = engine.calculateCession(null, tiv, qsPct);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("1200000.00").compareTo(result.getQuotaShareCededAmount()));
        assertEquals(0, new BigDecimal("2000000.00").compareTo(result.getInsurerRetentionAmount()));
        assertEquals(0, new BigDecimal("2800000.00").compareTo(result.getSurplusTreatyCededAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getFacultativeRequiredAmount()));
        assertFalse(result.isFacultativeRequired());
        assertEquals("AUTOMATIC_TREATY_BOUND", result.getPlacementStatus());
    }

    @Test
    @DisplayName("Should Trigger Facultative Placement when TIV Exceeds Retention and Surplus Treaties ($10M+)")
    void testFacultativePlacementTrigger() {
        FacultativeReinsuranceEngine engine = FacultativeReinsuranceEngine.getInstance();

        // TIV = $15,000,000, 20% Quota Share ($3.0M), Net = $12.0M
        // Retention = $2.0M, Surplus Cap = $5.0M, Fac Required = $5.0M
        BigDecimal tiv = new BigDecimal("15000000.00");
        BigDecimal qsPct = new BigDecimal("0.20");

        FacultativeReinsuranceEngine.ReinsuranceAllocationResult result = engine.calculateCession(null, tiv, qsPct);

        assertNotNull(result);
        assertTrue(result.isFacultativeRequired());
        assertEquals(new BigDecimal("5000000.00"), result.getFacultativeRequiredAmount());
        assertTrue(result.getFacultativeCertificateId().startsWith("FAC-CERT-"));
        assertEquals("FACULTATIVE_PLACEMENT_PENDING", result.getPlacementStatus());
        assertFalse(result.getReinsurers().isEmpty());
    }

    @Test
    @DisplayName("Should Accurately Calculate Excess of Loss (XOL) Reinsurance Loss Recovery")
    void testExcessOfLossRecovery() {
        FacultativeReinsuranceEngine engine = FacultativeReinsuranceEngine.getInstance();

        BigDecimal grossLoss = new BigDecimal("7500000.00");
        BigDecimal attachmentPoint = new BigDecimal("2000000.00"); // $2M Retention
        BigDecimal xolLimit = new BigDecimal("10000000.00");        // $10M Layer Limit

        BigDecimal recovery = engine.evaluateExcessOfLossRecovery(grossLoss, attachmentPoint, xolLimit);

        // Recovery = $7.5M - $2.0M = $5.5M
        assertEquals(new BigDecimal("5500000.00"), recovery);
    }
}
