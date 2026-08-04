package com.guidewire.ig.vehicledetails.client;

import com.guidewire.ig.vehicledetails.dto.VehicleDetailsResponse;
import com.guidewire.ig.vehicledetails.dto.VehicleLookupRequest;
import com.guidewire.ig.vehicledetails.service.ExternalVendorMVRConnector;

/**
 * Guidewire Cloud Integration Gateway SDK Client
 * Shipped inside vehicledetails_IG-1.0.0.jar to enable seamless integration.
 */
public class VehicleDetailsIGClient {
    private static final VehicleDetailsIGClient instance = new VehicleDetailsIGClient();
    private final ExternalVendorMVRConnector connector;

    private VehicleDetailsIGClient() {
        this.connector = new ExternalVendorMVRConnector();
    }

    public static VehicleDetailsIGClient getInstance() {
        return instance;
    }

    /**
     * Executes vehicle & MVR vendor integration lookup via the Integration Gateway
     */
    public VehicleDetailsResponse executeGatewayLookup(VehicleLookupRequest request) {
        if (request == null) {
            request = new VehicleLookupRequest();
        }
        return connector.performOutboundVendorLookup(request);
    }
}
