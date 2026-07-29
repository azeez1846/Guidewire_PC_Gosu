package com.guidewire.pc;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.validation.PCValidationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationTest {

    @Test
    public void testDraftValidationFailureWithoutAccount() {
        PolicyPeriod period = new PolicyPeriod();
        period.setEffectiveDate("2026-01-01");
        PCValidationContext ctx = period.validate("Draft");

        assertTrue(ctx.hasErrors());
        assertEquals(1, ctx.getErrors().size());
        assertTrue(ctx.getErrors().get(0).getMessage().contains("Account is required"));
    }

    @Test
    public void testQuotationValidationWithCoverage() {
        Account account = new Account();
        account.setFein("12-3456789");

        PolicyPeriod period = new PolicyPeriod();
        period.setAccount(account);
        period.setEffectiveDate("2026-01-01");
        period.setProducerCode("PR-1001");
        period.setBaseState("CA");
        period.createCoverage("PAAutoLiabilityCov");

        PCValidationContext ctx = period.validate("Quotation");
        assertFalse(ctx.hasErrors(), "Quotation validation should pass when all required fields and coverages are provided");
    }

    @Test
    public void testBindValidationChecksFein() {
        Account accountWithoutFein = new Account();
        accountWithoutFein.setFein(null);

        PolicyPeriod period = new PolicyPeriod();
        period.setAccount(accountWithoutFein);
        period.setEffectiveDate("2026-01-01");
        period.setProducerCode("PR-1001");
        period.setBaseState("CA");
        period.createCoverage("PAAutoLiabilityCov");

        PCValidationContext ctx = period.validate("Bind");
        assertTrue(ctx.hasErrors());
        assertTrue(ctx.getErrors().stream().anyMatch(e -> e.getMessage().contains("FEIN")));
    }
}
