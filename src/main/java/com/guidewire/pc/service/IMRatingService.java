package com.guidewire.pc.service;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.ScheduledEquipmentItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IMRatingService {
    private static final Logger LOGGER = Logger.getLogger(IMRatingService.class.getName());
    private static final IMRatingService instance = new IMRatingService();

    private IMRatingService() {}

    public static IMRatingService getInstance() {
        return instance;
    }

    /**
     * Rate Inland Marine Policy Period
     */
    public PolicyPeriod rateInlandMarine(PolicyPeriod period) {
        if (period == null) return null;

        BigDecimal totalEquipmentValue = BigDecimal.ZERO;
        BigDecimal calculatedPremium = BigDecimal.ZERO;

        if (period.getScheduledEquipmentItems().isEmpty()) {
            // Default baseline equipment item if empty
            period.addScheduledEquipmentItem(new ScheduledEquipmentItem(
                    1, "HeavyMachinery", "Commercial Excavator CAT 320", "SN-9481203", new BigDecimal("150000.00")
            ));
        }

        for (ScheduledEquipmentItem item : period.getScheduledEquipmentItems()) {
            BigDecimal itemVal = item.getStatedValue() != null ? item.getStatedValue() : BigDecimal.ZERO;
            totalEquipmentValue = totalEquipmentValue.add(itemVal);

            double rate = getEquipmentRate(item.getEquipmentType());
            double dedFactor = getDeductibleFactor(item.getDeductible());
            double coinsuranceFactor = item.getCoinsurancePercentage() >= 0.90 ? 0.95 : 1.00;

            BigDecimal itemPrem = itemVal.multiply(BigDecimal.valueOf(rate * dedFactor * coinsuranceFactor));
            calculatedPremium = calculatedPremium.add(itemPrem);
        }

        // Apply state tax (5%) and policy fee ($100)
        BigDecimal basePrem = calculatedPremium.setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxesFees = basePrem.multiply(new BigDecimal("0.05")).add(new BigDecimal("100.00")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPrem = basePrem.add(taxesFees);

        period.setBasePremium(basePrem);
        period.setTaxesAndFees(taxesFees);
        period.setTotalPremium(totalPrem);
        period.setProductCode(PCConstants.PRODUCT_INLAND_MARINE);

        LOGGER.log(Level.INFO, "Inland Marine Policy Rated: {0} (Items: {1}, Total Stated Value: ${2}, Total Premium: ${3})",
                new Object[]{period.getPolicyNumber(), period.getScheduledEquipmentItems().size(), totalEquipmentValue, totalPrem});

        return period;
    }

    private double getEquipmentRate(String type) {
        if (type == null) return 0.015;
        return switch (type.toLowerCase()) {
            case "heavymachinery" -> 0.015;
            case "mobiletools" -> 0.020;
            case "transitcargo" -> 0.012;
            case "medicalequipment" -> 0.018;
            case "solarpanels" -> 0.010;
            default -> 0.015;
        };
    }

    private double getDeductibleFactor(BigDecimal ded) {
        if (ded == null) return 1.00;
        int val = ded.intValue();
        if (val >= 5000) return 0.85;
        if (val >= 2500) return 0.90;
        if (val >= 1000) return 0.95;
        return 1.00;
    }
}
