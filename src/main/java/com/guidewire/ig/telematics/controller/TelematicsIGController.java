package com.guidewire.ig.telematics.controller;

import com.guidewire.ig.telematics.dto.TelematicsLookupRequest;
import com.guidewire.ig.telematics.dto.TelematicsResponse;
import com.guidewire.ig.telematics.service.ExternalTelematicsVendorConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ig/v1/telematics")
public class TelematicsIGController {

    private final ExternalTelematicsVendorConnector connector;

    @Autowired
    public TelematicsIGController(ExternalTelematicsVendorConnector connector) {
        this.connector = connector;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        return ResponseEntity.ok(Map.of(
            "service", "telematics_IG",
            "status", "UP",
            "gatewayVersion", "1.0.0"
        ));
    }

    @PostMapping("/ingest")
    public ResponseEntity<TelematicsResponse> ingestFleetTelematics(@RequestBody TelematicsLookupRequest req) {
        TelematicsResponse response = connector.performTelematicsIngestion(req);
        return ResponseEntity.ok(response);
    }
}
