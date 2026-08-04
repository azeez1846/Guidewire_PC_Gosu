package com.guidewire.ig.telematics.client;

import com.guidewire.ig.telematics.dto.TelematicsLookupRequest;
import com.guidewire.ig.telematics.dto.TelematicsResponse;
import com.guidewire.ig.telematics.service.ExternalTelematicsVendorConnector;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TelematicsIGClient {
    private static final Logger LOGGER = Logger.getLogger(TelematicsIGClient.class.getName());

    private static final TelematicsIGClient instance = new TelematicsIGClient();
    private final ExternalTelematicsVendorConnector connector;

    private TelematicsIGClient() {
        LOGGER.log(Level.FINE, "→ TelematicsIGClient.TelematicsIGClient");
        this.connector = new ExternalTelematicsVendorConnector();
    }

    public static TelematicsIGClient getInstance() {
        LOGGER.log(Level.FINE, "→ TelematicsIGClient.getInstance");
        return instance;
    }

    public TelematicsResponse ingestFleetTelematics(TelematicsLookupRequest request) {
        LOGGER.log(Level.FINE, "→ TelematicsIGClient.ingestFleetTelematics");
        if (request == null) {
            request = new TelematicsLookupRequest();
        }
        return connector.performTelematicsIngestion(request);
    }
}
