package com.guidewire.pc.orm;
import java.util.logging.Logger;
import java.util.logging.Level;

public interface KeyableBean {
    Long getID();
    void setID(Long id);
    boolean isNew();
}
