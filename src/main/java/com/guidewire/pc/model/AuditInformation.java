package com.guidewire.pc.model;

import com.guidewire.pc.orm.KeyableBean;
import com.guidewire.pc.orm.GosuORMSession;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuditInformation implements KeyableBean {
    private static final Logger LOGGER = Logger.getLogger(AuditInformation.class.getName());

    private Long id;
    private String auditType = "FinalAudit";
    private String auditStatus = "Draft";
    private String auditMethod = "Voluntary";
    private BigDecimal estimatedExposure = BigDecimal.ZERO;
    private BigDecimal auditedExposure = BigDecimal.ZERO;
    private BigDecimal auditPremiumAdjustment = BigDecimal.ZERO;
    private String auditDueDate;
    private String auditCompleteDate;

    public AuditInformation() {
        this.id = GosuORMSession.getInstance().nextID();
        LOGGER.log(Level.FINE, "AuditInformation initialized: ID={0}", this.id);
    }

    public AuditInformation(String auditType, String auditMethod, BigDecimal estimatedExposure) {
        this();
        this.auditType = auditType != null ? auditType : "FinalAudit";
        this.auditMethod = auditMethod != null ? auditMethod : "Voluntary";
        this.estimatedExposure = estimatedExposure != null ? estimatedExposure : BigDecimal.ZERO;
        this.auditedExposure = this.estimatedExposure;
    }

    @Override
    public Long getID() { return id; }
    @Override
    public void setID(Long id) { this.id = id; }
    @Override
    public boolean isNew() { return id == null; }

    public String getAuditType() { return auditType; }
    public void setAuditType(String auditType) { this.auditType = auditType; }

    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }

    public String getAuditMethod() { return auditMethod; }
    public void setAuditMethod(String auditMethod) { this.auditMethod = auditMethod; }

    public BigDecimal getEstimatedExposure() { return estimatedExposure; }
    public void setEstimatedExposure(BigDecimal estimatedExposure) { this.estimatedExposure = estimatedExposure; }

    public BigDecimal getAuditedExposure() { return auditedExposure; }
    public void setAuditedExposure(BigDecimal auditedExposure) { this.auditedExposure = auditedExposure; }

    public BigDecimal getAuditPremiumAdjustment() { return auditPremiumAdjustment; }
    public void setAuditPremiumAdjustment(BigDecimal auditPremiumAdjustment) { this.auditPremiumAdjustment = auditPremiumAdjustment; }

    public String getAuditDueDate() { return auditDueDate; }
    public void setAuditDueDate(String auditDueDate) { this.auditDueDate = auditDueDate; }

    public String getAuditCompleteDate() { return auditCompleteDate; }
    public void setAuditCompleteDate(String auditCompleteDate) { this.auditCompleteDate = auditCompleteDate; }
}
