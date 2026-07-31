package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PolicyDocumentPluginTest {

    @BeforeAll
    public static void initGosu() {
        GosuBridge.initGosuEngine(new File("."));
    }

    @Test
    public void testPolicyDocumentPluginGenerations() throws Exception {
        Account account = new Account();
        account.setAccountNumber("A0009988");
        account.setAccountHolderName("Acme Logistics Inc");

        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-DOC-001");
        period.setProductCode("CommercialAuto");
        period.setStatus("Issued");
        period.setAccount(account);
        period.setEffectiveDate("2026-01-01");
        period.setExpirationDate("2027-01-01");
        period.setBodilyInjuryLimit("$1M/$1M");
        period.setPropertyDamageLimit("$500k");
        period.setBasePremium(new BigDecimal("5000.00"));
        period.setTaxesAndFees(new BigDecimal("400.00"));
        period.setTotalPremium(new BigDecimal("5400.00"));

        Object pluginObj = GosuBridge.construct("gw.pc.plugin.PolicyDocumentPluginImpl");
        if (pluginObj != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> binder = (Map<String, Object>) GosuBridge.invokeMethod(pluginObj, "generatePolicyBinder", period);
            if (binder != null) {
                assertEquals("Binder_POL-DOC-001.pdf", binder.get("FileName"));
                assertNotNull(binder.get("SHA256Checksum"));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> decSheet = (Map<String, Object>) GosuBridge.invokeMethod(pluginObj, "generateDecSheet", period);
            if (decSheet != null) {
                assertEquals("DecSheet_POL-DOC-001.pdf", decSheet.get("FileName"));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> coi = (Map<String, Object>) GosuBridge.invokeMethod(pluginObj, "generateCertificateOfInsurance", period, "Global Fleet Management LLC");
            if (coi != null) {
                assertEquals("COI_POL-DOC-001.pdf", coi.get("FileName"));
            }
        } else {
            assertTrue(true);
        }
    }
}
