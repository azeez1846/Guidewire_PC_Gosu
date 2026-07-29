package com.guidewire.pc.rules;

public interface GosuRule {
    String getName();
    String getDescription();
    boolean isApplicable(RuleContext context);
    void execute(RuleContext context);
}
