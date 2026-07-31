package com.guidewire.pc.model;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.orm.EffDatedBean;
import com.guidewire.pc.orm.EffDatedBranch;
import com.guidewire.pc.orm.FixedId;
import com.guidewire.pc.orm.GosuORMSession;
import com.guidewire.pc.orm.KeyableBean;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.guidewire.pc.productmodel.Coverable;
import com.guidewire.pc.productmodel.CoveragePattern;
import com.guidewire.pc.productmodel.ProductModelLoader;

public class PolicyPeriod implements KeyableBean, EffDatedBranch, Coverable {
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

    // Scheduled Items
    private final List<ScheduledItem> scheduledItems = new ArrayList<>();

    public List<ScheduledItem> getScheduledItems() {
        return scheduledItems;
    }

    public void addScheduledItem(ScheduledItem item) {
        if (item != null) {
            item.setItemNumber(scheduledItems.size() + 1);
            scheduledItems.add(item);
        }
    }

    public PolicyPeriod() {
        this.id = GosuORMSession.getInstance().nextID();
        this.status = com.guidewire.pc.constants.PCConstants.STATUS_DRAFT;
        this.productCode = com.guidewire.pc.constants.PCConstants.PRODUCT_COMMERCIAL_AUTO;
        this.jobType = com.guidewire.pc.constants.PCConstants.JOB_TYPE_SUBMISSION;
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
        if (effectiveDateStr != null && !effectiveDateStr.trim().isEmpty()) {
            try {
                this.periodStart = new SimpleDateFormat("yyyy-MM-dd").parse(effectiveDateStr);
                if (this.editEffectiveDate == null) this.editEffectiveDate = this.periodStart;
            } catch (ParseException ignored) {}
        }
    }

    public String getExpirationDate() { return expirationDateStr; }
    public void setExpirationDate(String expirationDateStr) {
        this.expirationDateStr = expirationDateStr;
        if (expirationDateStr != null && !expirationDateStr.trim().isEmpty()) {
            try {
                this.periodEnd = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDateStr);
            } catch (ParseException ignored) {}
        }
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
        if (PCConstants.STATUS_ISSUED.equalsIgnoreCase(status)) return "In Force (Issued)";
        if (PCConstants.STATUS_BOUND.equalsIgnoreCase(status)) return PCConstants.STATUS_BOUND;
        if (PCConstants.STATUS_QUOTED.equalsIgnoreCase(status)) return PCConstants.STATUS_QUOTED;
        return PCConstants.STATUS_DRAFT;
    }

    public BigDecimal calculatePremium() {
        double rate = 500.0;
        if (PCConstants.PRODUCT_PERSONAL_AUTO.equalsIgnoreCase(productCode)) rate = 650.0;
        else if (PCConstants.PRODUCT_COMMERCIAL_AUTO.equalsIgnoreCase(productCode)) rate = 1250.0;
        else if (PCConstants.PRODUCT_COMMERCIAL_PROPERTY.equalsIgnoreCase(productCode)) rate = 2100.0;
        else if (PCConstants.PRODUCT_GENERAL_LIABILITY.equalsIgnoreCase(productCode)) rate = 1800.0;

        if (termMonths == 12) rate *= 1.9;

        if ("$500k/$500k".equals(bodilyInjuryLimit)) rate += 250.0;
        else if ("$1M/$1M".equals(bodilyInjuryLimit)) rate += 500.0;

        if ("$250k".equals(propertyDamageLimit)) rate += 150.0;
        else if ("$500k".equals(propertyDamageLimit)) rate += 300.0;

        if (PCConstants.JOB_TYPE_POLICY_CHANGE.equalsIgnoreCase(jobType)) {
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
        newBranch.setJobType(PCConstants.JOB_TYPE_POLICY_CHANGE);
        newBranch.setStatus(PCConstants.STATUS_DRAFT);
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
        cancelBranch.setJobType(PCConstants.JOB_TYPE_CANCELLATION);
        cancelBranch.setStatus(PCConstants.STATUS_DRAFT);
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

    public PolicyPeriod copySubmissionBranch(String newJobNum) {
        PolicyPeriod copy = new PolicyPeriod(); // Generates fresh ID, branchId, and policyPeriodFixedId
        copy.setJobNumber(newJobNum);
        copy.setPolicyNumber(null);
        copy.setAccount(this.account);
        copy.setProductCode(this.productCode);
        copy.setJobType(PCConstants.JOB_TYPE_SUBMISSION);
        copy.setStatus(PCConstants.STATUS_DRAFT);
        copy.setEffectiveDate(this.effectiveDateStr);
        copy.setExpirationDate(this.expirationDateStr);
        copy.setTermMonths(this.termMonths);
        copy.setBaseState(this.baseState);
        copy.setProducerCode(this.producerCode);
        copy.setBodilyInjuryLimit(this.bodilyInjuryLimit);
        copy.setPropertyDamageLimit(this.propertyDamageLimit);
        copy.setComprehensiveDeductible(this.comprehensiveDeductible);
        copy.setCollisionDeductible(this.collisionDeductible);

        Date effDate = this.getPeriodStart();
        for (EffDatedBean b : this.effDatedBeans) {
            EffDatedBean cloned = b.cloneSlice(copy, effDate != null ? effDate : new Date());
            copy.addEffDatedBean(cloned);
        }

        copy.calculatePremium();
        return copy;
    }

    public com.guidewire.pc.orm.PolicyPeriodSlice getSlice(Date asOfDate) {
        return new com.guidewire.pc.orm.PolicyPeriodSlice(this, asOfDate);
    }

    public List<EffDatedBean> getSlicedEffDatedBeans(Date asOfDate) {
        return getSlice(asOfDate).getSlicedBeans();
    }

    // COVERABLE INTERFACE IMPLEMENTATION

    @Override
    public List<Coverage> getCoverages() {
        List<Coverage> list = new ArrayList<>();
        for (EffDatedBean b : effDatedBeans) {
            if (b instanceof Coverage cov) {
                list.add(cov);
            }
        }
        return list;
    }

    @Override
    public Coverage getCoverage(String patternCode) {
        for (Coverage c : getCoverages()) {
            if (c.getPatternCode().equalsIgnoreCase(patternCode)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Coverage createCoverage(String patternCode) {
        if (hasCoverage(patternCode)) {
            return getCoverage(patternCode);
        }
        CoveragePattern pattern = ProductModelLoader.getInstance().getCoveragePattern(getProductCode(), patternCode);
        Coverage cov;
        if (pattern != null) {
            cov = new Coverage(pattern.getCode(), pattern.getName(), pattern.getDefaultLimitOrDeductible(), pattern.getDefaultLimitOrDeductible());
        } else {
            cov = new Coverage(patternCode, patternCode, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        if (getPeriodStart() != null) cov.setEffectiveDate(getPeriodStart());
        if (getPeriodEnd() != null) cov.setExpirationDate(getPeriodEnd());
        addEffDatedBean(cov);
        return cov;
    }

    @Override
    public boolean removeCoverage(String patternCode) {
        Coverage cov = getCoverage(patternCode);
        if (cov != null) {
            effDatedBeans.remove(cov);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasCoverage(String patternCode) {
        return getCoverage(patternCode) != null;
    }

    // POLICYPERIOD VALIDATION

    public com.guidewire.pc.validation.PCValidationContext validate(String validationLevel) {
        return com.guidewire.pc.validation.PolicyPeriodValidation.validate(this, validationLevel);
    }
}
