package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.ProducerCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ProducerCommissionService {
    private static final Logger LOGGER = Logger.getLogger(ProducerCommissionService.class.getName());


    public static BigDecimal calculateCommission(PolicyPeriod period, ProducerCode producer) {
        LOGGER.log(Level.FINE, "→ ProducerCommissionService.calculateCommission");
        if (period == null || producer == null) return BigDecimal.ZERO;

        BigDecimal prem = period.getTotalPremium() != null ? period.getTotalPremium() : BigDecimal.ZERO;
        boolean isRenewal = "Renewal".equalsIgnoreCase(period.getJobType());
        BigDecimal rate = isRenewal ? producer.getRenewalCommissionRate() : producer.getNewBusinessCommissionRate();

        if (rate == null) return BigDecimal.ZERO;

        BigDecimal pct = rate.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal commission = prem.multiply(pct).setScale(2, RoundingMode.HALF_UP);

        return commission;
    }
}
