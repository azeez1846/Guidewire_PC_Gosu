package com.guidewire.pc.rules;

import com.guidewire.pc.model.PolicyPeriod;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RulesEngine {
    private static final Logger LOGGER = Logger.getLogger(RulesEngine.class.getName());
    private static final RulesEngine instance = new RulesEngine();

    private final List<GosuRule> preQuoteRules = new ArrayList<>();
    private final List<GosuRule> preBindRules = new ArrayList<>();

    private RulesEngine() {
        registerDefaultRules();
    }

    public static RulesEngine getInstance() {
        return instance;
    }

    private void registerDefaultRules() {
        // Pre-Quote Rule 1: High Liability Limit UW Approval Rule
        preQuoteRules.add(new GosuRule() {
            @Override
            public String getName() { return "HighLiabilityLimitRule"; }

            @Override
            public String getDescription() { return "Requires Underwriting approval for bodily injury limits of $1M/$1M or higher."; }

            @Override
            public boolean isApplicable(RuleContext context) {
                return context.getPolicyPeriod() != null;
            }

            @Override
            public void execute(RuleContext context) {
                PolicyPeriod period = context.getPolicyPeriod();
                if ("$1M/$1M".equals(period.getBodilyInjuryLimit())) {
                    context.addWarning("High Liability Limit ($1M/$1M) selected - requires Underwriting review.");
                    context.triggerUnderwritingHold();
                }
            }
        });

        // Pre-Quote Rule 2: Minimum Term Verification
        preQuoteRules.add(new GosuRule() {
            @Override
            public String getName() { return "TermMonthsValidationRule"; }

            @Override
            public String getDescription() { return "Validates policy term months."; }

            @Override
            public boolean isApplicable(RuleContext context) {
                return context.getPolicyPeriod() != null;
            }

            @Override
            public void execute(RuleContext context) {
                PolicyPeriod period = context.getPolicyPeriod();
                if (period.getTermMonths() <= 0) {
                    context.addError("Policy term months must be greater than zero.");
                }
            }
        });

        // Pre-Quote Rule 3: Geospatial Catastrophe Risk Rule
        preQuoteRules.add(new GosuRule() {
            @Override
            public String getName() { return "GeospatialCatastropheRiskRule"; }

            @Override
            public String getDescription() { return "Triggers Underwriting Hold if base state has high Wildfire or Coastal Flood risk."; }

            @Override
            public boolean isApplicable(RuleContext context) {
                return context.getPolicyPeriod() != null && context.getPolicyPeriod().getBaseState() != null;
            }

            @Override
            public void execute(RuleContext context) {
                String state = context.getPolicyPeriod().getBaseState();
                if ("CA".equalsIgnoreCase(state)) {
                    context.addWarning("Geospatial Risk: High Wildfire Zone (Score 85/100) - Underwriting Hold applied.");
                    context.triggerUnderwritingHold();
                } else if ("FL".equalsIgnoreCase(state)) {
                    context.addWarning("Geospatial Risk: Coastal Special Flood Hazard Area (Zone A) - Special Deductible required.");
                    context.triggerUnderwritingHold();
                }
            }
        });

        // Pre-Bind Rule 1: Mandatory Producer Code Rule
        preBindRules.add(new GosuRule() {
            @Override
            public String getName() { return "MandatoryProducerCodeRule"; }

            @Override
            public String getDescription() { return "Ensures valid producer code before binding."; }

            @Override
            public boolean isApplicable(RuleContext context) {
                return context.getPolicyPeriod() != null;
            }

            @Override
            public void execute(RuleContext context) {
                PolicyPeriod period = context.getPolicyPeriod();
                if (period.getProducerCode() == null || period.getProducerCode().trim().isEmpty()) {
                    context.addError("Producer Code is mandatory before binding policy.");
                }
            }
        });

        // Pre-Bind Rule 2: Account Status Active Verification
        preBindRules.add(new GosuRule() {
            @Override
            public String getName() { return "ActiveAccountCheckRule"; }

            @Override
            public String getDescription() { return "Verifies account is active before binding."; }

            @Override
            public boolean isApplicable(RuleContext context) {
                return context.getPolicyPeriod() != null && context.getAccount() != null;
            }

            @Override
            public void execute(RuleContext context) {
                if (!"Active".equalsIgnoreCase(context.getAccount().getAccountStatus())) {
                    context.addError("Policy cannot be bound because linked Account is not Active.");
                }
            }
        });
    }

    public RuleContext evaluatePreQuoteRules(PolicyPeriod period) {
        RuleContext context = new RuleContext(period);
        LOGGER.log(java.util.logging.Level.INFO, "Evaluating Pre-Quote Rules for submission: {0}", period != null ? period.getJobNumber() : "null");
        for (GosuRule rule : preQuoteRules) {
            if (rule.isApplicable(context)) {
                rule.execute(context);
            }
        }
        return context;
    }

    public RuleContext evaluatePreBindRules(PolicyPeriod period) {
        RuleContext context = new RuleContext(period);
        LOGGER.log(java.util.logging.Level.INFO, "Evaluating Pre-Bind Rules for submission: {0}", period != null ? period.getJobNumber() : "null");
        for (GosuRule rule : preBindRules) {
            if (rule.isApplicable(context)) {
                rule.execute(context);
            }
        }
        return context;
    }
}
