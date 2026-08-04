package com.guidewire.pc.rules;
import java.util.logging.Logger;
import java.util.logging.Level;

public interface GosuRule {
    String getName();
    String getDescription();
    boolean isApplicable(RuleContext context);
    void execute(RuleContext context);
}
