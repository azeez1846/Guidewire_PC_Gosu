package com.guidewire.pc.orm;

import java.io.Serializable;
import java.util.Objects;

public class FixedId<T extends KeyableBean> implements Serializable, Comparable<FixedId<T>> {
    private static final long serialVersionUID = 1L;
    private final Long value;
    private final String entityTypeName;

    public FixedId(Long value, Class<T> entityClass) {
        this.value = value;
        this.entityTypeName = entityClass != null ? entityClass.getSimpleName() : "KeyableBean";
    }

    public FixedId(Long value, String entityTypeName) {
        this.value = value;
        this.entityTypeName = entityTypeName;
    }

    public Long getValue() {
        return value;
    }

    public String getEntityTypeName() {
        return entityTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FixedId<?> fixedId = (FixedId<?>) o;
        return Objects.equals(value, fixedId.value) && Objects.equals(entityTypeName, fixedId.entityTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, entityTypeName);
    }

    @Override
    public String toString() {
        return entityTypeName + ":" + value;
    }

    @Override
    public int compareTo(FixedId<T> o) {
        if (o == null) return 1;
        return this.value.compareTo(o.value);
    }
}
