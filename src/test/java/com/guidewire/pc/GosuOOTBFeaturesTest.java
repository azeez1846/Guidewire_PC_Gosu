package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.Coverage;
import com.guidewire.pc.model.PolicyPeriod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class GosuOOTBFeaturesTest {

    @BeforeAll
    public static void setupGosuEngine() {
        GosuBridge.initGosuEngine(new File("."));
    }

    @Test
    public void testPolicyPeriodModel() {
        PolicyPeriod period = new PolicyPeriod();
        period.setProductCode("CommercialProperty");
        period.setBaseState("FL");
        period.setBodilyInjuryLimit("$1M/$1M");
        period.setTotalPremium(new BigDecimal("3500.00"));
        period.setPeriodStart(new Date(System.currentTimeMillis() - 86400000L * 30));
        period.setPeriodEnd(new Date(System.currentTimeMillis() + 86400000L * 335));

        assertEquals("CommercialProperty", period.getProductCode());
        assertEquals("FL", period.getBaseState());
        assertEquals("$1M/$1M", period.getBodilyInjuryLimit());
        assertEquals(new BigDecimal("3500.00"), period.getTotalPremium());
    }

    @Test
    public void testCoverageModel() {
        Coverage cov = new Coverage("PAAutoLiabilityCov", "Auto Liability Coverage", new BigDecimal("500000.00"), new BigDecimal("1000.00"));
        assertEquals("PAAutoLiabilityCov", cov.getPatternCode());
        assertEquals("Auto Liability Coverage", cov.getCoverageName());
        assertEquals(new BigDecimal("500000.00"), cov.getDirectLimit());
        assertEquals(new BigDecimal("1000.00"), cov.getDeductible());
    }

    @Test
    public void testAccountModel() {
        Account acc = new Account();
        acc.setAccountNumber("A0009999");
        acc.setAccountHolderName("Acme Logistics Inc.");
        acc.setAddressLine1("100 Enterprise Way");
        acc.setCity("San Jose");
        acc.setState("CA");
        acc.setPostalCode("95110");
        acc.setAccountStatus("Active");

        assertEquals("A0009999", acc.getAccountNumber());
        assertEquals("Acme Logistics Inc.", acc.getAccountHolderName());
        assertEquals("Active", acc.getAccountStatus());
    }
}
