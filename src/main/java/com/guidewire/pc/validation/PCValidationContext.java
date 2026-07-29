package com.guidewire.pc.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PCValidationContext {
    private final String targetLevel;
    private final List<ValidationIssue> issues = new ArrayList<>();

    public PCValidationContext(String targetLevel) {
        this.targetLevel = targetLevel;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void addError(Object entity, String level, String message) {
        issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, level, message, entity));
    }

    public void addWarning(Object entity, String level, String message) {
        issues.add(new ValidationIssue(ValidationIssue.Severity.WARNING, level, message, entity));
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(i -> i.getSeverity() == ValidationIssue.Severity.ERROR);
    }

    public List<ValidationIssue> getErrors() {
        return issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.ERROR).collect(Collectors.toList());
    }

    public List<ValidationIssue> getWarnings() {
        return issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.WARNING).collect(Collectors.toList());
    }

    public List<ValidationIssue> getAllIssues() {
        return new ArrayList<>(issues);
    }
}
