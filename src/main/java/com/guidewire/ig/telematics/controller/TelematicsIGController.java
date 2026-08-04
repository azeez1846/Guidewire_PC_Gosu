package com.guidewire.ig.telematics.controller;

import com.guidewire.ig.telematics.dto.TelematicsLookupRequest;
import com.guidewire.ig.telematics.dto.TelematicsResponse;
import com.guidewire.ig.telematics.service.ExternalTelematicsVendorConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

@RestController
@RequestMapping("/api/ig/v1/telematics")
public class TelematicsIGController {
    private static final Logger LOGGER = Logger.getLogger(TelematicsIGController.class.getName());


    private final ExternalTelematicsVendorConnector connector;

    @Autowired
    public TelematicsIGController(ExternalTelematicsVendorConnector connector) {
        LOGGER.log(Level.FINE, "→ TelematicsIGController.TelematicsIGController");
        this.connector = connector;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        LOGGER.log(Level.FINE, "→ TelematicsIGController.checkHealth");
        return ResponseEntity.ok(Map.of(
            "service", "telematics_IG",
            "status", "UP",
            "gatewayVersion", "1.0.0"
        ));
    }

    @PostMapping("/ingest")
    public ResponseEntity<TelematicsResponse> ingestFleetTelematics(@RequestBody TelematicsLookupRequest req) {
        LOGGER.log(Level.FINE, "→ TelematicsIGController.ingestFleetTelematics");
        TelematicsResponse response = connector.performTelematicsIngestion(req);
        return ResponseEntity.ok(response);
    }
}
