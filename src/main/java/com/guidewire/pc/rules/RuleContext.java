package com.guidewire.pc.rules;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.Activity;
import com.guidewire.pc.model.PolicyPeriod;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class RuleContext {
    private static final Logger LOGGER = Logger.getLogger(RuleContext.class.getName());

    private final PolicyPeriod policyPeriod;
    private final Account account;
    private final Activity activity;

    private final List<String> errorMessages = new ArrayList<>();
    private final List<String> warningMessages = new ArrayList<>();
    private final List<String> infoMessages = new ArrayList<>();
    private boolean underwritingHoldRequired = false;

    public RuleContext(PolicyPeriod policyPeriod) {
        LOGGER.log(Level.FINE, "→ RuleContext.RuleContext");
        this.policyPeriod = policyPeriod;
        this.account = policyPeriod != null ? policyPeriod.getAccount() : null;
        this.activity = null;
    }

    public RuleContext(Account account) {
        LOGGER.log(Level.FINE, "→ RuleContext.RuleContext");
        this.policyPeriod = null;
        this.account = account;
        this.activity = null;
    }

    public RuleContext(Activity activity) {
        LOGGER.log(Level.FINE, "→ RuleContext.RuleContext");
        this.policyPeriod = null;
        this.account = null;
        this.activity = activity;
    }

    public PolicyPeriod getPolicyPeriod() {
        LOGGER.log(Level.FINE, "→ RuleContext.getPolicyPeriod"); return policyPeriod; }
    public Account getAccount() {
        LOGGER.log(Level.FINE, "→ RuleContext.getAccount"); return account; }
    public Activity getActivity() {
        LOGGER.log(Level.FINE, "→ RuleContext.getActivity"); return activity; }

    public void addError(String message) {
        LOGGER.log(Level.FINE, "→ RuleContext.addError");
        errorMessages.add(message);
    }

    public void addWarning(String message) {
        LOGGER.log(Level.FINE, "→ RuleContext.addWarning");
        warningMessages.add(message);
    }

    public void addInfo(String message) {
        LOGGER.log(Level.FINE, "→ RuleContext.addInfo");
        infoMessages.add(message);
    }

    public void triggerUnderwritingHold() {
        LOGGER.log(Level.FINE, "→ RuleContext.triggerUnderwritingHold");
        this.underwritingHoldRequired = true;
    }

    public boolean isUnderwritingHoldRequired() {
        LOGGER.log(Level.FINE, "→ RuleContext.isUnderwritingHoldRequired"); return underwritingHoldRequired; }
    public List<String> getErrorMessages() {
        LOGGER.log(Level.FINE, "→ RuleContext.getErrorMessages"); return errorMessages; }
    public List<String> getWarningMessages() {
        LOGGER.log(Level.FINE, "→ RuleContext.getWarningMessages"); return warningMessages; }
    public List<String> getInfoMessages() {
        LOGGER.log(Level.FINE, "→ RuleContext.getInfoMessages"); return infoMessages; }

    public boolean hasErrors() {
        LOGGER.log(Level.FINE, "→ RuleContext.hasErrors");
        return !errorMessages.isEmpty();
    }
}
