package com.guidewire.pc.validation;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ValidationIssue {
    private static final Logger LOGGER = Logger.getLogger(ValidationIssue.class.getName());

    public enum Severity { ERROR, WARNING }

    private final Severity severity;
    private final String level;
    private final String message;
    private final Object entity;

    public ValidationIssue(Severity severity, String level, String message, Object entity) {
        LOGGER.log(Level.FINE, "→ ValidationIssue.ValidationIssue");
        this.severity = severity;
        this.level = level;
        this.message = message;
        this.entity = entity;
    }

    public Severity getSeverity() {
        LOGGER.log(Level.FINE, "→ ValidationIssue.getSeverity");
        return severity;
    }

    public String getLevel() {
        LOGGER.log(Level.FINE, "→ ValidationIssue.getLevel");
        return level;
    }

    public String getMessage() {
        LOGGER.log(Level.FINE, "→ ValidationIssue.getMessage");
        return message;
    }

    public Object getEntity() {
        LOGGER.log(Level.FINE, "→ ValidationIssue.getEntity");
        return entity;
    }

    @Override
    public String toString() {
        LOGGER.log(Level.FINE, "→ ValidationIssue.toString");
        return "[" + severity + " @ " + level + "] " + message;
    }
}
