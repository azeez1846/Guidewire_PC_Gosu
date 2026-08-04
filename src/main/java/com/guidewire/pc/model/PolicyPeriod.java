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
import java.util.logging.Logger;
import java.util.logging.Level;

public class PolicyPeriod implements KeyableBean, EffDatedBranch, Coverable {
    private static final Logger LOGGER = Logger.getLogger(PolicyPeriod.class.getName());

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

    // OOTB UW Issues
    private final List<UWIssue> uwIssues = new ArrayList<>();

    // OOTB Inland Marine Scheduled Equipment
    private final List<ScheduledEquipmentItem> scheduledEquipmentItems = new ArrayList<>();

    public List<ScheduledItem> getScheduledItems() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getScheduledItems");
        return scheduledItems;
    }

    public void addScheduledItem(ScheduledItem item) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.addScheduledItem");
        if (item != null) {
            item.setItemNumber(scheduledItems.size() + 1);
            scheduledItems.add(item);
        }
    }

    public List<UWIssue> getUwIssues() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getUwIssues");
        return uwIssues;
    }

    public void addUWIssue(UWIssue issue) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.addUWIssue");
        if (issue != null) {
            this.uwIssues.add(issue);
        }
    }

    public List<UWIssue> getOpenBlockingQuoteIssues() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getOpenBlockingQuoteIssues");
        List<UWIssue> list = new ArrayList<>();
        for (UWIssue issue : uwIssues) {
            if (issue.isBlockingQuote()) {
                list.add(issue);
            }
        }
        return list;
    }

    public List<UWIssue> getOpenBlockingBindIssues() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getOpenBlockingBindIssues");
        List<UWIssue> list = new ArrayList<>();
        for (UWIssue issue : uwIssues) {
            if (issue.isBlockingBind()) {
                list.add(issue);
            }
        }
        return list;
    }

    public boolean hasBlockingQuoteIssues() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.hasBlockingQuoteIssues");
        return !getOpenBlockingQuoteIssues().isEmpty();
    }

    public boolean hasBlockingBindIssues() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.hasBlockingBindIssues");
        return !getOpenBlockingBindIssues().isEmpty();
    }

    public List<ScheduledEquipmentItem> getScheduledEquipmentItems() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getScheduledEquipmentItems");
        return scheduledEquipmentItems;
    }

    public void addScheduledEquipmentItem(ScheduledEquipmentItem item) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.addScheduledEquipmentItem");
        if (item != null) {
            item.setItemNumber(scheduledEquipmentItems.size() + 1);
            scheduledEquipmentItems.add(item);
        }
    }

    public PolicyPeriod() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.PolicyPeriod");
        this.id = GosuORMSession.getInstance().nextID();
        this.status = com.guidewire.pc.constants.PCConstants.STATUS_DRAFT;
        this.productCode = com.guidewire.pc.constants.PCConstants.PRODUCT_COMMERCIAL_AUTO;
        this.jobType = com.guidewire.pc.constants.PCConstants.JOB_TYPE_SUBMISSION;
        this.branchId = GosuORMSession.getInstance().nextID();
        this.policyPeriodFixedId = GosuORMSession.getInstance().nextFixedId(PolicyPeriod.class);
    }

    @Override
    public Long getID() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getID"); return id; }

    @Override
    public void setID(Long id) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setID"); this.id = id; }

    @Override
    public boolean isNew() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.isNew"); return id == null; }

    @Override
    public Long getBranchId() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getBranchId"); return branchId; }
    public void setBranchId(Long branchId) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setBranchId"); this.branchId = branchId; }

    public FixedId<PolicyPeriod> getPolicyPeriodFixedId() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getPolicyPeriodFixedId"); return policyPeriodFixedId; }
    public void setPolicyPeriodFixedId(FixedId<PolicyPeriod> policyPeriodFixedId) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setPolicyPeriodFixedId"); this.policyPeriodFixedId = policyPeriodFixedId; }

    @Override
    public String getJobNumber() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getJobNumber"); return jobNumber; }
    public void setJobNumber(String jobNumber) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setJobNumber"); this.jobNumber = jobNumber; }

    @Override
    public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getPolicyNumber"); return policyNumber; }
    public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setPolicyNumber"); this.policyNumber = policyNumber; }

    public String getProductCode() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getProductCode"); return productCode; }
    public void setProductCode(String productCode) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setProductCode"); this.productCode = productCode; }

    public String getStatus() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getStatus"); return status; }
    public void setStatus(String status) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setStatus"); this.status = status; }

    @Override
    public String getJobType() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getJobType"); return jobType; }
    public void setJobType(String jobType) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setJobType"); this.jobType = jobType; }

    @Override
    public Date getPeriodStart() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getPeriodStart"); return periodStart; }
    public void setPeriodStart(Date periodStart) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setPeriodStart"); this.periodStart = periodStart; }

    @Override
    public Date getPeriodEnd() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getPeriodEnd"); return periodEnd; }
    public void setPeriodEnd(Date periodEnd) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setPeriodEnd"); this.periodEnd = periodEnd; }

    @Override
    public Date getEditEffectiveDate() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getEditEffectiveDate"); return editEffectiveDate; }
    public void setEditEffectiveDate(Date editEffectiveDate) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setEditEffectiveDate"); this.editEffectiveDate = editEffectiveDate; }

    public String getEffectiveDate() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getEffectiveDate"); return effectiveDateStr; }
    public void setEffectiveDate(String effectiveDateStr) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setEffectiveDate");
        this.effectiveDateStr = effectiveDateStr;
        if (effectiveDateStr != null && !effectiveDateStr.trim().isEmpty()) {
            try {
                this.periodStart = new SimpleDateFormat("yyyy-MM-dd").parse(effectiveDateStr);
                if (this.editEffectiveDate == null) this.editEffectiveDate = this.periodStart;
            } catch (ParseException ignored) {}
        }
    }

    public String getExpirationDate() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getExpirationDate"); return expirationDateStr; }
    public void setExpirationDate(String expirationDateStr) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setExpirationDate");
        this.expirationDateStr = expirationDateStr;
        if (expirationDateStr != null && !expirationDateStr.trim().isEmpty()) {
            try {
                this.periodEnd = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDateStr);
            } catch (ParseException ignored) {}
        }
    }

    public int getTermMonths() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getTermMonths"); return termMonths; }
    public void setTermMonths(int termMonths) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setTermMonths"); this.termMonths = termMonths; }

    public String getBaseState() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getBaseState"); return baseState; }
    public void setBaseState(String baseState) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setBaseState"); this.baseState = baseState; }

    public String getProducerCode() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getProducerCode"); return producerCode; }
    public void setProducerCode(String producerCode) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setProducerCode"); this.producerCode = producerCode; }

    public Account getAccount() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getAccount"); return account; }
    public void setAccount(Account account) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setAccount"); this.account = account; }

    @Override
    public List<EffDatedBean> getEffDatedBeans() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getEffDatedBeans"); return effDatedBeans; }

    @Override
    public void addEffDatedBean(EffDatedBean bean) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.addEffDatedBean");
        bean.setBranch(this);
        effDatedBeans.add(bean);
        GosuORMSession.getInstance().saveEffDatedBean(bean);
    }

    public String getBodilyInjuryLimit() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getBodilyInjuryLimit"); return bodilyInjuryLimit; }
    public void setBodilyInjuryLimit(String bodilyInjuryLimit) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setBodilyInjuryLimit"); this.bodilyInjuryLimit = bodilyInjuryLimit; }

    public String getPropertyDamageLimit() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getPropertyDamageLimit"); return propertyDamageLimit; }
    public void setPropertyDamageLimit(String propertyDamageLimit) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setPropertyDamageLimit"); this.propertyDamageLimit = propertyDamageLimit; }

    public String getComprehensiveDeductible() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getComprehensiveDeductible"); return comprehensiveDeductible; }
    public void setComprehensiveDeductible(String comprehensiveDeductible) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setComprehensiveDeductible"); this.comprehensiveDeductible = comprehensiveDeductible; }

    public String getCollisionDeductible() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getCollisionDeductible"); return collisionDeductible; }
    public void setCollisionDeductible(String collisionDeductible) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setCollisionDeductible"); this.collisionDeductible = collisionDeductible; }

    public BigDecimal getBasePremium() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getBasePremium"); return basePremium; }
    public void setBasePremium(BigDecimal basePremium) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setBasePremium"); this.basePremium = basePremium; }

    public BigDecimal getTaxesAndFees() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getTaxesAndFees"); return taxesAndFees; }
    public void setTaxesAndFees(BigDecimal taxesAndFees) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setTaxesAndFees"); this.taxesAndFees = taxesAndFees; }

    public BigDecimal getTotalPremium() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getTotalPremium"); return totalPremium; }
    public void setTotalPremium(BigDecimal totalPremium) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setTotalPremium"); this.totalPremium = totalPremium; }

    public String getCreateTime() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getCreateTime"); return createTime; }
    public void setCreateTime(String createTime) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.setCreateTime"); this.createTime = createTime; }

    public String getFormattedStatus() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getFormattedStatus");
        if (PCConstants.STATUS_ISSUED.equalsIgnoreCase(status)) return "In Force (Issued)";
        if (PCConstants.STATUS_BOUND.equalsIgnoreCase(status)) return PCConstants.STATUS_BOUND;
        if (PCConstants.STATUS_QUOTED.equalsIgnoreCase(status)) return PCConstants.STATUS_QUOTED;
        return PCConstants.STATUS_DRAFT;
    }

    public BigDecimal calculatePremium() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.calculatePremium");
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
        LOGGER.log(Level.FINE, "→ PolicyPeriod.createPolicyChangeBranch");
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
        LOGGER.log(Level.FINE, "→ PolicyPeriod.createCancellationBranch");
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
        LOGGER.log(Level.FINE, "→ PolicyPeriod.copySubmissionBranch");
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
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getSlice");
        return new com.guidewire.pc.orm.PolicyPeriodSlice(this, asOfDate);
    }

    public List<EffDatedBean> getSlicedEffDatedBeans(Date asOfDate) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getSlicedEffDatedBeans");
        return getSlice(asOfDate).getSlicedBeans();
    }

    // COVERABLE INTERFACE IMPLEMENTATION

    @Override
    public List<Coverage> getCoverages() {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getCoverages");
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
        LOGGER.log(Level.FINE, "→ PolicyPeriod.getCoverage");
        for (Coverage c : getCoverages()) {
            if (c.getPatternCode().equalsIgnoreCase(patternCode)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Coverage createCoverage(String patternCode) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.createCoverage");
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
        LOGGER.log(Level.FINE, "→ PolicyPeriod.removeCoverage");
        Coverage cov = getCoverage(patternCode);
        if (cov != null) {
            effDatedBeans.remove(cov);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasCoverage(String patternCode) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.hasCoverage");
        return getCoverage(patternCode) != null;
    }

    // POLICYPERIOD VALIDATION

    public com.guidewire.pc.validation.PCValidationContext validate(String validationLevel) {
        LOGGER.log(Level.FINE, "→ PolicyPeriod.validate");
        return com.guidewire.pc.validation.PolicyPeriodValidation.validate(this, validationLevel);
    }
}
