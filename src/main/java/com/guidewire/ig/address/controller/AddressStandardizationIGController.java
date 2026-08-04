package com.guidewire.ig.address.controller;

import com.guidewire.ig.address.dto.AddressLookupRequest;
import com.guidewire.ig.address.dto.AddressValidationResponse;
import com.guidewire.ig.address.service.ExternalAddressVendorConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ig/v1/address")
public class AddressStandardizationIGController {

    private final ExternalAddressVendorConnector connector;

    @Autowired
    public AddressStandardizationIGController(ExternalAddressVendorConnector connector) {
        this.connector = connector;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        return ResponseEntity.ok(Map.of(
            "service", "addressstandardization_IG",
            "status", "UP",
            "gatewayVersion", "1.0.0"
        ));
    }

    @PostMapping("/standardize")
    public ResponseEntity<AddressValidationResponse> standardizeAddress(@RequestBody AddressLookupRequest req) {
        AddressValidationResponse response = connector.performOutboundAddressStandardization(req);
        return ResponseEntity.ok(response);
    }
}
