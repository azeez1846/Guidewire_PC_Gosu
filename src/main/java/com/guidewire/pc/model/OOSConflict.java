package com.guidewire.pc.model;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OOSConflict implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(OOSConflict.class.getName());

    private String fieldName;
    private String slice1EffectiveDate;
    private String slice1Value;
    private String slice2EffectiveDate;
    private String slice2Value;
    private String resolutionStatus; // UNRESOLVED, FORWARD_MERGED, OVERRIDDEN

    public OOSConflict() {
        this.resolutionStatus = "UNRESOLVED";
    }

    public OOSConflict(String fieldName, String slice1EffectiveDate, String slice1Value, String slice2EffectiveDate, String slice2Value) {
        this();
        this.fieldName = fieldName;
        this.slice1EffectiveDate = slice1EffectiveDate;
        this.slice1Value = slice1Value;
        this.slice2EffectiveDate = slice2EffectiveDate;
        this.slice2Value = slice2Value;
        LOGGER.log(Level.FINE, "OOSConflict detected on field={0} ({1} vs {2})", new Object[]{fieldName, slice1Value, slice2Value});
    }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getSlice1EffectiveDate() { return slice1EffectiveDate; }
    public void setSlice1EffectiveDate(String slice1EffectiveDate) { this.slice1EffectiveDate = slice1EffectiveDate; }

    public String getSlice1Value() { return slice1Value; }
    public void setSlice1Value(String slice1Value) { this.slice1Value = slice1Value; }

    public String getSlice2EffectiveDate() { return slice2EffectiveDate; }
    public void setSlice2EffectiveDate(String slice2EffectiveDate) { this.slice2EffectiveDate = slice2EffectiveDate; }

    public String getSlice2Value() { return slice2Value; }
    public void setSlice2Value(String slice2Value) { this.slice2Value = slice2Value; }

    public String getResolutionStatus() { return resolutionStatus; }
    public void setResolutionStatus(String resolutionStatus) { this.resolutionStatus = resolutionStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OOSConflict that)) return false;
        return java.util.Objects.equals(fieldName, that.fieldName) &&
                java.util.Objects.equals(slice1EffectiveDate, that.slice1EffectiveDate) &&
                java.util.Objects.equals(slice2EffectiveDate, that.slice2EffectiveDate);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(fieldName, slice1EffectiveDate, slice2EffectiveDate);
    }

    @Override
    public String toString() {
        return "OOSConflict{" +
                "field='" + fieldName + '\'' +
                ", v1='" + slice1Value + '\'' +
                ", v2='" + slice2Value + '\'' +
                ", status='" + resolutionStatus + '\'' +
                '}';
    }
}
