package com.guidewire.pc.service;

import com.guidewire.pc.model.Activity;
import com.guidewire.pc.model.FraudRiskScore;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.SIURiskSignal;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SIURiskScoringEngine {
    private static final Logger LOGGER = Logger.getLogger(SIURiskScoringEngine.class.getName());
    private static final SIURiskScoringEngine instance = new SIURiskScoringEngine();

    private final DataStoreService dataStore = DataStoreService.getInstance();

    private SIURiskScoringEngine() {
        LOGGER.log(Level.FINE, "→ SIURiskScoringEngine.SIURiskScoringEngine");}

    public static SIURiskScoringEngine getInstance() {
        LOGGER.log(Level.FINE, "→ SIURiskScoringEngine.getInstance");
        return instance;
    }

    /**
     * Evaluates policy period for fraud indicators and computes SIU Risk Score
     */
    public FraudRiskScore evaluatePolicyFraudRisk(PolicyPeriod period) {
        LOGGER.log(Level.FINE, "→ SIURiskScoringEngine.evaluatePolicyFraudRisk");
        if (period == null) return new FraudRiskScore();

        FraudRiskScore score = new FraudRiskScore(period.getPolicyNumber(), period.getJobNumber());

        // 1. FEIN / Identity Anomaly Check
        if (period.getAccount() != null) {
            String fein = period.getAccount().getFein();
            if (fein == null || fein.trim().isEmpty() || fein.length() < 9) {
                score.addRiskSignal(new SIURiskSignal(
                        "FRD_IDENTITY_ANOMALY",
                        "Identity",
                        "Account FEIN missing or incomplete format",
                        20
                ));
            }
        }

        // 2. Policy Change Velocity Check (>2 policy changes on account)
        if (period.getAccount() != null) {
            String accNum = period.getAccount().getAccountNumber();
            long changeCount = dataStore.getSubmissions().stream().filter(s ->
                    s.getAccount() != null && accNum.equalsIgnoreCase(s.getAccount().getAccountNumber()) &&
                    "PolicyChange".equalsIgnoreCase(s.getJobType())
            ).count();

            if (changeCount >= 2) {
                score.addRiskSignal(new SIURiskSignal(
                        "FRD_CHANGE_VELOCITY",
                        "PolicyVelocity",
                        "High frequency of policy changes (" + changeCount + " endorsements detected)",
                        25
                ));
            }
        }

        // 3. Garaging State vs Policy Base State Mismatch
        if (period.getAccount() != null && period.getAccount().getState() != null) {
            if (!period.getAccount().getState().equalsIgnoreCase(period.getBaseState())) {
                score.addRiskSignal(new SIURiskSignal(
                        "FRD_TERRITORY_MISMATCH",
                        "TerritoryMismatch",
                        "Account address state (" + period.getAccount().getState() + ") differs from Policy Base State (" + period.getBaseState() + ")",
                        15
                ));
            }
        }

        // 4. Backdated Endorsement Anomaly (>30 days backdated)
        if (period.getEditEffectiveDate() != null) {
            long diffMs = Math.abs(new Date().getTime() - period.getEditEffectiveDate().getTime());
            long daysBack = TimeUnit.MILLISECONDS.toDays(diffMs);
            if (daysBack > 30) {
                score.addRiskSignal(new SIURiskSignal(
                        "FRD_BACKDATED_ANOMALY",
                        "EndorsementDateAnomaly",
                        "Policy change effective date is backdated by " + daysBack + " days",
                        20
                ));
            }
        }

        // 5. Total Claims Loss History Check (> $25,000)
        if (period.getPolicyNumber() != null) {
            var claims = ClaimCenterService.getInstance().getClaimsForPolicy(period.getPolicyNumber());
            BigDecimal totalLosses = BigDecimal.ZERO;
            for (var c : claims) {
                if (c.getLossAmount() != null) {
                    totalLosses = totalLosses.add(c.getLossAmount());
                }
            }
            if (totalLosses.compareTo(new BigDecimal("25000.00")) > 0) {
                score.addRiskSignal(new SIURiskSignal(
                        "FRD_HIGH_LOSS_HISTORY",
                        "LossHistory",
                        "Prior claims incurred loss of $" + totalLosses + " exceeds SIU threshold $25,000",
                        30
                ));
            }
        }

        // Auto-Trigger SIU Referral Activity if Score >= 70
        if (score.isSiuHoldRequired()) {
            Activity siuAct = new Activity();
            siuAct.setSubject("SIU Fraud Referral Hold: " + score.getRiskTier());
            siuAct.setDescription("Policy " + period.getPolicyNumber() + " scored " + score.getTotalRiskScore() + " risk points. Referred to SIU Fraud Unit for mandatory investigation.");
            siuAct.setPriority("High");
            siuAct.setStatus("Open");
            siuAct.setRelatedAccountId(period.getAccount() != null ? period.getAccount().getAccountNumber() : "N/A");
            dataStore.createActivity(siuAct);

            LOGGER.log(Level.WARNING, "SIU Fraud Hold Triggered for policy {0} (Score: {1}, Tier: {2})",
                    new Object[]{period.getPolicyNumber(), score.getTotalRiskScore(), score.getRiskTier()});
        } else {
            LOGGER.log(Level.INFO, "SIU Fraud Score evaluated for policy {0}: {1} pts ({2})",
                    new Object[]{period.getPolicyNumber(), score.getTotalRiskScore(), score.getRiskTier()});
        }

        return score;
    }
}
