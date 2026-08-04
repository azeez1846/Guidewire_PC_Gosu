package com.guidewire.pc.service;

import com.guidewire.ig.address.client.AddressStandardizationIGClient;
import com.guidewire.ig.address.dto.AddressLookupRequest;
import com.guidewire.ig.address.dto.AddressValidationResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Guidewire PolicyCenter Service connecting to the
 * Address Standardization Integration Gateway (IG) Microservice (addressstandardization_IG-1.0.0.jar).
 */
public class AddressStandardizationService {
    private static final Logger LOGGER = Logger.getLogger(AddressStandardizationService.class.getName());
    private static final AddressStandardizationService instance = new AddressStandardizationService();

    private AddressStandardizationService() {}

    public static AddressStandardizationService getInstance() {
        return instance;
    }

    /**
     * Standardizes street addresses and retrieves USPS DPV deliverability and geocoding details via the IG Layer.
     */
    public AddressValidationResponse executeAddressStandardization(String addressLine1, String addressLine2, String city, String state, String zip, String country) {
        LOGGER.log(Level.INFO, "[PolicyCenter Address IG Bridge] Invoking Address Standardization IG JAR for: {0}, {1}, {2} {3}",
                new Object[]{addressLine1, city, state, zip});

        AddressLookupRequest request = new AddressLookupRequest(addressLine1, addressLine2, city, state, zip, country);
        return AddressStandardizationIGClient.getInstance().executeAddressStandardization(request);
    }
}
