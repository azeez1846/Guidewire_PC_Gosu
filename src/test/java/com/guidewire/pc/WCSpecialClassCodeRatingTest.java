package com.guidewire.pc;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.WCRatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WCSpecialClassCodeRatingTest {

    private PolicyPeriod wcPeriod;

    @BeforeEach
    public void setUp() {
        Account account = new Account();
        account.setAccountNumber("A0009999");
        account.setAccountHolderName("Acme Construction LLC");

        wcPeriod = new PolicyPeriod();
        wcPeriod.setJobNumber("S0009876");
        wcPeriod.setProductCode("WorkersComp");
        wcPeriod.setAccount(account);
        wcPeriod.setBaseState("CA");
    }

    @Test
    @DisplayName("Rate Standard Workers' Compensation Premium")
    public void testStandardWCRating() {
        BigDecimal payroll = new BigDecimal("250000.00");
        BigDecimal expMod = new BigDecimal("0.900");
        BigDecimal classRate = new BigDecimal("3.00");

        BigDecimal totalPrem = WCRatingService.rateWorkersComp(wcPeriod, payroll, expMod, classRate);

        assertNotNull(totalPrem);
        assertTrue(totalPrem.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(new BigDecimal("7290.00"), totalPrem);
    }

    @Test
    @DisplayName("Rate WC Premium with Special Class Code (Clerical / Executive) and OSHA Safety Discount")
    public void testSpecialClassCodeRating() {
        BigDecimal payroll = new BigDecimal("500000.00");
        BigDecimal expMod = new BigDecimal("1.000");
        BigDecimal classRate = new BigDecimal("4.00");
        String specialClassCode = "8810";
        BigDecimal specialClassPayroll = new BigDecimal("100000.00");
        BigDecimal specialClassRate = new BigDecimal("0.50");

        BigDecimal totalPrem = WCRatingService.rateWorkersCompWithSpecialClassCodes(
                wcPeriod, payroll, expMod, classRate, specialClassCode, specialClassPayroll, specialClassRate, true
        );

        assertNotNull(totalPrem);
        assertTrue(totalPrem.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(new BigDecimal("21033.00"), totalPrem);
    }

    @Test
    @DisplayName("Validate Workers' Compensation Line Rules")
    public void testWCLineValidation() {
        List<String> validErrors = WCRatingService.validateWorkersCompLine(wcPeriod, new BigDecimal("150000.00"), new BigDecimal("0.950"));
        assertTrue(validErrors.isEmpty(), "Valid WC period should produce no validation errors");

        PolicyPeriod invalidPeriod = new PolicyPeriod();
        invalidPeriod.setJobNumber("S0000000");
        invalidPeriod.setProductCode("WorkersComp");
        invalidPeriod.setBaseState(null);

        List<String> errors = WCRatingService.validateWorkersCompLineDetails(
                invalidPeriod, new BigDecimal("-500.00"), new BigDecimal("3.500"), "8810", null
        );

        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Estimated annual payroll must be greater than 0")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Experience Modifier")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("primary jurisdiction base state is required")));
    }
}
