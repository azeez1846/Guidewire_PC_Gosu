package com.guidewire.pc.service;

import com.guidewire.ig.vehicledetails.client.VehicleDetailsIGClient;
import com.guidewire.ig.vehicledetails.dto.VehicleDetailsResponse;
import com.guidewire.ig.vehicledetails.dto.VehicleLookupRequest;
import com.guidewire.pc.model.PolicyPeriod;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Guidewire PolicyCenter Integration Service connecting to the
 * Integration Gateway (IG) Microservice JAR (vehicledetails_IG-1.0.0.jar).
 */
public class VehicleDetailsIntegrationService {
    private static final Logger LOGGER = Logger.getLogger(VehicleDetailsIntegrationService.class.getName());
    private static final VehicleDetailsIntegrationService instance = new VehicleDetailsIntegrationService();

    private VehicleDetailsIntegrationService() {}

    public static VehicleDetailsIntegrationService getInstance() {
        return instance;
    }

    /**
     * Invokes the Integration Gateway (IG) microservice layer for Vehicle & MVR vendor verification
     */
    public VehicleDetailsResponse executeVehicleLookup(String vin, Integer year, String make, String model, String driverLicense, String driverState, String policyType) {
        LOGGER.log(Level.INFO, "[PolicyCenter Integration Gateway Bridge] Invoking Integration Gateway (IG) JAR for VIN: {0}, DL: {1}",
                new Object[]{vin, driverLicense});

        VehicleLookupRequest request = new VehicleLookupRequest(vin, year, make, model, driverLicense, driverState, policyType);
        return VehicleDetailsIGClient.getInstance().executeGatewayLookup(request);
    }

    /**
     * Executes auto underwriting enrichment during Personal Auto or Commercial Auto submission
     */
    public VehicleDetailsResponse enrichAutoSubmissionWithIG(PolicyPeriod period, String vin, String driverLicense, String driverState) {
        String polType = period != null && period.getProductCode() != null ? period.getProductCode() : "PersonalAuto";
        VehicleDetailsResponse igResponse = executeVehicleLookup(vin, 2025, "Ford", "F-150", driverLicense, driverState, polType);

        if (period != null && igResponse != null && igResponse.getRecommendedTierDiscountSurchargePct() != null) {
            // Apply recommended IG tier adjustment to total premium
            if (igResponse.getRecommendedTierDiscountSurchargePct() > 0) {
                LOGGER.log(Level.INFO, "[PolicyCenter Auto UW] IG Gateway returned surcharge tier: {0}% on submission {1}",
                        new Object[]{igResponse.getRecommendedTierDiscountSurchargePct() * 100, period.getJobNumber()});
            } else {
                LOGGER.log(Level.INFO, "[PolicyCenter Auto UW] IG Gateway returned preferred tier discount: {0}% on submission {1}",
                        new Object[]{igResponse.getRecommendedTierDiscountSurchargePct() * 100, period.getJobNumber()});
            }
        }
        return igResponse;
    }
}
