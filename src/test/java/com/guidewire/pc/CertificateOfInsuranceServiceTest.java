package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.CertificateOfInsuranceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CertificateOfInsuranceServiceTest {

    @Test
    @DisplayName("Test ACORD 25 Certificate of Insurance PDF Generation")
    public void testAcord25PdfGeneration() throws IOException {
        CertificateOfInsuranceService service = CertificateOfInsuranceService.getInstance();
        assertNotNull(service);

        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-TEST-COI-991");
        period.setJobNumber("JOB-COI-1001");
        period.setProductCode("PersonalAuto");

        byte[] pdfBytes = service.generateAcord25CoiPdf(period, "City of San Francisco", "Primary & Non-Contributory AI");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "PDF bytes should be non-empty and formatted correctly");

        // Verify PDF Magic Bytes (%PDF-)
        String pdfHeader = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", pdfHeader, "Output binary must have valid PDF magic header");
    }
}
