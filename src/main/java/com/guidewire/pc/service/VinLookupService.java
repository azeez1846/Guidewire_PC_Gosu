package com.guidewire.pc.service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class VinLookupService {
    private static final Logger LOGGER = Logger.getLogger(VinLookupService.class.getName());

    private static final VinLookupService INSTANCE = new VinLookupService();

    private VinLookupService() {
        LOGGER.log(Level.FINE, "→ VinLookupService.VinLookupService");}

    public static VinLookupService getInstance() {
        LOGGER.log(Level.FINE, "→ VinLookupService.getInstance");
        return INSTANCE;
    }

    public Map<String, Object> decodeVin(String vin) {
        LOGGER.log(Level.FINE, "→ VinLookupService.decodeVin");
        Map<String, Object> result = new HashMap<>();

        if (vin == null || vin.trim().length() != 17) {
            result.put("valid", false);
            result.put("error", "Invalid VIN format: Must be exactly 17 characters");
            return result;
        }

        String cleanVin = vin.trim().toUpperCase();
        result.put("vin", cleanVin);
        result.put("valid", true);

        String wmi = cleanVin.substring(0, 3);
        String yearCode = cleanVin.substring(9, 10);

        if (wmi.startsWith("1") || wmi.startsWith("4") || wmi.startsWith("5")) {
            result.put("country", "USA");
        } else if (wmi.startsWith("2")) {
            result.put("country", "Canada");
        } else if (wmi.startsWith("J")) {
            result.put("country", "Japan");
        } else {
            result.put("country", "Global");
        }

        int year;
        switch (yearCode) {
            case "R" -> year = 2024;
            case "S" -> year = 2025;
            case "T" -> year = 2026;
            default -> year = 2023;
        }
        result.put("modelYear", year);

        if (cleanVin.startsWith("1FA")) {
            result.put("make", "Ford");
            result.put("model", "F-150 Commercial SuperDuty");
            result.put("bodyStyle", "Pickup Truck");
            result.put("msrp", 48500.00);
            result.put("antiTheftDevice", "Factory Alarm & GPS Tracker");
            result.put("safetyScore", 92);
        } else if (cleanVin.startsWith("5YJ")) {
            result.put("make", "Tesla");
            result.put("model", "Model Y Fleet Edition");
            result.put("bodyStyle", "Electric SUV");
            result.put("msrp", 52900.00);
            result.put("antiTheftDevice", "Sentry Mode & Immobilizer");
            result.put("safetyScore", 98);
        } else {
            result.put("make", "Freightliner");
            result.put("model", "M2 106 Medium Duty");
            result.put("bodyStyle", "Box Truck");
            result.put("msrp", 75000.00);
            result.put("antiTheftDevice", "Ignition Cutoff");
            result.put("safetyScore", 88);
        }

        return result;
    }
}
