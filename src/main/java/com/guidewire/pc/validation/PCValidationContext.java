package com.guidewire.pc.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PCValidationContext {
    private static final Logger LOGGER = Logger.getLogger(PCValidationContext.class.getName());

    private final String targetLevel;
    private final List<ValidationIssue> issues = new ArrayList<>();

    public PCValidationContext(String targetLevel) {
        LOGGER.log(Level.FINE, "→ PCValidationContext.PCValidationContext");
        this.targetLevel = targetLevel;
    }

    public String getTargetLevel() {
        LOGGER.log(Level.FINE, "→ PCValidationContext.getTargetLevel");
        return targetLevel;
    }

    public void addError(Object entity, String level, String message) {
        LOGGER.log(Level.FINE, "→ PCValidationContext.addError");
        issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, level, message, entity));
    }

    public void addWarning(Object entity, String level, String message) {
        LOGGER.log(Level.FINE, "→ PCValidationContext.addWarning");
        issues.add(new ValidationIssue(ValidationIssue.Severity.WARNING, level, message, entity));
    }

    public boolean hasErrors() {
        LOGGER.log(Level.FINE, "→ PCValidationContext.hasErrors");
        return issues.stream().anyMatch(i -> i.getSeverity() == ValidationIssue.Severity.ERROR);
    }

    public List<ValidationIssue> getErrors() {
        LOGGER.log(Level.FINE, "→ PCValidationContext.getErrors");
        return issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.ERROR).collect(Collectors.toList());
    }

    public List<ValidationIssue> getWarnings() {
        LOGGER.log(Level.FINE, "→ PCValidationContext.getWarnings");
        return issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.WARNING).collect(Collectors.toList());
    }

    public List<ValidationIssue> getAllIssues() {
        LOGGER.log(Level.FINE, "→ PCValidationContext.getAllIssues");
        return new ArrayList<>(issues);
    }
}
