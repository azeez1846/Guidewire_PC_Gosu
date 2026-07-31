package com.guidewire.pc.service;

import com.guidewire.pc.model.Cost;
import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class StructuredRatingEngine {
    private static final Logger LOGGER = Logger.getLogger(StructuredRatingEngine.class.getName());
    private static final StructuredRatingEngine instance = new StructuredRatingEngine();

    private StructuredRatingEngine() {}

    public static StructuredRatingEngine getInstance() {
        return instance;
    }

    public List<Cost> rateMultiLineStructured(PolicyPeriod period) {
        LOGGER.info("[Structured Rating Engine] Initiating concurrent Virtual Thread multi-line rating...");

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<List<Cost>> f1 = executor.submit(() -> {
                List<Cost> costs = new ArrayList<>();
                BigDecimal base = period.getBasePremium() != null ? period.getBasePremium() : new BigDecimal("2500.00");
                Cost c1 = new Cost();
                c1.setChargePattern("BasePremium");
                c1.setActualAmount(base);
                c1.setDescription("Base Line Premium");
                costs.add(c1);

                if (period.getBodilyInjuryLimit() != null) {
                    Cost c2 = new Cost();
                    c2.setChargePattern("BodilyInjuryCoverage");
                    c2.setActualAmount(new BigDecimal("350.00"));
                    c2.setDescription("Bodily Injury Limit: " + period.getBodilyInjuryLimit());
                    costs.add(c2);
                }
                return costs;
            });

            Future<List<Cost>> f2 = executor.submit(() -> {
                List<Cost> costs = new ArrayList<>();
                if (period.getCollisionDeductible() != null) {
                    Cost c = new Cost();
                    c.setChargePattern("CollisionCoverage");
                    c.setActualAmount(new BigDecimal("220.00"));
                    c.setDescription("Collision Deductible: " + period.getCollisionDeductible());
                    costs.add(c);
                }
                return costs;
            });

            Future<List<Cost>> f3 = executor.submit(() -> {
                List<Cost> costs = new ArrayList<>();
                Cost stateTax = new Cost();
                stateTax.setChargePattern("StateTax");
                stateTax.setActualAmount(new BigDecimal("125.00"));
                stateTax.setDescription("State Statutory Tax & Assessment");
                costs.add(stateTax);

                Cost fee = new Cost();
                fee.setChargePattern("PolicyFee");
                fee.setActualAmount(new BigDecimal("45.00"));
                fee.setDescription("Policy Origination Fee");
                costs.add(fee);
                return costs;
            });

            List<Cost> allCosts = new ArrayList<>();
            allCosts.addAll(f1.get());
            allCosts.addAll(f2.get());
            allCosts.addAll(f3.get());

            LOGGER.log(java.util.logging.Level.INFO, "[Structured Rating Engine] Completed Virtual Thread rating with {0} cost line items.", allCosts.size());
            return allCosts;
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "[Structured Rating Engine] Exception during execution: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
