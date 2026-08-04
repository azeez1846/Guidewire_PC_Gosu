package com.guidewire.pc.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClaimCenterService {
    private static final Logger LOGGER = Logger.getLogger(ClaimCenterService.class.getName());
    private static final ClaimCenterService instance = new ClaimCenterService();

    public static class Claim {
        private final String claimNumber;
        private final String policyNumber;
        private final String claimDate;
        private final String status; // Open, Closed, UnderInvestigation
        private final BigDecimal lossAmount;
        private final String description;

        public Claim(String claimNumber, String policyNumber, String claimDate, String status, BigDecimal lossAmount, String description) {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.Claim");
            this.claimNumber = claimNumber;
            this.policyNumber = policyNumber;
            this.claimDate = claimDate;
            this.status = status;
            this.lossAmount = lossAmount;
            this.description = description;
        }

        public String getClaimNumber() {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.getClaimNumber"); return claimNumber; }
        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.getPolicyNumber"); return policyNumber; }
        public String getClaimDate() {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.getClaimDate"); return claimDate; }
        public String getStatus() {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.getStatus"); return status; }
        public BigDecimal getLossAmount() {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.getLossAmount"); return lossAmount; }
        public String getDescription() {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.getDescription"); return description; }
    }

    private final Map<String, List<Claim>> claimsStore = new ConcurrentHashMap<>();

    private ClaimCenterService() {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.ClaimCenterService");
        seedSampleClaims();
    }

    public static ClaimCenterService getInstance() {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.getInstance");
        return instance;
    }

    private void seedSampleClaims() {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.seedSampleClaims");
        List<Claim> list = new ArrayList<>();
        list.add(new Claim("CLM-9001", "POL-849102", "2026-03-15", "Open", new BigDecimal("12500.00"), "Commercial Fleet Collision on I-80"));
        list.add(new Claim("CLM-9002", "POL-849102", "2025-11-20", "Closed", new BigDecimal("3200.00"), "Windshield Property Damage"));
        claimsStore.put("POL-849102", list);
    }

    public List<Claim> getClaimsForPolicy(String policyNumber) {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.getClaimsForPolicy");
        if (policyNumber == null) return Collections.emptyList();
        return claimsStore.getOrDefault(policyNumber.toUpperCase(), Collections.emptyList());
    }

    public Claim reportFnol(String policyNumber, String description, BigDecimal amount) {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.reportFnol");
        String claimNum = "CLM-" + (System.currentTimeMillis() % 8999 + 1000);
        Claim newClaim = new Claim(claimNum, policyNumber, "2026-07-31", "Open", amount, description);
        claimsStore.computeIfAbsent(policyNumber != null ? policyNumber.toUpperCase() : "UNKNOWN", k -> new ArrayList<>()).add(newClaim);
        LOGGER.log(Level.INFO, "FNOL Claim reported: {0} for policy: {1} Amount: {2}", new Object[]{claimNum, policyNumber, amount});
        return newClaim;
    }

    public BigDecimal getTotalOpenLossForPolicy(String policyNumber) {
        LOGGER.log(Level.FINE, "→ ClaimCenterService.getTotalOpenLossForPolicy");
        List<Claim> claims = getClaimsForPolicy(policyNumber);
        BigDecimal total = BigDecimal.ZERO;
        for (Claim c : claims) {
            if ("Open".equalsIgnoreCase(c.getStatus()) && c.getLossAmount() != null) {
                total = total.add(c.getLossAmount());
            }
        }
        return total;
    }
}
