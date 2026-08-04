package com.guidewire.ig.vehicledetails.controller;

import com.guidewire.ig.vehicledetails.dto.VehicleDetailsResponse;
import com.guidewire.ig.vehicledetails.dto.VehicleLookupRequest;
import com.guidewire.ig.vehicledetails.service.ExternalVendorMVRConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ig/v1/vehicle-details")
public class VehicleDetailsIGController {

    private final ExternalVendorMVRConnector vendorConnector;

    @Autowired
    public VehicleDetailsIGController(ExternalVendorMVRConnector vendorConnector) {
        this.vendorConnector = vendorConnector;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        return ResponseEntity.ok(Map.of(
            "service", "vehicledetails_IG",
            "status", "UP",
            "gatewayVersion", "1.0.0"
        ));
    }

    @PostMapping("/lookup")
    public ResponseEntity<VehicleDetailsResponse> lookupVehicleDetails(@RequestBody VehicleLookupRequest req) {
        VehicleDetailsResponse response = vendorConnector.performOutboundVendorLookup(req);
        return ResponseEntity.ok(response);
    }
}
