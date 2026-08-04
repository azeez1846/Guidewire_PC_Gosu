package com.guidewire.ig.address.controller;

import com.guidewire.ig.address.dto.AddressLookupRequest;
import com.guidewire.ig.address.dto.AddressValidationResponse;
import com.guidewire.ig.address.service.ExternalAddressVendorConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

@RestController
@RequestMapping("/api/ig/v1/address")
public class AddressStandardizationIGController {
    private static final Logger LOGGER = Logger.getLogger(AddressStandardizationIGController.class.getName());


    private final ExternalAddressVendorConnector connector;

    @Autowired
    public AddressStandardizationIGController(ExternalAddressVendorConnector connector) {
        LOGGER.log(Level.FINE, "→ AddressStandardizationIGController.AddressStandardizationIGController");
        this.connector = connector;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        LOGGER.log(Level.FINE, "→ AddressStandardizationIGController.checkHealth");
        return ResponseEntity.ok(Map.of(
            "service", "addressstandardization_IG",
            "status", "UP",
            "gatewayVersion", "1.0.0"
        ));
    }

    @PostMapping("/standardize")
    public ResponseEntity<AddressValidationResponse> standardizeAddress(@RequestBody AddressLookupRequest req) {
        LOGGER.log(Level.FINE, "→ AddressStandardizationIGController.standardizeAddress");
        AddressValidationResponse response = connector.performOutboundAddressStandardization(req);
        return ResponseEntity.ok(response);
    }
}
