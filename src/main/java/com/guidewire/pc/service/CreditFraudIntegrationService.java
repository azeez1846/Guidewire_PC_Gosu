package com.guidewire.pc.service;

import com.guidewire.ig.credit.client.CreditFraudIGClient;
import com.guidewire.ig.credit.dto.CreditLookupRequest;
import com.guidewire.ig.credit.dto.CreditFraudResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Guidewire PolicyCenter Service connecting to the
 * Credit Scoring & OFAC Sanctions Integration Gateway (IG) Microservice (creditfraud_IG-1.0.0.jar).
 */
public class CreditFraudIntegrationService {
    private static final Logger LOGGER = Logger.getLogger(CreditFraudIntegrationService.class.getName());
    private static final CreditFraudIntegrationService instance = new CreditFraudIntegrationService();

    private CreditFraudIntegrationService() {}

    public static CreditFraudIntegrationService getInstance() {
        return instance;
    }

    public CreditFraudResponse executeCreditAndFraudLookup(String accountHolderName, String feinOrSsn, String orgType, String state) {
        LOGGER.log(Level.INFO, "[PolicyCenter Credit IG Bridge] Invoking Credit & OFAC IG JAR for: {0} (FEIN: {1})",
                new Object[]{accountHolderName, feinOrSsn});

        CreditLookupRequest request = new CreditLookupRequest(accountHolderName, feinOrSsn, orgType, state);
        return CreditFraudIGClient.getInstance().evaluateCreditAndFraud(request);
    }
}
