package com.guidewire.pc;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyForm;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.UWIssue;
import com.guidewire.pc.service.PolicyFormInferenceEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Policy Form Inference & Mandatory Document Attachment Engine Tests")
public class PolicyFormInferenceEngineTest {

    @Test
    @DisplayName("Should infer Commercial Auto mandatory forms plus California Statutory Notice form")
    public void testCommercialAutoFormInference() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-FORM-101");
        period.setProductCode(PCConstants.PRODUCT_COMMERCIAL_AUTO);
        period.setBaseState("CA");

        List<PolicyForm> forms = PolicyFormInferenceEngine.inferPolicyForms(period);

        assertNotNull(forms);
        assertTrue(forms.size() >= 3);
        assertEquals("IL 00 17", forms.get(0).getFormNumber());
        assertEquals("IL 00 21", forms.get(1).getFormNumber());
        assertEquals("CA 00 01", forms.get(2).getFormNumber());
    }

    @Test
    @DisplayName("Should attach Terrorism TRIA Disclosure form when UW Issues exist")
    public void testTRIAFormInferenceWithUWIssues() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-FORM-202");
        period.setProductCode(PCConstants.PRODUCT_GENERAL_LIABILITY);
        period.setBaseState("NY");
        period.getUwIssues().add(new UWIssue("UW_HIGH_LIMIT", "HighLimit", "High limit approval required", "BlockingQuote", "Level3"));

        List<PolicyForm> forms = PolicyFormInferenceEngine.inferPolicyForms(period);

        assertNotNull(forms);
        assertTrue(forms.stream().anyMatch(f -> "IL 09 85".equals(f.getFormNumber())));
    }
}
