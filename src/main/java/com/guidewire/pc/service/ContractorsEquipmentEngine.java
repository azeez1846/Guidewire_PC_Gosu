package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ContractorsEquipmentEngine {
    private static final Logger LOGGER = Logger.getLogger(ContractorsEquipmentEngine.class.getName());
    private static final ContractorsEquipmentEngine INSTANCE = new ContractorsEquipmentEngine();

    public static ContractorsEquipmentEngine getInstance() {
        return INSTANCE;
    }

    public static class ContractorsEquipmentResult {
        public String policyNumber;
        public BigDecimal scheduledEquipmentLimit;
        public BigDecimal scheduledBasePremium;
        public BigDecimal rentedBorrowedEquipmentLimit;
        public BigDecimal rentedEquipmentPremium;
        public BigDecimal unscheduledToolsLimit;
        public BigDecimal unscheduledToolsPremium;
        public String valuationBasis; // AGREED_VALUE, REPLACEMENT_COST, ACTUAL_CASH_VALUE
        public boolean hasBoomOverloadEndorsement;
        public BigDecimal boomOverloadSurcharge;
        public BigDecimal deductible;
        public BigDecimal deductibleCreditOrCharge;
        public BigDecimal subtotalPremium;
        public BigDecimal totalEquipmentFloaterPremium;
    }

    public ContractorsEquipmentResult rateContractorsEquipment(PolicyPeriod period, BigDecimal scheduledLimit, BigDecimal rentedLimit,
                                                              BigDecimal unscheduledLimit, String valuation,
                                                              boolean boomOverload, BigDecimal deductible) {
        LOGGER.log(Level.FINE, "→ ContractorsEquipmentEngine.rateContractorsEquipment");
        ContractorsEquipmentResult res = new ContractorsEquipmentResult();
        res.policyNumber = period != null && period.getPolicyNumber() != null ? period.getPolicyNumber() : "POL-IM-EQUIP-8001";

        res.scheduledEquipmentLimit = scheduledLimit != null ? scheduledLimit : new BigDecimal("750000.00");
        res.rentedBorrowedEquipmentLimit = rentedLimit != null ? rentedLimit : new BigDecimal("150000.00");
        res.unscheduledToolsLimit = unscheduledLimit != null ? unscheduledLimit : new BigDecimal("50000.00");
        res.valuationBasis = valuation != null ? valuation : "REPLACEMENT_COST";
        res.hasBoomOverloadEndorsement = boomOverload;
        res.deductible = deductible != null ? deductible : new BigDecimal("2500.00");

        // 1. Scheduled Equipment ($1.65 per $100 value)
        BigDecimal schedUnits = res.scheduledEquipmentLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal schedPrem = schedUnits.multiply(new BigDecimal("1.65")).setScale(2, RoundingMode.HALF_UP);

        if ("REPLACEMENT_COST".equalsIgnoreCase(res.valuationBasis)) {
            schedPrem = schedPrem.multiply(new BigDecimal("1.12")).setScale(2, RoundingMode.HALF_UP); // 12% surcharge
        }
        res.scheduledBasePremium = schedPrem;

        // 2. Rented / Borrowed Equipment ($1.90 per $100 limit)
        BigDecimal rentedUnits = res.rentedBorrowedEquipmentLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        res.rentedEquipmentPremium = rentedUnits.multiply(new BigDecimal("1.90")).setScale(2, RoundingMode.HALF_UP);

        // 3. Unscheduled Miscellaneous Tools ($2.25 per $100 limit)
        BigDecimal toolUnits = res.unscheduledToolsLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        res.unscheduledToolsPremium = toolUnits.multiply(new BigDecimal("2.25")).setScale(2, RoundingMode.HALF_UP);

        BigDecimal baseCombined = res.scheduledBasePremium.add(res.rentedEquipmentPremium).add(res.unscheduledToolsPremium);

        // 4. Boom & Overload / Crane Collapse Endorsement (+20% surcharge on scheduled heavy gear)
        if (res.hasBoomOverloadEndorsement) {
            res.boomOverloadSurcharge = res.scheduledBasePremium.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        } else {
            res.boomOverloadSurcharge = BigDecimal.ZERO;
        }

        // 5. Deductible modifier ($2,500 is base. $1k = +10% surcharge, $5k = -12% credit, $10k = -20% credit)
        int dedInt = res.deductible.intValue();
        BigDecimal dedFactor = BigDecimal.ZERO;
        if (dedInt <= 1000) {
            dedFactor = baseCombined.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP); // +10% charge
        } else if (dedInt >= 10000) {
            dedFactor = baseCombined.multiply(new BigDecimal("-0.20")).setScale(2, RoundingMode.HALF_UP); // -20% credit
        } else if (dedInt >= 5000) {
            dedFactor = baseCombined.multiply(new BigDecimal("-0.12")).setScale(2, RoundingMode.HALF_UP); // -12% credit
        }
        res.deductibleCreditOrCharge = dedFactor;

        res.subtotalPremium = baseCombined.add(res.boomOverloadSurcharge).add(res.deductibleCreditOrCharge);
        // State inland marine assessment (+4%)
        BigDecimal taxes = res.subtotalPremium.multiply(new BigDecimal("0.04")).setScale(2, RoundingMode.HALF_UP);
        res.totalEquipmentFloaterPremium = res.subtotalPremium.add(taxes);

        return res;
    }

    public Map<String, Object> toMap(ContractorsEquipmentResult r) {
        Map<String, Object> map = new HashMap<>();
        map.put("policyNumber", r.policyNumber);
        map.put("scheduledEquipmentLimit", r.scheduledEquipmentLimit);
        map.put("scheduledBasePremium", r.scheduledBasePremium);
        map.put("rentedBorrowedEquipmentLimit", r.rentedBorrowedEquipmentLimit);
        map.put("rentedEquipmentPremium", r.rentedEquipmentPremium);
        map.put("unscheduledToolsLimit", r.unscheduledToolsLimit);
        map.put("unscheduledToolsPremium", r.unscheduledToolsPremium);
        map.put("valuationBasis", r.valuationBasis);
        map.put("hasBoomOverloadEndorsement", r.hasBoomOverloadEndorsement);
        map.put("boomOverloadSurcharge", r.boomOverloadSurcharge);
        map.put("deductible", r.deductible);
        map.put("deductibleCreditOrCharge", r.deductibleCreditOrCharge);
        map.put("subtotalPremium", r.subtotalPremium);
        map.put("totalEquipmentFloaterPremium", r.totalEquipmentFloaterPremium);
        map.put("status", "SUCCESS");
        return map;
    }
}
