package com.guidewire.pc.typelist;

import java.util.Objects;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Typecode implements TypeKey {
    private static final Logger LOGGER = Logger.getLogger(Typecode.class.getName());

    private final String typelistName;
    private final String code;
    private final String displayName;
    private final int priority;
    private final String description;

    public Typecode(String typelistName, String code, String displayName, int priority, String description) {
        LOGGER.log(Level.FINE, "→ Typecode.Typecode");
        this.typelistName = typelistName;
        this.code = code;
        this.displayName = displayName != null ? displayName : code;
        this.priority = priority;
        this.description = description != null ? description : "";
    }

    @Override
    public String getTypelistName() {
        LOGGER.log(Level.FINE, "→ Typecode.getTypelistName");
        return typelistName;
    }

    @Override
    public String getCode() {
        LOGGER.log(Level.FINE, "→ Typecode.getCode");
        return code;
    }

    @Override
    public String getDisplayName() {
        LOGGER.log(Level.FINE, "→ Typecode.getDisplayName");
        return displayName;
    }

    @Override
    public int getPriority() {
        LOGGER.log(Level.FINE, "→ Typecode.getPriority");
        return priority;
    }

    @Override
    public String getDescription() {
        LOGGER.log(Level.FINE, "→ Typecode.getDescription");
        return description;
    }

    @Override
    public boolean equals(Object o) {
        LOGGER.log(Level.FINE, "→ Typecode.equals");
        if (this == o) return true;
        if (!(o instanceof TypeKey)) return false;
        TypeKey typeKey = (TypeKey) o;
        return Objects.equals(typelistName, typeKey.getTypelistName()) &&
                Objects.equals(code, typeKey.getCode());
    }

    @Override
    public int hashCode() {
        LOGGER.log(Level.FINE, "→ Typecode.hashCode");
        return Objects.hash(typelistName, code);
    }

    @Override
    public String toString() {
        LOGGER.log(Level.FINE, "→ Typecode.toString");
        return typelistName + "." + code;
    }
}
