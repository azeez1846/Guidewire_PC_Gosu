package com.guidewire.ig.credit.controller;

import com.guidewire.ig.credit.dto.CreditLookupRequest;
import com.guidewire.ig.credit.dto.CreditFraudResponse;
import com.guidewire.ig.credit.service.ExternalCreditFraudVendorConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

@RestController
@RequestMapping("/api/ig/v1/credit-fraud")
public class CreditFraudIGController {
    private static final Logger LOGGER = Logger.getLogger(CreditFraudIGController.class.getName());


    private final ExternalCreditFraudVendorConnector connector;

    @Autowired
    public CreditFraudIGController(ExternalCreditFraudVendorConnector connector) {
        LOGGER.log(Level.FINE, "→ CreditFraudIGController.CreditFraudIGController");
        this.connector = connector;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        LOGGER.log(Level.FINE, "→ CreditFraudIGController.checkHealth");
        return ResponseEntity.ok(Map.of(
            "service", "creditfraud_IG",
            "status", "UP",
            "gatewayVersion", "1.0.0"
        ));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<CreditFraudResponse> evaluateCreditAndFraud(@RequestBody CreditLookupRequest req) {
        LOGGER.log(Level.FINE, "→ CreditFraudIGController.evaluateCreditAndFraud");
        CreditFraudResponse response = connector.performCreditAndFraudEvaluation(req);
        return ResponseEntity.ok(response);
    }
}
