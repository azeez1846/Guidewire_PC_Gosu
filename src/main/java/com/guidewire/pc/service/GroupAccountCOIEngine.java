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

    private GroupAccountCOIEngine() {}

    public static GroupAccountCOIEngine getInstance() {
        return instance;
    }

    public CertificateOfInsurance issueCertificate(PolicyPeriod period, String certificateHolderName, String certificateHolderAddress, boolean isAdditionalInsured, boolean isWaiverOfSubrogation) {
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

        public String getCertificateNumber() { return certificateNumber; }
        public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }

        public String getPolicyNumber() { return policyNumber; }
        public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

        public String getInsuredName() { return insuredName; }
        public void setInsuredName(String insuredName) { this.insuredName = insuredName; }

        public String getCertificateHolderName() { return certificateHolderName; }
        public void setCertificateHolderName(String certificateHolderName) { this.certificateHolderName = certificateHolderName; }

        public String getCertificateHolderAddress() { return certificateHolderAddress; }
        public void setCertificateHolderAddress(String certificateHolderAddress) { this.certificateHolderAddress = certificateHolderAddress; }

        public boolean isAdditionalInsured() { return isAdditionalInsured; }
        public void setAdditionalInsured(boolean additionalInsured) { isAdditionalInsured = additionalInsured; }

        public boolean isWaiverOfSubrogation() { return isWaiverOfSubrogation; }
        public void setWaiverOfSubrogation(boolean waiverOfSubrogation) { isWaiverOfSubrogation = waiverOfSubrogation; }

        public String getBodilyInjuryLimit() { return bodilyInjuryLimit; }
        public void setBodilyInjuryLimit(String bodilyInjuryLimit) { this.bodilyInjuryLimit = bodilyInjuryLimit; }

        public String getPropertyDamageLimit() { return propertyDamageLimit; }
        public void setPropertyDamageLimit(String propertyDamageLimit) { this.propertyDamageLimit = propertyDamageLimit; }

        public Date getIssueDate() { return issueDate; }
        public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    }
}
