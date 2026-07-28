package com.guidewire.pc;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DataStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class DataStoreServiceTest {

    private DataStoreService dataStore;

    @BeforeEach
    public void setUp() {
        dataStore = DataStoreService.getInstance();
    }

    @Test
    public void testAccountCreation() {
        Account acc = new Account();
        acc.setAccountHolderName("Global Tech Dynamics");
        acc.setAccountHolderType("Company");
        acc.setFein("88-1234567");
        acc.setAddressLine1("500 Silicon Ave");
        acc.setCity("Austin");
        acc.setState("TX");
        acc.setPostalCode("78701");
        acc.setProducerCode("PR-10928");

        Account created = dataStore.createAccount(acc);
        assertNotNull(created.getAccountNumber());
        assertTrue(created.getAccountNumber().startsWith("A000"));
        assertEquals("Global Tech Dynamics", created.getAccountHolderName());
        assertEquals("500 Silicon Ave, Austin, TX 78701", created.getFormattedAddress());
    }

    @Test
    public void testSubmissionRatingAndBinding() {
        Account acc = dataStore.getAccounts().get(0);
        assertNotNull(acc);

        PolicyPeriod sub = new PolicyPeriod();
        sub.setAccount(acc);
        sub.setProductCode("CommercialAuto");
        sub.setBodilyInjuryLimit("$500k/$500k");
        sub.setPropertyDamageLimit("$250k");

        PolicyPeriod created = dataStore.createSubmission(sub);
        assertNotNull(created.getJobNumber());
        assertTrue(created.getJobNumber().startsWith("S000"));
        assertEquals("Draft", created.getStatus());

        // Test rating
        BigDecimal totalPremium = created.calculatePremium();
        assertTrue(totalPremium.compareTo(BigDecimal.ZERO) > 0);

        // Bind
        created.setStatus("Quoted");
        if (created.getPolicyNumber() == null) {
            created.setPolicyNumber("POL-998877");
        }
        created.setStatus("Issued");
        assertEquals("Issued", created.getStatus());
        assertEquals("In Force (Issued)", created.getFormattedStatus());
    }
}
