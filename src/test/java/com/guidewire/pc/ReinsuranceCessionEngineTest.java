package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.RIAgreement;
import com.guidewire.pc.model.RICession;
import com.guidewire.pc.service.ReinsuranceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reinsurance Cession & Treaty Engine Tests")
public class ReinsuranceCessionEngineTest {

    private PolicyPeriod period;

    @BeforeEach
    public void setUp() {
        period = new PolicyPeriod();
        period.setPolicyNumber("POL-RI-80008");
        period.setTotalPremium(new BigDecimal("50000.00"));
    }

    @Test
    @DisplayName("Quota Share Treaty Cession & Ceding Commission Rating")
    public void testQuotaShareCession() {
        RIAgreement agreement = new RIAgreement("AGR-QS-01", "30% Quota Share Treaty", "QuotaShare", new BigDecimal("5000000.00"), new BigDecimal("30.00"));
        agreement.setCedingCommissionPct(new BigDecimal("20.00"));

        RICession cession = ReinsuranceService.calculateCession(period, agreement, new BigDecimal("2000000.00"));

        assertNotNull(cession);
        // 30% of $2,000,000 exposure = $600,000 ceded exposure
        assertEquals(new BigDecimal("600000.00"), cession.getCededExposure());
        assertEquals(new BigDecimal("1400000.00"), cession.getRetainedExposure());

        // 30% of $50,000 written premium = $15,000 ceded premium
        assertEquals(new BigDecimal("15000.00"), cession.getCededPremium());

        // 20% ceding commission on $15,000 ceded premium = $3,000
        assertEquals(new BigDecimal("3000.00"), cession.getCedingCommission());
    }

    @Test
    @DisplayName("Surplus Treaty Cession & Retention Line Limit")
    public void testSurplusTreatyCession() {
        RIAgreement agreement = new RIAgreement("AGR-SURPLUS-01", "Property Surplus Treaty", "Surplus", new BigDecimal("500000.00"), new BigDecimal("0.00"));
        agreement.setCedingCommissionPct(new BigDecimal("15.00"));

        BigDecimal grossExposure = new BigDecimal("2000000.00");
        RICession cession = ReinsuranceService.calculateCession(period, agreement, grossExposure);

        assertNotNull(cession);
        // Retained line limit = $500,000. Surplus ceded = $1,500,000
        assertEquals(new BigDecimal("500000.00"), cession.getRetainedExposure());
        assertEquals(new BigDecimal("1500000.00"), cession.getCededExposure());

        // Ceded ratio = 1,500,000 / 2,000,000 = 75%. Ceded premium = $50,000 * 0.75 = $37,500.00
        assertEquals(new BigDecimal("37500.00"), cession.getCededPremium());
        assertEquals(new BigDecimal("5625.00"), cession.getCedingCommission());
    }

    @Test
    @DisplayName("Facultative Placement Alert for Large Risk Exposure")
    public void testFacultativePlacementAlert() {
        RIAgreement agreement = new RIAgreement("AGR-QS-02", "Small Capacity Treaty", "QuotaShare", new BigDecimal("1000000.00"), new BigDecimal("20.00"));

        BigDecimal largeExposure = new BigDecimal("3000000.00");
        RICession cession = ReinsuranceService.calculateCession(period, agreement, largeExposure);

        assertNotNull(cession);
        assertTrue(cession.isRequiresFacultative(), "Risk exposure exceeding treaty limit must trigger facultative placement");
    }
}
