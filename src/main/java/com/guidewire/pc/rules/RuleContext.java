package com.guidewire.pc.rules;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.Activity;
import com.guidewire.pc.model.PolicyPeriod;

import java.util.ArrayList;
import java.util.List;

public class RuleContext {
    private final PolicyPeriod policyPeriod;
    private final Account account;
    private final Activity activity;

    private final List<String> errorMessages = new ArrayList<>();
    private final List<String> warningMessages = new ArrayList<>();
    private final List<String> infoMessages = new ArrayList<>();
    private boolean underwritingHoldRequired = false;

    public RuleContext(PolicyPeriod policyPeriod) {
        this.policyPeriod = policyPeriod;
        this.account = policyPeriod != null ? policyPeriod.getAccount() : null;
        this.activity = null;
    }

    public RuleContext(Account account) {
        this.policyPeriod = null;
        this.account = account;
        this.activity = null;
    }

    public RuleContext(Activity activity) {
        this.policyPeriod = null;
        this.account = null;
        this.activity = activity;
    }

    public PolicyPeriod getPolicyPeriod() { return policyPeriod; }
    public Account getAccount() { return account; }
    public Activity getActivity() { return activity; }

    public void addError(String message) {
        errorMessages.add(message);
    }

    public void addWarning(String message) {
        warningMessages.add(message);
    }

    public void addInfo(String message) {
        infoMessages.add(message);
    }

    public void triggerUnderwritingHold() {
        this.underwritingHoldRequired = true;
    }

    public boolean isUnderwritingHoldRequired() { return underwritingHoldRequired; }
    public List<String> getErrorMessages() { return errorMessages; }
    public List<String> getWarningMessages() { return warningMessages; }
    public List<String> getInfoMessages() { return infoMessages; }

    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }
}
