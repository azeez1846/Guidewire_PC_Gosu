package com.guidewire.pc.service;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnderwritingDashboardService {
    private static final Logger LOGGER = Logger.getLogger(UnderwritingDashboardService.class.getName());
    private static final UnderwritingDashboardService instance = new UnderwritingDashboardService();

    public record UnderwritingKpis(
            int totalAccounts,
            int totalSubmissions,
            int totalBoundPolicies,
            BigDecimal directWrittenPremium,
            double overallLossRatioPercentage,
            double quoteConversionRatePercentage,
            Map<String, BigDecimal> premiumByLine,
            int openActivitiesCount
    ) {}

    private UnderwritingDashboardService() {}

    public static UnderwritingDashboardService getInstance() {
        return instance;
    }

    public UnderwritingKpis computeKpis() {
        DataStoreService dataStore = DataStoreService.getInstance();
        List<PolicyPeriod> submissions = dataStore.getSubmissions();
        int totalAccounts = dataStore.getAccountCount();
        int totalSubmissions = submissions.size();
        int openActivitiesCount = dataStore.getActivityCount();

        int boundCount = 0;
        BigDecimal dwp = BigDecimal.ZERO;
        Map<String, BigDecimal> dwpByLine = new HashMap<>();

        for (PolicyPeriod p : submissions) {
            String status = p.getStatus();
            if (PCConstants.STATUS_ISSUED.equalsIgnoreCase(status) || PCConstants.STATUS_BOUND.equalsIgnoreCase(status)) {
                boundCount++;
                BigDecimal prem = p.getTotalPremium() != null ? p.getTotalPremium() : BigDecimal.ZERO;
                dwp = dwp.add(prem);

                String line = p.getProductCode() != null ? p.getProductCode() : "Other";
                dwpByLine.put(line, dwpByLine.getOrDefault(line, BigDecimal.ZERO).add(prem));
            }
        }

        double conversionRate = totalSubmissions > 0 ? ((double) boundCount / (double) totalSubmissions) * 100.0 : 0.0;
        double simulatedLossRatio = 42.5; // OOTB benchmark Loss Ratio

        LOGGER.log(Level.INFO, "Computed Underwriting KPIs: DWP = ${0}, Bound = {1}, Conversion = {2}%",
                new Object[]{dwp, boundCount, String.format("%.1f", conversionRate)});

        return new UnderwritingKpis(
                totalAccounts,
                totalSubmissions,
                boundCount,
                dwp.setScale(2, RoundingMode.HALF_UP),
                simulatedLossRatio,
                Math.round(conversionRate * 10.0) / 10.0,
                dwpByLine,
                openActivitiesCount
        );
    }
}
