package com.guidewire.pc.model;

import com.guidewire.pc.orm.KeyableBean;
import com.guidewire.pc.orm.GosuORMSession;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PolicyForm implements KeyableBean {
    private static final Logger LOGGER = Logger.getLogger(PolicyForm.class.getName());

    private Long id;
    private String formNumber;
    private String formName;
    private String editionDate = "01 22";
    private boolean isMandatory = true;
    private String inferredState;
    private String formDescription;

    public PolicyForm() {
        this.id = GosuORMSession.getInstance().nextID();
        LOGGER.log(Level.FINE, "Created PolicyForm instance with ID={0}", this.id);
    }

    public PolicyForm(String formNumber, String formName, String editionDate, boolean isMandatory, String inferredState, String formDescription) {
        this();
        this.formNumber = formNumber;
        this.formName = formName;
        this.editionDate = editionDate != null ? editionDate : "01 22";
        this.isMandatory = isMandatory;
        this.inferredState = inferredState;
        this.formDescription = formDescription;
    }

    @Override
    public Long getID() { return id; }
    @Override
    public void setID(Long id) { this.id = id; }
    @Override
    public boolean isNew() { return id == null; }

    public String getFormNumber() { return formNumber; }
    public void setFormNumber(String formNumber) { this.formNumber = formNumber; }

    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }

    public String getEditionDate() { return editionDate; }
    public void setEditionDate(String editionDate) { this.editionDate = editionDate; }

    public boolean isMandatory() { return isMandatory; }
    public void setMandatory(boolean isMandatory) { this.isMandatory = isMandatory; }

    public String getInferredState() { return inferredState; }
    public void setInferredState(String inferredState) { this.inferredState = inferredState; }

    public String getFormDescription() { return formDescription; }
    public void setFormDescription(String formDescription) { this.formDescription = formDescription; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PolicyForm that)) return false;
        return java.util.Objects.equals(id, that.id) ||
                (formNumber != null && formNumber.equalsIgnoreCase(that.formNumber));
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : (formNumber != null ? formNumber.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "PolicyForm{" +
                "formNumber='" + formNumber + '\'' +
                ", name='" + formName + '\'' +
                ", mandatory=" + isMandatory +
                '}';
    }
}
