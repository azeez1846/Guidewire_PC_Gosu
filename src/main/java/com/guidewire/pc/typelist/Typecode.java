package com.guidewire.pc.typelist;

import java.util.Objects;

public class Typecode implements TypeKey {
    private final String typelistName;
    private final String code;
    private final String displayName;
    private final int priority;
    private final String description;

    public Typecode(String typelistName, String code, String displayName, int priority, String description) {
        this.typelistName = typelistName;
        this.code = code;
        this.displayName = displayName != null ? displayName : code;
        this.priority = priority;
        this.description = description != null ? description : "";
    }

    @Override
    public String getTypelistName() {
        return typelistName;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeKey)) return false;
        TypeKey typeKey = (TypeKey) o;
        return Objects.equals(typelistName, typeKey.getTypelistName()) &&
                Objects.equals(code, typeKey.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(typelistName, code);
    }

    @Override
    public String toString() {
        return typelistName + "." + code;
    }
}
