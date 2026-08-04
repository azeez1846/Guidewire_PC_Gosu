package com.guidewire.ig.vehicledetails.controller;

import com.guidewire.ig.vehicledetails.dto.VehicleDetailsResponse;
import com.guidewire.ig.vehicledetails.dto.VehicleLookupRequest;
import com.guidewire.ig.vehicledetails.service.ExternalVendorMVRConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

@RestController
@RequestMapping("/api/ig/v1/vehicle-details")
public class VehicleDetailsIGController {
    private static final Logger LOGGER = Logger.getLogger(VehicleDetailsIGController.class.getName());


    private final ExternalVendorMVRConnector vendorConnector;

    @Autowired
    public VehicleDetailsIGController(ExternalVendorMVRConnector vendorConnector) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsIGController.VehicleDetailsIGController");
        this.vendorConnector = vendorConnector;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        LOGGER.log(Level.FINE, "→ VehicleDetailsIGController.checkHealth");
        return ResponseEntity.ok(Map.of(
            "service", "vehicledetails_IG",
            "status", "UP",
            "gatewayVersion", "1.0.0"
        ));
    }

    @PostMapping("/lookup")
    public ResponseEntity<VehicleDetailsResponse> lookupVehicleDetails(@RequestBody VehicleLookupRequest req) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsIGController.lookupVehicleDetails");
        VehicleDetailsResponse response = vendorConnector.performOutboundVendorLookup(req);
        return ResponseEntity.ok(response);
    }
}
