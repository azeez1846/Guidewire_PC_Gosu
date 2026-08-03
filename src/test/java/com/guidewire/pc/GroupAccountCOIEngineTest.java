package com.guidewire.pc;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.GroupAccountCOIEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Group Account & Certificate of Insurance (COI) Engine Tests")
public class GroupAccountCOIEngineTest {

    @Test
    @DisplayName("Should issue Certificate of Insurance for General Contractor with Additional Insured")
    public void testIssueCOICertificate() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-COI-8801");
        period.setBodilyInjuryLimit("$1M/$2M");

        GroupAccountCOIEngine.CertificateOfInsurance coi = GroupAccountCOIEngine.getInstance()
                .issueCertificate(period, "Apex Construction Mgmt", "500 Builder Way, Austin, TX", true, true);

        assertNotNull(coi);
        assertTrue(coi.getCertificateNumber().startsWith("COI-"));
        assertEquals("Apex Construction Mgmt", coi.getCertificateHolderName());
        assertTrue(coi.isAdditionalInsured());
        assertTrue(coi.isWaiverOfSubrogation());
        assertFalse(GroupAccountCOIEngine.getInstance().getCertificateRegistry().isEmpty());
    }
}
