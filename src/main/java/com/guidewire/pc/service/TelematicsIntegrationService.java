package com.guidewire.pc.service;

import com.guidewire.ig.telematics.client.TelematicsIGClient;
import com.guidewire.ig.telematics.dto.TelematicsLookupRequest;
import com.guidewire.ig.telematics.dto.TelematicsResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Guidewire PolicyCenter Service connecting to the
 * Commercial IoT Telematics Integration Gateway (IG) Microservice (telematics_IG-1.0.0.jar).
 */
public class TelematicsIntegrationService {
    private static final Logger LOGGER = Logger.getLogger(TelematicsIntegrationService.class.getName());
    private static final TelematicsIntegrationService instance = new TelematicsIntegrationService();

    private TelematicsIntegrationService() {}

    public static TelematicsIntegrationService getInstance() {
        return instance;
    }

    public TelematicsResponse executeTelematicsIngestion(String fleetId, String accountNumber, Integer activeVehiclesCount) {
        LOGGER.log(Level.INFO, "[PolicyCenter Telematics IG Bridge] Invoking Telematics IG JAR for Fleet: {0} (Account: {1})",
                new Object[]{fleetId, accountNumber});

        TelematicsLookupRequest request = new TelematicsLookupRequest(fleetId, accountNumber, activeVehiclesCount);
        return TelematicsIGClient.getInstance().ingestFleetTelematics(request);
    }
}
