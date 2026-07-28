package com.guidewire.pc.service;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.Activity;
import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataStoreService {
    private static DataStoreService instance;

    private final List<Account> accounts = new ArrayList<>();
    private final List<PolicyPeriod> submissions = new ArrayList<>();
    private final List<Activity> activities = new ArrayList<>();
    private int accountSeq = 1003;
    private int jobSeq = 5003;

    private DataStoreService() {
        seedSampleData();
    }

    public static synchronized DataStoreService getInstance() {
        if (instance == null) {
            instance = new DataStoreService();
        }
        return instance;
    }

    private void seedSampleData() {
        // Sample Account 1
        Account acc1 = new Account();
        acc1.setAccountNumber("A0001001");
        acc1.setAccountHolderName("Acme Logistics Inc.");
        acc1.setAccountHolderType("Company");
        acc1.setFein("12-3456789");
        acc1.setAddressLine1("100 Innovation Way");
        acc1.setAddressLine2("Suite 400");
        acc1.setCity("San Jose");
        acc1.setState("CA");
        acc1.setPostalCode("95113");
        acc1.setPhone("(408) 555-0199");
        acc1.setEmail("contact@acmelogistics.com");
        acc1.setAccountStatus("Active");
        acc1.setProducerCode("PR-10928");
        acc1.setIndustryCode("484110 - General Freight");
        acc1.setOrgType("Corporation");
        acc1.setCreateTime("2026-01-15 09:30:00");
        accounts.add(acc1);

        // Sample Account 2
        Account acc2 = new Account();
        acc2.setAccountNumber("A0001002");
        acc2.setAccountHolderName("Johnathan Mercer");
        acc2.setAccountHolderType("Individual");
        acc2.setFein("XXX-XX-4891");
        acc2.setAddressLine1("742 Evergreen Terrace");
        acc2.setCity("Springfield");
        acc2.setState("OR");
        acc2.setPostalCode("97477");
        acc2.setPhone("(541) 555-0142");
        acc2.setEmail("john.mercer@example.com");
        acc2.setAccountStatus("Active");
        acc2.setProducerCode("PR-20451");
        acc2.setIndustryCode("811111 - Automotive Repair");
        acc2.setOrgType("Individual");
        acc2.setCreateTime("2026-02-01 14:15:00");
        accounts.add(acc2);

        // Sample Submission 1
        PolicyPeriod sub1 = new PolicyPeriod();
        sub1.setJobNumber("S0005001");
        sub1.setPolicyNumber("POL-849102");
        sub1.setProductCode("CommercialAuto");
        sub1.setStatus("Issued");
        sub1.setEffectiveDate("2026-03-01");
        sub1.setExpirationDate("2027-03-01");
        sub1.setTermMonths(12);
        sub1.setBaseState("CA");
        sub1.setProducerCode("PR-10928");
        sub1.setAccount(acc1);
        sub1.setBodilyInjuryLimit("$500k/$500k");
        sub1.setPropertyDamageLimit("$250k");
        sub1.setComprehensiveDeductible("$500");
        sub1.setCollisionDeductible("$1000");
        sub1.setBasePremium(new BigDecimal("2675.00"));
        sub1.setTaxesAndFees(new BigDecimal("214.00"));
        sub1.setTotalPremium(new BigDecimal("2889.00"));
        sub1.setCreateTime("2026-02-10 11:20:00");
        submissions.add(sub1);

        // Sample Submission 2
        PolicyPeriod sub2 = new PolicyPeriod();
        sub2.setJobNumber("S0005002");
        sub2.setProductCode("GeneralLiability");
        sub2.setStatus("Quoted");
        sub2.setEffectiveDate("2026-04-01");
        sub2.setExpirationDate("2027-04-01");
        sub2.setTermMonths(12);
        sub2.setBaseState("CA");
        sub2.setProducerCode("PR-10928");
        sub2.setAccount(acc1);
        sub2.setBodilyInjuryLimit("$1M/$1M");
        sub2.setPropertyDamageLimit("$500k");
        sub2.setComprehensiveDeductible("$1000");
        sub2.setCollisionDeductible("$1000");
        sub2.setBasePremium(new BigDecimal("3920.00"));
        sub2.setTaxesAndFees(new BigDecimal("313.60"));
        sub2.setTotalPremium(new BigDecimal("4233.60"));
        sub2.setCreateTime("2026-02-20 16:45:00");
        submissions.add(sub2);

        // Sample Activities
        Activity act1 = new Activity();
        act1.setSubject("Verify High-Value Commercial Fleet Risk");
        act1.setDescription("Perform loss history verification for Acme Logistics Inc.");
        act1.setPriority("High");
        act1.setStatus("Open");
        act1.setDueDate("2026-08-05");
        act1.setAssignedUser("su");
        act1.setRelatedAccountId("A0001001");
        act1.setRelatedJobNumber("S0005001");
        act1.setCreateTime("2026-07-25 10:00:00");
        activities.add(act1);

        Activity act2 = new Activity();
        act2.setSubject("Review General Liability Endorsement Request");
        act2.setDescription("Underwriter review needed for sub-contractor coverage limits.");
        act2.setPriority("Normal");
        act2.setStatus("Open");
        act2.setDueDate("2026-08-10");
        act2.setAssignedUser("su");
        act2.setRelatedAccountId("A0001001");
        act2.setRelatedJobNumber("S0005002");
        act2.setCreateTime("2026-07-26 14:30:00");
        activities.add(act2);
    }

    public List<Account> getAccounts() { return accounts; }
    public List<PolicyPeriod> getSubmissions() { return submissions; }
    public List<Activity> getActivities() { return activities; }

    public Account findAccount(String accountNumber) {
        for (Account a : accounts) {
            if (a.getAccountNumber().equalsIgnoreCase(accountNumber)) return a;
        }
        return null;
    }

    public PolicyPeriod findSubmission(String jobNumber) {
        for (PolicyPeriod s : submissions) {
            if (s.getJobNumber().equalsIgnoreCase(jobNumber)) return s;
        }
        return null;
    }

    public synchronized Account createAccount(Account newAccount) {
        newAccount.setAccountNumber("A000" + accountSeq++);
        if (newAccount.getAccountStatus() == null) newAccount.setAccountStatus("Active");
        if (newAccount.getCreateTime() == null) {
            newAccount.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        accounts.add(0, newAccount);
        return newAccount;
    }

    public synchronized PolicyPeriod createSubmission(PolicyPeriod submission) {
        submission.setJobNumber("S000" + jobSeq++);
        if (submission.getStatus() == null) submission.setStatus("Draft");
        if (submission.getCreateTime() == null) {
            submission.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        submission.calculatePremium();
        submissions.add(0, submission);
        return submission;
    }
}
