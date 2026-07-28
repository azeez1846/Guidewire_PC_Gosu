package com.guidewire.pc.orm;

public interface KeyableBean {
    Long getID();
    void setID(Long id);
    boolean isNew();
}
