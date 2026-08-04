package com.guidewire.pc.orm;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FixedId<T extends KeyableBean> implements Serializable, Comparable<FixedId<T>> {
    private static final Logger LOGGER = Logger.getLogger(FixedId.class.getName());

    private static final long serialVersionUID = 1L;
    private final Long value;
    private final String entityTypeName;

    public FixedId(Long value, Class<T> entityClass) {
        LOGGER.log(Level.FINE, "→ FixedId.FixedId");
        this.value = value;
        this.entityTypeName = entityClass != null ? entityClass.getSimpleName() : "KeyableBean";
    }

    public FixedId(Long value, String entityTypeName) {
        LOGGER.log(Level.FINE, "→ FixedId.FixedId");
        this.value = value;
        this.entityTypeName = entityTypeName;
    }

    public Long getValue() {
        LOGGER.log(Level.FINE, "→ FixedId.getValue");
        return value;
    }

    public String getEntityTypeName() {
        LOGGER.log(Level.FINE, "→ FixedId.getEntityTypeName");
        return entityTypeName;
    }

    @Override
    public boolean equals(Object o) {
        LOGGER.log(Level.FINE, "→ FixedId.equals");
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FixedId<?> fixedId = (FixedId<?>) o;
        return Objects.equals(value, fixedId.value) && Objects.equals(entityTypeName, fixedId.entityTypeName);
    }

    @Override
    public int hashCode() {
        LOGGER.log(Level.FINE, "→ FixedId.hashCode");
        return Objects.hash(value, entityTypeName);
    }

    @Override
    public String toString() {
        LOGGER.log(Level.FINE, "→ FixedId.toString");
        return entityTypeName + ":" + value;
    }

    @Override
    public int compareTo(FixedId<T> o) {
        LOGGER.log(Level.FINE, "→ FixedId.compareTo");
        if (o == null) return 1;
        return this.value.compareTo(o.value);
    }
}
