package com.guidewire.pc;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DocumentGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentGeneratorTest {

    private DocumentGenerator documentGenerator;

    @BeforeEach
    public void setUp() {
        documentGenerator = DocumentGenerator.getInstance();
    }

    @Test
    public void testPolicyBinderPdfGeneration() throws IOException {
        Account acc = new Account();
        acc.setAccountNumber("A0009999");
        acc.setAccountHolderName("Acme Enterprises");
        acc.setAddressLine1("100 Corporate Blvd");
        acc.setCity("San Francisco");
        acc.setState("CA");
        acc.setPostalCode("94105");

        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S0009999");
        period.setPolicyNumber("POL-112233");
        period.setProductCode("CommercialAuto");
        period.setStatus("Issued");
        period.setAccount(acc);
        period.setBasePremium(new BigDecimal("2500.00"));
        period.setTaxesAndFees(new BigDecimal("200.00"));
        period.setTotalPremium(new BigDecimal("2700.00"));

        byte[] pdfBytes = documentGenerator.generatePolicyBinderPdf(period);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // PDF header magic bytes %PDF-
        String pdfHeader = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", pdfHeader);
    }
}
