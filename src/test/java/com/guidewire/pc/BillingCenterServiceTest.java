package com.guidewire.pc;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.BillingCenterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BillingCenter Payment & Schedule Engine Tests")
public class BillingCenterServiceTest {

    private PolicyPeriod period;

    @BeforeEach
    public void setUp() {
        Account account = new Account();
        account.setAccountNumber("A0001001");
        account.setAccountHolderName("Acme Global Logistics");

        period = new PolicyPeriod();
        period.setPolicyNumber("POL-BILL-90001");
        period.setAccount(account);
        period.setTotalPremium(new BigDecimal("2400.00"));
    }

    @Test
    @DisplayName("Generate FourPay Billing Schedule & Verify Installments")
    public void testGenerateFourPaySchedule() {
        BillingCenterService.BillingSchedule schedule = BillingCenterService.getInstance().generateSchedule(period, "FourPay");

        assertNotNull(schedule);
        assertEquals("POL-BILL-90001", schedule.getPolicyNumber());
        assertEquals("FourPay", schedule.getPlanName());
        assertEquals(4, schedule.getInstallments().size());

        // Installment #1 DownPayment (25%) = $600.00
        assertEquals(new BigDecimal("600.00"), schedule.getInstallments().get(0).getAmount());
        assertEquals("Paid", schedule.getInstallments().get(0).getStatus());

        // Remaining 3 installments = $600.00 each
        assertEquals(new BigDecimal("600.00"), schedule.getInstallments().get(1).getAmount());
        assertEquals("Pending", schedule.getInstallments().get(1).getStatus());
    }

    @Test
    @DisplayName("Apply Payment & Calculate Outstanding Balance")
    public void testApplyPaymentAndBalance() {
        BillingCenterService.BillingSchedule schedule = BillingCenterService.getInstance().generateSchedule(period, "FourPay");

        BigDecimal initialBalance = BillingCenterService.getInstance().calculateOutstandingBalance(schedule);
        assertEquals(new BigDecimal("1800.00"), initialBalance);

        boolean paymentSuccess = BillingCenterService.getInstance().applyPayment(schedule, 2, new BigDecimal("600.00"));
        assertTrue(paymentSuccess);

        BigDecimal updatedBalance = BillingCenterService.getInstance().calculateOutstandingBalance(schedule);
        assertEquals(new BigDecimal("1200.00"), updatedBalance);
    }

    @Test
    @DisplayName("Assess Late Fees on Past Due Installments")
    public void testLateFeeAssessment() {
        BillingCenterService.BillingSchedule schedule = BillingCenterService.getInstance().generateSchedule(period, "FourPay");

        boolean lateFeeApplied = BillingCenterService.getInstance().applyLateFees(schedule, 3, new BigDecimal("25.00"));
        assertTrue(lateFeeApplied);

        BillingCenterService.PaymentInstallment inst3 = schedule.getInstallments().get(2);
        assertEquals("PastDue", inst3.getStatus());
        assertEquals(new BigDecimal("625.00"), inst3.getAmount());
    }

    @Test
    @DisplayName("Generate HTML Invoice Markup")
    public void testGenerateInvoiceHtml() {
        BillingCenterService.BillingSchedule schedule = BillingCenterService.getInstance().generateSchedule(period, "FourPay");
        String html = BillingCenterService.getInstance().generateInvoiceHtml(period, schedule);

        assertNotNull(html);
        assertTrue(html.contains("Guidewire BillingCenter Invoice"));
        assertTrue(html.contains("POL-BILL-90001"));
        assertTrue(html.contains("Acme Global Logistics"));
    }
}
