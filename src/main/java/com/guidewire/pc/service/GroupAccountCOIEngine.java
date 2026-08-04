package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupAccountCOIEngine {
    private static final Logger LOGGER = Logger.getLogger(GroupAccountCOIEngine.class.getName());
    private static final GroupAccountCOIEngine instance = new GroupAccountCOIEngine();

    private final List<CertificateOfInsurance> certificateRegistry = new ArrayList<>();

    private GroupAccountCOIEngine() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.GroupAccountCOIEngine");}

    public static GroupAccountCOIEngine getInstance() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.getInstance");
        return instance;
    }

    public CertificateOfInsurance issueCertificate(PolicyPeriod period, String certificateHolderName, String certificateHolderAddress, boolean isAdditionalInsured, boolean isWaiverOfSubrogation) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.issueCertificate");
        CertificateOfInsurance coi = new CertificateOfInsurance();
        coi.setCertificateNumber("COI-" + (System.currentTimeMillis() % 899999 + 100000));
        coi.setPolicyNumber(period != null ? period.getPolicyNumber() : "POL-GEN-1001");
        coi.setInsuredName(period != null && period.getAccount() != null ? period.getAccount().getAccountHolderName() : "Acme Industrial Corp");
        coi.setCertificateHolderName(certificateHolderName);
        coi.setCertificateHolderAddress(certificateHolderAddress);
        coi.setAdditionalInsured(isAdditionalInsured);
        coi.setWaiverOfSubrogation(isWaiverOfSubrogation);
        coi.setBodilyInjuryLimit(period != null ? period.getBodilyInjuryLimit() : "$1,000,000 / $2,000,000");
        coi.setPropertyDamageLimit(period != null ? period.getPropertyDamageLimit() : "$1,000,000");
        coi.setIssueDate(new Date());

        certificateRegistry.add(coi);

        LOGGER.log(Level.INFO, "Issued Certificate of Insurance {0} for Holder {1} on Policy {2}",
                new Object[]{coi.getCertificateNumber(), certificateHolderName, coi.getPolicyNumber()});

        return coi;
    }

    public List<CertificateOfInsurance> getCertificateRegistry() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.getCertificateRegistry");
        return certificateRegistry;
    }

    public static class CertificateOfInsurance implements Serializable {
        private static final long serialVersionUID = 1L;

        private String certificateNumber;
        private String policyNumber;
        private String insuredName;
        private String certificateHolderName;
        private String certificateHolderAddress;
        private boolean isAdditionalInsured;
        private boolean isWaiverOfSubrogation;
        private String bodilyInjuryLimit;
        private String propertyDamageLimit;
        private Date issueDate;

        public String getCertificateNumber() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.getCertificateNumber"); return certificateNumber; }
        public void setCertificateNumber(String certificateNumber) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.setCertificateNumber"); this.certificateNumber = certificateNumber; }

        public String getPolicyNumber() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.getPolicyNumber"); return policyNumber; }
        public void setPolicyNumber(String policyNumber) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.setPolicyNumber"); this.policyNumber = policyNumber; }

        public String getInsuredName() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.getInsuredName"); return insuredName; }
        public void setInsuredName(String insuredName) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.setInsuredName"); this.insuredName = insuredName; }

        public String getCertificateHolderName() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.getCertificateHolderName"); return certificateHolderName; }
        public void setCertificateHolderName(String certificateHolderName) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.setCertificateHolderName"); this.certificateHolderName = certificateHolderName; }

        public String getCertificateHolderAddress() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.getCertificateHolderAddress"); return certificateHolderAddress; }
        public void setCertificateHolderAddress(String certificateHolderAddress) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.setCertificateHolderAddress"); this.certificateHolderAddress = certificateHolderAddress; }

        public boolean isAdditionalInsured() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.isAdditionalInsured"); return isAdditionalInsured; }
        public void setAdditionalInsured(boolean additionalInsured) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.setAdditionalInsured"); isAdditionalInsured = additionalInsured; }

        public boolean isWaiverOfSubrogation() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.isWaiverOfSubrogation"); return isWaiverOfSubrogation; }
        public void setWaiverOfSubrogation(boolean waiverOfSubrogation) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.setWaiverOfSubrogation"); isWaiverOfSubrogation = waiverOfSubrogation; }

        public String getBodilyInjuryLimit() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.getBodilyInjuryLimit"); return bodilyInjuryLimit; }
        public void setBodilyInjuryLimit(String bodilyInjuryLimit) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.setBodilyInjuryLimit"); this.bodilyInjuryLimit = bodilyInjuryLimit; }

        public String getPropertyDamageLimit() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.getPropertyDamageLimit"); return propertyDamageLimit; }
        public void setPropertyDamageLimit(String propertyDamageLimit) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.setPropertyDamageLimit"); this.propertyDamageLimit = propertyDamageLimit; }

        public Date getIssueDate() {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.getIssueDate"); return issueDate; }
        public void setIssueDate(Date issueDate) {
        LOGGER.log(Level.FINE, "→ GroupAccountCOIEngine.setIssueDate"); this.issueDate = issueDate; }
    }
}
