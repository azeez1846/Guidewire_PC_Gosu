package com.guidewire.ig.telematics.client;

import com.guidewire.ig.telematics.dto.TelematicsLookupRequest;
import com.guidewire.ig.telematics.dto.TelematicsResponse;
import com.guidewire.ig.telematics.service.ExternalTelematicsVendorConnector;

public class TelematicsIGClient {
    private static final TelematicsIGClient instance = new TelematicsIGClient();
    private final ExternalTelematicsVendorConnector connector;

    private TelematicsIGClient() {
        this.connector = new ExternalTelematicsVendorConnector();
    }

    public static TelematicsIGClient getInstance() {
        return instance;
    }

    public TelematicsResponse ingestFleetTelematics(TelematicsLookupRequest request) {
        if (request == null) {
            request = new TelematicsLookupRequest();
        }
        return connector.performTelematicsIngestion(request);
    }
}
