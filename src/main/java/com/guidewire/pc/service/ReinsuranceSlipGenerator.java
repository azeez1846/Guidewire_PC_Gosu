package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ReinsuranceSlipGenerator {
    private static final Logger LOGGER = Logger.getLogger(ReinsuranceSlipGenerator.class.getName());
    private static final ReinsuranceSlipGenerator INSTANCE = new ReinsuranceSlipGenerator();

    public static ReinsuranceSlipGenerator getInstance() {
        return INSTANCE;
    }

    public static class ReinsuranceParticipant {
        public String reinsurerName;
        public String rating; // A.M. Best A++, A+
        public double sharePercentage;
        public BigDecimal cededPremium;
        public BigDecimal cedingCommission;
        public BigDecimal netPayable;

        public ReinsuranceParticipant(String reinsurerName, String rating, double sharePercentage, BigDecimal totalCededPrem, BigDecimal cedingCommPct) {
            this.reinsurerName = reinsurerName;
            this.rating = rating;
            this.sharePercentage = sharePercentage;
            this.cededPremium = totalCededPrem.multiply(new BigDecimal(sharePercentage / 100.0)).setScale(2, RoundingMode.HALF_UP);
            this.cedingCommission = this.cededPremium.multiply(cedingCommPct).setScale(2, RoundingMode.HALF_UP);
            this.netPayable = this.cededPremium.subtract(this.cedingCommission);
        }
    }

    public static class ReinsurancePlacementSlip {
        public String slipReferenceNumber;
        public String dateGenerated;
        public String policyNumber;
        public String namedInsured;
        public String lineOfBusiness;
        public String treatyType; // QUOTA_SHARE, EXCESS_OF_LOSS, FACULTATIVE_PER_RISK
        public BigDecimal grossPolicyLimit;
        public BigDecimal grossGrossPremium;
        public BigDecimal carrierRetentionLimit;
        public BigDecimal totalCededLimit;
        public BigDecimal totalCededPremium;
        public BigDecimal totalCedingCommission;
        public BigDecimal netCarrierPremium;
        public List<ReinsuranceParticipant> syndicateParticipants = new ArrayList<>();
    }

    public ReinsurancePlacementSlip generatePlacementSlip(PolicyPeriod period, String treatyType, BigDecimal policyLimit, BigDecimal grossPremium, double quotaShareCededPct) {
        LOGGER.log(Level.FINE, "→ ReinsuranceSlipGenerator.generatePlacementSlip");
        ReinsurancePlacementSlip slip = new ReinsurancePlacementSlip();
        slip.slipReferenceNumber = "RI-SLIP-" + System.currentTimeMillis() % 1000000;
        slip.dateGenerated = LocalDate.now().toString();
        slip.policyNumber = period != null && period.getPolicyNumber() != null ? period.getPolicyNumber() : "POL-COMM-PROP-8891";
        slip.namedInsured = period != null && period.getAccount() != null && period.getAccount().getAccountHolderName() != null ? period.getAccount().getAccountHolderName() : "Apex Commercial Enterprises LLC";
        slip.lineOfBusiness = period != null && period.getProductCode() != null ? period.getProductCode() : "CommercialProperty";
        slip.treatyType = treatyType != null ? treatyType : "QUOTA_SHARE";

        slip.grossPolicyLimit = policyLimit != null ? policyLimit : new BigDecimal("20000000.00");
        slip.grossGrossPremium = grossPremium != null ? grossPremium : new BigDecimal("65000.00");

        double cededPct = quotaShareCededPct > 0 ? quotaShareCededPct : 40.0;
        BigDecimal cededFraction = new BigDecimal(cededPct / 100.0);
        BigDecimal retainFraction = BigDecimal.ONE.subtract(cededFraction);

        slip.totalCededLimit = slip.grossPolicyLimit.multiply(cededFraction).setScale(2, RoundingMode.HALF_UP);
        slip.carrierRetentionLimit = slip.grossPolicyLimit.multiply(retainFraction).setScale(2, RoundingMode.HALF_UP);

        slip.totalCededPremium = slip.grossGrossPremium.multiply(cededFraction).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cedingCommRate = new BigDecimal("0.25"); // 25% ceding commission to ceding carrier
        slip.totalCedingCommission = slip.totalCededPremium.multiply(cedingCommRate).setScale(2, RoundingMode.HALF_UP);
        slip.netCarrierPremium = slip.grossGrossPremium.subtract(slip.totalCededPremium).add(slip.totalCedingCommission);

        // Syndicate participants
        slip.syndicateParticipants.add(new ReinsuranceParticipant("Swiss Reinsurance America Corp", "A.M. Best A+ (XV)", 45.0, slip.totalCededPremium, cedingCommRate));
        slip.syndicateParticipants.add(new ReinsuranceParticipant("Munich Reinsurance America Inc", "A.M. Best A++ (XV)", 35.0, slip.totalCededPremium, cedingCommRate));
        slip.syndicateParticipants.add(new ReinsuranceParticipant("Hannover Rück SE (Lloyd's Syndicate)", "A.M. Best A+ (XV)", 20.0, slip.totalCededPremium, cedingCommRate));

        return slip;
    }

    public Map<String, Object> toMap(ReinsurancePlacementSlip slip) {
        Map<String, Object> map = new HashMap<>();
        map.put("slipReferenceNumber", slip.slipReferenceNumber);
        map.put("dateGenerated", slip.dateGenerated);
        map.put("policyNumber", slip.policyNumber);
        map.put("namedInsured", slip.namedInsured);
        map.put("lineOfBusiness", slip.lineOfBusiness);
        map.put("treatyType", slip.treatyType);
        map.put("grossPolicyLimit", slip.grossPolicyLimit);
        map.put("grossGrossPremium", slip.grossGrossPremium);
        map.put("carrierRetentionLimit", slip.carrierRetentionLimit);
        map.put("totalCededLimit", slip.totalCededLimit);
        map.put("totalCededPremium", slip.totalCededPremium);
        map.put("totalCedingCommission", slip.totalCedingCommission);
        map.put("netCarrierPremium", slip.netCarrierPremium);
        map.put("syndicateParticipants", slip.syndicateParticipants);
        map.put("status", "SUCCESS");
        return map;
    }
}
