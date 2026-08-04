package com.guidewire.ig.credit.client;

import com.guidewire.ig.credit.dto.CreditLookupRequest;
import com.guidewire.ig.credit.dto.CreditFraudResponse;
import com.guidewire.ig.credit.service.ExternalCreditFraudVendorConnector;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CreditFraudIGClient {
    private static final Logger LOGGER = Logger.getLogger(CreditFraudIGClient.class.getName());

    private static final CreditFraudIGClient instance = new CreditFraudIGClient();
    private final ExternalCreditFraudVendorConnector connector;

    private CreditFraudIGClient() {
        LOGGER.log(Level.FINE, "→ CreditFraudIGClient.CreditFraudIGClient");
        this.connector = new ExternalCreditFraudVendorConnector();
    }

    public static CreditFraudIGClient getInstance() {
        LOGGER.log(Level.FINE, "→ CreditFraudIGClient.getInstance");
        return instance;
    }

    public CreditFraudResponse evaluateCreditAndFraud(CreditLookupRequest request) {
        LOGGER.log(Level.FINE, "→ CreditFraudIGClient.evaluateCreditAndFraud");
        if (request == null) {
            request = new CreditLookupRequest();
        }
        return connector.performCreditAndFraudEvaluation(request);
    }
}
