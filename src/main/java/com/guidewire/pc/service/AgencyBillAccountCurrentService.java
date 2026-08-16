package com.guidewire.pc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AgencyBillAccountCurrentService {
    private static final Logger LOGGER = Logger.getLogger(AgencyBillAccountCurrentService.class.getName());
    private static final AgencyBillAccountCurrentService INSTANCE = new AgencyBillAccountCurrentService();

    public static AgencyBillAccountCurrentService getInstance() {
        return INSTANCE;
    }

    public static class AccountCurrentLineItem {
        public String policyNumber;
        public String namedInsured;
        public String transactionType; // NEW_BUSINESS, RENEWAL, ENDORSEMENT, CANCELLATION
        public BigDecimal grossWrittenPremium;
        public BigDecimal commissionRatePct; // e.g. 15.0%
        public BigDecimal agencyRetainedCommission;
        public BigDecimal netRemittanceToCarrier;

        public AccountCurrentLineItem(String polNum, String insured, String txnType, BigDecimal grossPrem, BigDecimal commRate) {
            this.policyNumber = polNum;
            this.namedInsured = insured;
            this.transactionType = txnType;
            this.grossWrittenPremium = grossPrem;
            this.commissionRatePct = commRate;

            BigDecimal rateDec = commRate.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
            this.agencyRetainedCommission = grossPrem.multiply(rateDec).setScale(2, RoundingMode.HALF_UP);
            this.netRemittanceToCarrier = grossPrem.subtract(this.agencyRetainedCommission);
        }
    }

    public static class AccountCurrentStatement {
        public String statementId;
        public String producerCode;
        public String agencyName;
        public String billingMonth;
        public String statementDueDate;
        public List<AccountCurrentLineItem> lineItems = new ArrayList<>();
        public BigDecimal totalGrossWrittenPremium = BigDecimal.ZERO;
        public BigDecimal totalAgencyCommission = BigDecimal.ZERO;
        public BigDecimal totalNetRemittancePayable = BigDecimal.ZERO;
        public String reconciliationStatus; // BALANCED_RECONCILED, DISCREPANCY_FLAGGED
        public String settlementNotes;
    }

    public AccountCurrentStatement generateAccountCurrent(String producerCode, String agencyName, String billingMonth, List<AccountCurrentLineItem> customItems) {
        LOGGER.log(Level.FINE, "→ AgencyBillAccountCurrentService.generateAccountCurrent");
        AccountCurrentStatement stmt = new AccountCurrentStatement();
        stmt.statementId = "ACCT-CURR-" + (System.currentTimeMillis() % 100000);
        stmt.producerCode = producerCode != null ? producerCode : "PR-WEST-901";
        stmt.agencyName = agencyName != null ? agencyName : "Pacific Coast Commercial Insurance Brokers Inc";
        stmt.billingMonth = billingMonth != null ? billingMonth : "2026-08";
        stmt.statementDueDate = "2026-09-15";

        if (customItems != null && !customItems.isEmpty()) {
            stmt.lineItems.addAll(customItems);
        } else {
            stmt.lineItems.add(new AccountCurrentLineItem("POL-CA-88910", "Apex Freight Logistics LLC", "NEW_BUSINESS", new BigDecimal("14500.00"), new BigDecimal("15.0")));
            stmt.lineItems.add(new AccountCurrentLineItem("POL-CP-88912", "Horizon Plaza Commercial Properties", "RENEWAL", new BigDecimal("28000.00"), new BigDecimal("12.5")));
            stmt.lineItems.add(new AccountCurrentLineItem("POL-GL-88915", "Vance Mechanical Contracting", "ENDORSEMENT", new BigDecimal("3200.00"), new BigDecimal("15.0")));
            stmt.lineItems.add(new AccountCurrentLineItem("POL-WC-88919", "Coastal Bay Dining Services", "CANCELLATION", new BigDecimal("-2100.00"), new BigDecimal("10.0")));
        }

        for (AccountCurrentLineItem item : stmt.lineItems) {
            stmt.totalGrossWrittenPremium = stmt.totalGrossWrittenPremium.add(item.grossWrittenPremium);
            stmt.totalAgencyCommission = stmt.totalAgencyCommission.add(item.agencyRetainedCommission);
            stmt.totalNetRemittancePayable = stmt.totalNetRemittancePayable.add(item.netRemittanceToCarrier);
        }

        stmt.reconciliationStatus = "BALANCED_RECONCILED";
        stmt.settlementNotes = "Monthly Agency Bill statement balanced. Net carrier remittance of $" +
                stmt.totalNetRemittancePayable + " due via Automated Clearing House (ACH) by " + stmt.statementDueDate + ".";

        return stmt;
    }

    public Map<String, Object> toMap(AccountCurrentStatement s) {
        Map<String, Object> map = new HashMap<>();
        map.put("statementId", s.statementId);
        map.put("producerCode", s.producerCode);
        map.put("agencyName", s.agencyName);
        map.put("billingMonth", s.billingMonth);
        map.put("statementDueDate", s.statementDueDate);
        map.put("lineItems", s.lineItems);
        map.put("totalGrossWrittenPremium", s.totalGrossWrittenPremium);
        map.put("totalAgencyCommission", s.totalAgencyCommission);
        map.put("totalNetRemittancePayable", s.totalNetRemittancePayable);
        map.put("reconciliationStatus", s.reconciliationStatus);
        map.put("settlementNotes", s.settlementNotes);
        map.put("status", "SUCCESS");
        return map;
    }
}
