package com.guidewire.ig.credit.client;

import com.guidewire.ig.credit.dto.CreditLookupRequest;
import com.guidewire.ig.credit.dto.CreditFraudResponse;
import com.guidewire.ig.credit.service.ExternalCreditFraudVendorConnector;

public class CreditFraudIGClient {
    private static final CreditFraudIGClient instance = new CreditFraudIGClient();
    private final ExternalCreditFraudVendorConnector connector;

    private CreditFraudIGClient() {
        this.connector = new ExternalCreditFraudVendorConnector();
    }

    public static CreditFraudIGClient getInstance() {
        return instance;
    }

    public CreditFraudResponse evaluateCreditAndFraud(CreditLookupRequest request) {
        if (request == null) {
            request = new CreditLookupRequest();
        }
        return connector.performCreditAndFraudEvaluation(request);
    }
}
