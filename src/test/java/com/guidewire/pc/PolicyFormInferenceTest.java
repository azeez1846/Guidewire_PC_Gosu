package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyForm;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.PolicyFormInferenceEngine;
import com.guidewire.pc.service.PolicyFormPackagePlugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 3: Policy Forms Inference & Document Packet Engine Tests")
public class PolicyFormInferenceTest {

    private PolicyPeriod policyPeriod;

    @BeforeEach
    public void setUp() {
        GosuBridge.initGosuEngine(new File("."));
        policyPeriod = new PolicyPeriod();
        policyPeriod.setPolicyNumber("POL-FORM-3003");
        policyPeriod.setProductCode("CommercialAuto");
        policyPeriod.setBaseState("FL");
        policyPeriod.setTotalPremium(new BigDecimal("12500.00"));
    }

    @Test
    @DisplayName("Test 1: Mandatory Common Policy Forms Inferred")
    public void testCommonMandatoryFormsInferred() {
        List<PolicyForm> forms = PolicyFormInferenceEngine.inferPolicyForms(policyPeriod);

        assertNotNull(forms);
        assertFalse(forms.isEmpty());
        assertTrue(forms.stream().anyMatch(f -> "IL 00 17".equals(f.getFormNumber()) && f.isMandatory()));
        assertTrue(forms.stream().anyMatch(f -> "IL 00 21".equals(f.getFormNumber()) && f.isMandatory()));
    }

    @Test
    @DisplayName("Test 2: Line of Business Specific Forms Inferred")
    public void testLineSpecificFormsInferred() {
        // Commercial Auto
        List<PolicyForm> autoForms = PolicyFormInferenceEngine.inferPolicyForms(policyPeriod);
        assertTrue(autoForms.stream().anyMatch(f -> "CA 00 01".equals(f.getFormNumber())));

        // Commercial Property
        PolicyPeriod propPeriod = new PolicyPeriod();
        propPeriod.setPolicyNumber("POL-FORM-PROP");
        propPeriod.setProductCode("CommercialProperty");
        List<PolicyForm> propForms = PolicyFormInferenceEngine.inferPolicyForms(propPeriod);
        assertTrue(propForms.stream().anyMatch(f -> "CP 00 10".equals(f.getFormNumber())));
    }

    @Test
    @DisplayName("Test 3: State-Specific Statutory Notices (FL, CA, TX)")
    public void testStateSpecificFormsInferred() {
        // FL Statutory Notice
        List<PolicyForm> flForms = PolicyFormInferenceEngine.inferPolicyForms(policyPeriod);
        assertTrue(flForms.stream().anyMatch(f -> "IL 01 02".equals(f.getFormNumber()) && "FL".equals(f.getInferredState())));

        // CA Statutory Notice
        PolicyPeriod caPeriod = new PolicyPeriod();
        caPeriod.setBaseState("CA");
        List<PolicyForm> caForms = PolicyFormInferenceEngine.inferPolicyForms(caPeriod);
        assertTrue(caForms.stream().anyMatch(f -> "IL 01 04".equals(f.getFormNumber()) && "CA".equals(f.getInferredState())));
    }

    @Test
    @DisplayName("Test 4: High Premium TRIA Endorsement Disclosure Inferred")
    public void testTRIAInferenceForHighPremium() {
        // High premium ($12.5k) -> TRIA inferred
        List<PolicyForm> highForms = PolicyFormInferenceEngine.inferPolicyForms(policyPeriod);
        assertTrue(highForms.stream().anyMatch(f -> "IL 09 85".equals(f.getFormNumber()) && !f.isMandatory()));

        // Low premium ($2.5k) -> TRIA NOT inferred
        PolicyPeriod lowPeriod = new PolicyPeriod();
        lowPeriod.setTotalPremium(new BigDecimal("2500.00"));
        List<PolicyForm> lowForms = PolicyFormInferenceEngine.inferPolicyForms(lowPeriod);
        assertFalse(lowForms.stream().anyMatch(f -> "IL 09 85".equals(f.getFormNumber())));
    }

    @Test
    @DisplayName("Test 5: Policy Packet Bundling & SHA-256 Checksum Generation")
    @SuppressWarnings("unchecked")
    public void testPolicyPacketBundlingAndChecksum() {
        List<PolicyForm> forms = PolicyFormInferenceEngine.inferPolicyForms(policyPeriod);

        Map<String, Object> packet = PolicyFormPackagePlugin.buildPolicyPacket(policyPeriod, forms);

        assertNotNull(packet);
        assertEquals("POL-FORM-3003", packet.get("PolicyNumber"));
        assertEquals(forms.size(), packet.get("TotalFormsCount"));
        assertTrue((int) packet.get("MandatoryFormsCount") > 0);
        assertNotNull(packet.get("TableOfContents"));
        List<String> toc = (List<String>) packet.get("TableOfContents");
        assertFalse(toc.isEmpty());

        String checksum = (String) packet.get("PacketChecksum");
        assertNotNull(checksum);
        assertEquals(64, checksum.length(), "SHA-256 checksum string should be exactly 64 hex characters");
    }
}
