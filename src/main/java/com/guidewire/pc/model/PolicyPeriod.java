package com.guidewire.pc.model;

import com.guidewire.pc.orm.EffDatedBean;
import com.guidewire.pc.orm.EffDatedBranch;
import com.guidewire.pc.orm.FixedId;
import com.guidewire.pc.orm.GosuORMSession;
import com.guidewire.pc.orm.KeyableBean;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PolicyPeriod implements KeyableBean, EffDatedBranch {
    private Long id;
    private Long branchId;
    private FixedId<PolicyPeriod> policyPeriodFixedId;
    private String jobNumber;
    private String policyNumber;
    private String productCode; // PersonalAuto, CommercialAuto, CommercialProperty, GeneralLiability
    private String status; // Draft, Quoted, Bound, Issued
    private String jobType = "Submission"; // Submission, PolicyChange, Cancellation, Renewal

    private Date periodStart;
    private Date periodEnd;
    private Date editEffectiveDate;

    private String effectiveDateStr;
    private String expirationDateStr;
    private int termMonths = 12;
    private String baseState = "CA";
    private String producerCode;
    private Account account;

    // Coverage details & EffDated Beans
    private final List<EffDatedBean> effDatedBeans = new ArrayList<>();
    private String bodilyInjuryLimit = "$500k/$500k";
    private String propertyDamageLimit = "$250k";
    private String comprehensiveDeductible = "$500";
    private String collisionDeductible = "$1000";

    // Financials
    private BigDecimal basePremium = BigDecimal.ZERO;
    private BigDecimal taxesAndFees = BigDecimal.ZERO;
    private BigDecimal totalPremium = BigDecimal.ZERO;
    private String createTime;

    public PolicyPeriod() {
        this.id = GosuORMSession.getInstance().nextID();
        this.status = "Draft";
        this.productCode = "CommercialAuto";
        this.jobType = "Submission";
        this.branchId = GosuORMSession.getInstance().nextID();
        this.policyPeriodFixedId = GosuORMSession.getInstance().nextFixedId(PolicyPeriod.class);
    }

    @Override
    public Long getID() { return id; }

    @Override
    public void setID(Long id) { this.id = id; }

    @Override
    public boolean isNew() { return id == null; }

    @Override
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public FixedId<PolicyPeriod> getPolicyPeriodFixedId() { return policyPeriodFixedId; }
    public void setPolicyPeriodFixedId(FixedId<PolicyPeriod> policyPeriodFixedId) { this.policyPeriodFixedId = policyPeriodFixedId; }

    @Override
    public String getJobNumber() { return jobNumber; }
    public void setJobNumber(String jobNumber) { this.jobNumber = jobNumber; }

    @Override
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    @Override
    public Date getPeriodStart() { return periodStart; }
    public void setPeriodStart(Date periodStart) { this.periodStart = periodStart; }

    @Override
    public Date getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(Date periodEnd) { this.periodEnd = periodEnd; }

    @Override
    public Date getEditEffectiveDate() { return editEffectiveDate; }
    public void setEditEffectiveDate(Date editEffectiveDate) { this.editEffectiveDate = editEffectiveDate; }

    public String getEffectiveDate() { return effectiveDateStr; }
    public void setEffectiveDate(String effectiveDateStr) {
        this.effectiveDateStr = effectiveDateStr;
        try {
            this.periodStart = new SimpleDateFormat("yyyy-MM-dd").parse(effectiveDateStr);
            if (this.editEffectiveDate == null) this.editEffectiveDate = this.periodStart;
        } catch (ParseException ignored) {}
    }

    public String getExpirationDate() { return expirationDateStr; }
    public void setExpirationDate(String expirationDateStr) {
        this.expirationDateStr = expirationDateStr;
        try {
            this.periodEnd = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDateStr);
        } catch (ParseException ignored) {}
    }

    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }

    public String getBaseState() { return baseState; }
    public void setBaseState(String baseState) { this.baseState = baseState; }

    public String getProducerCode() { return producerCode; }
    public void setProducerCode(String producerCode) { this.producerCode = producerCode; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    @Override
    public List<EffDatedBean> getEffDatedBeans() { return effDatedBeans; }

    @Override
    public void addEffDatedBean(EffDatedBean bean) {
        bean.setBranch(this);
        effDatedBeans.add(bean);
        GosuORMSession.getInstance().saveEffDatedBean(bean);
    }

    public String getBodilyInjuryLimit() { return bodilyInjuryLimit; }
    public void setBodilyInjuryLimit(String bodilyInjuryLimit) { this.bodilyInjuryLimit = bodilyInjuryLimit; }

    public String getPropertyDamageLimit() { return propertyDamageLimit; }
    public void setPropertyDamageLimit(String propertyDamageLimit) { this.propertyDamageLimit = propertyDamageLimit; }

    public String getComprehensiveDeductible() { return comprehensiveDeductible; }
    public void setComprehensiveDeductible(String comprehensiveDeductible) { this.comprehensiveDeductible = comprehensiveDeductible; }

    public String getCollisionDeductible() { return collisionDeductible; }
    public void setCollisionDeductible(String collisionDeductible) { this.collisionDeductible = collisionDeductible; }

    public BigDecimal getBasePremium() { return basePremium; }
    public void setBasePremium(BigDecimal basePremium) { this.basePremium = basePremium; }

    public BigDecimal getTaxesAndFees() { return taxesAndFees; }
    public void setTaxesAndFees(BigDecimal taxesAndFees) { this.taxesAndFees = taxesAndFees; }

    public BigDecimal getTotalPremium() { return totalPremium; }
    public void setTotalPremium(BigDecimal totalPremium) { this.totalPremium = totalPremium; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getFormattedStatus() {
        if ("Issued".equalsIgnoreCase(status)) return "In Force (Issued)";
        if ("Bound".equalsIgnoreCase(status)) return "Bound";
        if ("Quoted".equalsIgnoreCase(status)) return "Quoted";
        return "Draft";
    }

    public BigDecimal calculatePremium() {
        double rate = 500.0;
        if ("PersonalAuto".equalsIgnoreCase(productCode)) rate = 650.0;
        else if ("CommercialAuto".equalsIgnoreCase(productCode)) rate = 1250.0;
        else if ("CommercialProperty".equalsIgnoreCase(productCode)) rate = 2100.0;
        else if ("GeneralLiability".equalsIgnoreCase(productCode)) rate = 1800.0;

        if (termMonths == 12) rate *= 1.9;

        if ("$500k/$500k".equals(bodilyInjuryLimit)) rate += 250.0;
        else if ("$1M/$1M".equals(bodilyInjuryLimit)) rate += 500.0;

        if ("$250k".equals(propertyDamageLimit)) rate += 150.0;
        else if ("$500k".equals(propertyDamageLimit)) rate += 300.0;

        if ("PolicyChange".equalsIgnoreCase(jobType)) {
            rate *= 0.5; // Mid-term proration adjustment
        }

        double tax = rate * 0.08;
        this.basePremium = BigDecimal.valueOf(rate).setScale(2, java.math.RoundingMode.HALF_UP);
        this.taxesAndFees = BigDecimal.valueOf(tax).setScale(2, java.math.RoundingMode.HALF_UP);
        this.totalPremium = this.basePremium.add(this.taxesAndFees);

        return this.totalPremium;
    }

    // EFFDATED BRANCH TRANSACTION METHODS

    public PolicyPeriod createPolicyChangeBranch(Date changeEffDate, String newJobNum) {
        PolicyPeriod newBranch = new PolicyPeriod();
        newBranch.setPolicyPeriodFixedId(this.policyPeriodFixedId); // STABLE FIXEDID ACROSS BRANCHES!
        newBranch.setJobNumber(newJobNum);
        newBranch.setPolicyNumber(this.policyNumber);
        newBranch.setAccount(this.account);
        newBranch.setProductCode(this.productCode);
        newBranch.setJobType("PolicyChange");
        newBranch.setStatus("Draft");
        newBranch.setEffectiveDate(this.effectiveDateStr);
        newBranch.setExpirationDate(this.expirationDateStr);
        newBranch.setEditEffectiveDate(changeEffDate);
        newBranch.setProducerCode(this.producerCode);

        // Copy and clone FixedId coverage slices for new branch
        for (EffDatedBean b : this.effDatedBeans) {
            EffDatedBean clonedSlice = b.cloneSlice(newBranch, changeEffDate);
            newBranch.addEffDatedBean(clonedSlice);
        }

        return newBranch;
    }

    public PolicyPeriod createCancellationBranch(Date cancelEffDate, String newJobNum) {
        PolicyPeriod cancelBranch = new PolicyPeriod();
        cancelBranch.setPolicyPeriodFixedId(this.policyPeriodFixedId);
        cancelBranch.setJobNumber(newJobNum);
        cancelBranch.setPolicyNumber(this.policyNumber);
        cancelBranch.setAccount(this.account);
        cancelBranch.setProductCode(this.productCode);
        cancelBranch.setJobType("Cancellation");
        cancelBranch.setStatus("Draft");
        cancelBranch.setEffectiveDate(this.effectiveDateStr);
        cancelBranch.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd").format(cancelEffDate));
        cancelBranch.setEditEffectiveDate(cancelEffDate);
        cancelBranch.setProducerCode(this.producerCode);

        for (EffDatedBean b : this.effDatedBeans) {
            EffDatedBean cloned = b.cloneSlice(cancelBranch, cancelEffDate);
            cloned.setExpirationDate(cancelEffDate);
            cloned.setChangeType("REMOVE");
            cancelBranch.addEffDatedBean(cloned);
        }

        return cancelBranch;
    }
}
