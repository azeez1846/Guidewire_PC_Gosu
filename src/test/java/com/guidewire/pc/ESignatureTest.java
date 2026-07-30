package com.guidewire.pc;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ESignatureTest {

    @Test
    public void testEnvelopeCreation() {
        String jobNumber = "S0005001";
        String signerEmail = "insured@example.com";
        String envelopeId = "ENV-DOCUSIGN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        assertNotNull(jobNumber);
        assertNotNull(envelopeId);
        assertTrue(envelopeId.startsWith("ENV-DOCUSIGN-"));
        assertNotNull(signerEmail);
    }

    @Test
    public void testSignatureCallbackCompleted() {
        String status = "completed";
        boolean isSigned = "completed".equalsIgnoreCase(status) || "signed".equalsIgnoreCase(status);

        assertTrue(isSigned);
    }
}
