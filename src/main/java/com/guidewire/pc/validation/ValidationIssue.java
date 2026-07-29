package com.guidewire.pc.validation;

public class ValidationIssue {
    public enum Severity { ERROR, WARNING }

    private final Severity severity;
    private final String level;
    private final String message;
    private final Object entity;

    public ValidationIssue(Severity severity, String level, String message, Object entity) {
        this.severity = severity;
        this.level = level;
        this.message = message;
        this.entity = entity;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public Object getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return "[" + severity + " @ " + level + "] " + message;
    }
}
