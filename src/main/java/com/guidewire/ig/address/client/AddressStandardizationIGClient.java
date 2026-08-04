package com.guidewire.ig.address.client;

import com.guidewire.ig.address.dto.AddressLookupRequest;
import com.guidewire.ig.address.dto.AddressValidationResponse;
import com.guidewire.ig.address.service.ExternalAddressVendorConnector;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Guidewire Cloud Integration Gateway Address Standardization SDK Client
 * Shipped inside addressstandardization_IG-1.0.0.jar.
 */
public class AddressStandardizationIGClient {
    private static final Logger LOGGER = Logger.getLogger(AddressStandardizationIGClient.class.getName());

    private static final AddressStandardizationIGClient instance = new AddressStandardizationIGClient();
    private final ExternalAddressVendorConnector connector;

    private AddressStandardizationIGClient() {
        LOGGER.log(Level.FINE, "→ AddressStandardizationIGClient.AddressStandardizationIGClient");
        this.connector = new ExternalAddressVendorConnector();
    }

    public static AddressStandardizationIGClient getInstance() {
        LOGGER.log(Level.FINE, "→ AddressStandardizationIGClient.getInstance");
        return instance;
    }

    /**
     * Standardizes street addresses via the Integration Gateway layer
     */
    public AddressValidationResponse executeAddressStandardization(AddressLookupRequest request) {
        LOGGER.log(Level.FINE, "→ AddressStandardizationIGClient.executeAddressStandardization");
        if (request == null) {
            request = new AddressLookupRequest();
        }
        return connector.performOutboundAddressStandardization(request);
    }
}
