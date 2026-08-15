package com.guidewire.pc.model;

import com.guidewire.pc.orm.KeyableBean;
import com.guidewire.pc.orm.GosuORMSession;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Organization implements KeyableBean {
    private static final Logger LOGGER = Logger.getLogger(Organization.class.getName());

    private Long id;
    private String name;
    private String agencyFEIN;
    private String producerType = "Independent";
    private String address;

    public Organization() {
        this.id = GosuORMSession.getInstance().nextID();
        LOGGER.log(Level.FINE, "Organization created: ID={0}", this.id);
    }

    public Organization(String name, String agencyFEIN, String producerType, String address) {
        this();
        this.name = name;
        this.agencyFEIN = agencyFEIN;
        this.producerType = producerType != null ? producerType : "Independent";
        this.address = address;
    }

    @Override
    public Long getID() { return id; }
    @Override
    public void setID(Long id) { this.id = id; }
    @Override
    public boolean isNew() { return id == null; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAgencyFEIN() { return agencyFEIN; }
    public void setAgencyFEIN(String agencyFEIN) { this.agencyFEIN = agencyFEIN; }

    public String getProducerType() { return producerType; }
    public void setProducerType(String producerType) { this.producerType = producerType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
