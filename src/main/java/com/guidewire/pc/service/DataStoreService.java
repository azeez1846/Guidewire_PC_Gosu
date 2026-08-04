package com.guidewire.pc.service;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.Activity;
import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataStoreService {
    private static final Logger LOGGER = Logger.getLogger(DataStoreService.class.getName());
    private static DataStoreService instance;

    // Fast In-Memory Cache Layers (Eliminates N+1 SQL queries and disk delays)
    private final Map<String, Account> accountCache = new ConcurrentHashMap<>();
    private final Map<String, PolicyPeriod> submissionCache = new ConcurrentHashMap<>();
    private final Map<String, Activity> activityCache = new ConcurrentHashMap<>();
    private volatile boolean cacheLoaded = false;

    private DataStoreService() {
        seedSampleDataIfEmpty();
        warmupCacheFromDb();
    }

    public static synchronized DataStoreService getInstance() {
        if (instance == null) {
            instance = new DataStoreService();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DatabaseService.getInstance().getConnection();
    }

    private synchronized void warmupCacheFromDb() {
        if (cacheLoaded) return;
        try {
            // Load Accounts into cache
            List<Account> dbAccounts = loadAccountsFromDb();
            for (Account a : dbAccounts) {
                if (a.getAccountNumber() != null) {
                    accountCache.put(a.getAccountNumber().toUpperCase(), a);
                }
            }

            // Load Submissions into cache
            List<PolicyPeriod> dbSubmissions = loadSubmissionsFromDb();
            for (PolicyPeriod p : dbSubmissions) {
                if (p.getJobNumber() != null) {
                    submissionCache.put(p.getJobNumber().toUpperCase(), p);
                }
            }

            // Load Activities into cache
            List<Activity> dbActivities = loadActivitiesFromDb();
            for (Activity act : dbActivities) {
                if (act.getSubject() != null) {
                    activityCache.put(act.getSubject() + "_" + act.getCreateTime(), act);
                }
            }
            cacheLoaded = true;
            LOGGER.log(Level.INFO, "[DataStore Performance Cache] In-memory cache pre-warmed successfully. Loaded {0} accounts, {1} submissions.",
                    new Object[]{accountCache.size(), submissionCache.size()});
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error pre-warming DataStore cache from DB", e);
        }
    }

    public final synchronized void resetToSeedData() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM POLICY_PERIODS");
            stmt.execute("DELETE FROM ACTIVITIES");
            stmt.execute("DELETE FROM ACCOUNTS");
            submissionCache.clear();
            accountCache.clear();
            activityCache.clear();
            cacheLoaded = false;
            seedSampleDataIfEmpty();
            warmupCacheFromDb();
            LOGGER.info("Database reset to clean sample data successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to reset H2 database to seed data", e);
        }
    }

    private void seedSampleDataIfEmpty() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ACCOUNTS");
            if (rs.next() && rs.getInt(1) > 0) {
                LOGGER.info("H2 database already populated with accounts.");
                return;
            }

            LOGGER.info("Seeding initial OOTB Guidewire sample data into H2 database...");

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
            acc1.setAccountStatus(PCConstants.ACCOUNT_STATUS_ACTIVE);
            acc1.setProducerCode("PR-10928");
            acc1.setIndustryCode("484110 - General Freight");
            acc1.setOrgType("Corporation");
            acc1.setCreateTime("2026-01-15 09:30:00");
            insertAccountToDb(acc1);

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
            acc2.setAccountStatus(PCConstants.ACCOUNT_STATUS_ACTIVE);
            acc2.setProducerCode("PR-20451");
            acc2.setIndustryCode("811111 - Automotive Repair");
            acc2.setOrgType("Individual");
            acc2.setCreateTime("2026-02-01 14:15:00");
            insertAccountToDb(acc2);

            // Sample Submission 1
            PolicyPeriod sub1 = new PolicyPeriod();
            sub1.setJobNumber("S0005001");
            sub1.setPolicyNumber("POL-849102");
            sub1.setProductCode(PCConstants.PRODUCT_COMMERCIAL_AUTO);
            sub1.setStatus(PCConstants.STATUS_ISSUED);
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
            insertSubmissionToDb(sub1);

            // Sample Submission 2
            PolicyPeriod sub2 = new PolicyPeriod();
            sub2.setJobNumber("S0005002");
            sub2.setProductCode(PCConstants.PRODUCT_GENERAL_LIABILITY);
            sub2.setStatus(PCConstants.STATUS_QUOTED);
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
            insertSubmissionToDb(sub2);

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
            insertActivityToDb(act1);

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
            insertActivityToDb(act2);

            LOGGER.info("H2 database sample data seeded successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking or seeding H2 database sample data", e);
        }
    }

    private void insertAccountToDb(Account a) {
        String sql = "INSERT INTO ACCOUNTS (account_number, account_holder_name, account_holder_type, fein, " +
                "address_line1, address_line2, city, state, postal_code, phone, email, account_status, " +
                "producer_code, industry_code, org_type, create_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getAccountNumber());
            ps.setString(2, a.getAccountHolderName());
            ps.setString(3, a.getAccountHolderType());
            ps.setString(4, a.getFein());
            ps.setString(5, a.getAddressLine1());
            ps.setString(6, a.getAddressLine2());
            ps.setString(7, a.getCity());
            ps.setString(8, a.getState());
            ps.setString(9, a.getPostalCode());
            ps.setString(10, a.getPhone());
            ps.setString(11, a.getEmail());
            ps.setString(12, a.getAccountStatus());
            ps.setString(13, a.getProducerCode());
            ps.setString(14, a.getIndustryCode());
            ps.setString(15, a.getOrgType());
            ps.setString(16, a.getCreateTime());
            ps.executeUpdate();

            if (a.getAccountNumber() != null) {
                accountCache.put(a.getAccountNumber().toUpperCase(), a);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert account to H2 database: " + a.getAccountNumber(), e);
            throw new RuntimeException(e);
        }
    }

    private void insertSubmissionToDb(PolicyPeriod sub) {
        String sql = "INSERT INTO POLICY_PERIODS (job_number, policy_number, product_code, status, job_type, " +
                "effective_date, expiration_date, term_months, base_state, producer_code, account_number, " +
                "bodily_injury_limit, property_damage_limit, comprehensive_deductible, collision_deductible, " +
                "base_premium, taxes_and_fees, total_premium, create_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sub.getJobNumber());
            ps.setString(2, sub.getPolicyNumber());
            ps.setString(3, sub.getProductCode());
            ps.setString(4, sub.getStatus());
            ps.setString(5, sub.getJobType());
            ps.setString(6, sub.getEffectiveDate());
            ps.setString(7, sub.getExpirationDate());
            ps.setInt(8, sub.getTermMonths());
            ps.setString(9, sub.getBaseState());
            ps.setString(10, sub.getProducerCode());
            ps.setString(11, sub.getAccount() != null ? sub.getAccount().getAccountNumber() : null);
            ps.setString(12, sub.getBodilyInjuryLimit());
            ps.setString(13, sub.getPropertyDamageLimit());
            ps.setString(14, sub.getComprehensiveDeductible());
            ps.setString(15, sub.getCollisionDeductible());
            ps.setBigDecimal(16, sub.getBasePremium());
            ps.setBigDecimal(17, sub.getTaxesAndFees());
            ps.setBigDecimal(18, sub.getTotalPremium());
            ps.setString(19, sub.getCreateTime());
            ps.executeUpdate();

            if (sub.getJobNumber() != null) {
                submissionCache.put(sub.getJobNumber().toUpperCase(), sub);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert submission to H2 database: " + sub.getJobNumber(), e);
        }
    }

    private void insertActivityToDb(Activity act) {
        String sql = "INSERT INTO ACTIVITIES (subject, description, priority, status, due_date, assigned_user, " +
                "related_account_id, related_job_number, create_time) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, act.getSubject());
            ps.setString(2, act.getDescription());
            ps.setString(3, act.getPriority());
            ps.setString(4, act.getStatus());
            ps.setString(5, act.getDueDate());
            ps.setString(6, act.getAssignedUser());
            ps.setString(7, act.getRelatedAccountId());
            ps.setString(8, act.getRelatedJobNumber());
            ps.setString(9, act.getCreateTime());
            ps.executeUpdate();

            if (act.getSubject() != null) {
                activityCache.put(act.getSubject() + "_" + act.getCreateTime(), act);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert activity to H2 database", e);
        }
    }

    public synchronized Activity createActivity(Activity act) {
        if (act.getCreateTime() == null) {
            act.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        insertActivityToDb(act);
        return act;
    }

    public List<Account> getAccounts() {
        if (!accountCache.isEmpty()) {
            return new ArrayList<>(accountCache.values());
        }
        return loadAccountsFromDb();
    }

    private List<Account> loadAccountsFromDb() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM ACCOUNTS ORDER BY create_time DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Account a = mapResultSetToAccount(rs);
                list.add(a);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to query accounts from H2 database", e);
        }
        return list;
    }

    public List<PolicyPeriod> getSubmissions() {
        if (!submissionCache.isEmpty()) {
            return new ArrayList<>(submissionCache.values());
        }
        return loadSubmissionsFromDb();
    }

    private List<PolicyPeriod> loadSubmissionsFromDb() {
        List<PolicyPeriod> list = new ArrayList<>();
        String sql = "SELECT * FROM POLICY_PERIODS ORDER BY create_time DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PolicyPeriod p = mapResultSetToPolicyPeriod(rs);
                list.add(p);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to query submissions from H2 database", e);
        }
        return list;
    }

    public List<Activity> getActivities() {
        if (!activityCache.isEmpty()) {
            return new ArrayList<>(activityCache.values());
        }
        return loadActivitiesFromDb();
    }

    private List<Activity> loadActivitiesFromDb() {
        List<Activity> list = new ArrayList<>();
        String sql = "SELECT * FROM ACTIVITIES ORDER BY create_time DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Activity act = new Activity();
                act.setSubject(rs.getString("subject"));
                act.setDescription(rs.getString("description"));
                act.setPriority(rs.getString("priority"));
                act.setStatus(rs.getString("status"));
                act.setDueDate(rs.getString("due_date"));
                act.setAssignedUser(rs.getString("assigned_user"));
                act.setRelatedAccountId(rs.getString("related_account_id"));
                act.setRelatedJobNumber(rs.getString("related_job_number"));
                act.setCreateTime(rs.getString("create_time"));
                list.add(act);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to query activities from H2 database", e);
        }
        return list;
    }

    public int getAccountCount() {
        return !accountCache.isEmpty() ? accountCache.size() : loadAccountsFromDb().size();
    }

    public int getSubmissionCount() {
        return !submissionCache.isEmpty() ? submissionCache.size() : loadSubmissionsFromDb().size();
    }

    public int getActivityCount() {
        return !activityCache.isEmpty() ? activityCache.size() : loadActivitiesFromDb().size();
    }

    public Account findAccount(String accountNumber) {
        if (accountNumber == null) return null;
        Account cached = accountCache.get(accountNumber.toUpperCase());
        if (cached != null) return cached;

        String sql = "SELECT * FROM ACCOUNTS WHERE UPPER(account_number) = UPPER(?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account a = mapResultSetToAccount(rs);
                    accountCache.put(a.getAccountNumber().toUpperCase(), a);
                    return a;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find account by number in H2: " + accountNumber, e);
        }
        return null;
    }

    public Account findAccountByNumber(String accountNumber) {
        return findAccount(accountNumber);
    }

    public PolicyPeriod findSubmission(String jobNumber) {
        if (jobNumber == null) return null;
        PolicyPeriod cached = submissionCache.get(jobNumber.toUpperCase());
        if (cached != null) return cached;

        String sql = "SELECT * FROM POLICY_PERIODS WHERE UPPER(job_number) = UPPER(?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jobNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PolicyPeriod p = mapResultSetToPolicyPeriod(rs);
                    submissionCache.put(p.getJobNumber().toUpperCase(), p);
                    return p;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find submission by job number in H2: " + jobNumber, e);
        }
        return null;
    }

    public synchronized Account createAccount(Account newAccount) {
        if (newAccount.getAccountNumber() == null || newAccount.getAccountNumber().trim().isEmpty()) {
            newAccount.setAccountNumber("A000" + com.guidewire.pc.util.SequenceGenerator.nextId());
        }
        if (newAccount.getAccountStatus() == null) newAccount.setAccountStatus("Active");
        if (newAccount.getCreateTime() == null) {
            newAccount.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        insertAccountToDb(newAccount);
        return newAccount;
    }

    public PolicyPeriod findPolicyByPolicyNumber(String policyNumber) {
        if (policyNumber == null) return null;
        for (PolicyPeriod p : submissionCache.values()) {
            if (policyNumber.equalsIgnoreCase(p.getPolicyNumber())) {
                return p;
            }
        }

        String sql = "SELECT * FROM POLICY_PERIODS WHERE UPPER(policy_number) = UPPER(?) ORDER BY create_time DESC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, policyNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PolicyPeriod p = mapResultSetToPolicyPeriod(rs);
                    if (p.getJobNumber() != null) {
                        submissionCache.put(p.getJobNumber().toUpperCase(), p);
                    }
                    return p;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find policy by policy number in H2: " + policyNumber, e);
        }
        return null;
    }

    public synchronized void updateSubmission(PolicyPeriod period) {
        if (period == null || period.getJobNumber() == null) return;
        submissionCache.put(period.getJobNumber().toUpperCase(), period);

        String sql = "UPDATE POLICY_PERIODS SET status = ?, job_type = ?, bodily_injury_limit = ?, " +
                "collision_deductible = ?, base_premium = ?, taxes_and_fees = ?, total_premium = ? " +
                "WHERE UPPER(job_number) = UPPER(?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, period.getStatus());
            ps.setString(2, period.getJobType());
            ps.setString(3, period.getBodilyInjuryLimit());
            ps.setString(4, period.getCollisionDeductible());
            ps.setBigDecimal(5, period.getBasePremium());
            ps.setBigDecimal(6, period.getTaxesAndFees());
            ps.setBigDecimal(7, period.getTotalPremium());
            ps.setString(8, period.getJobNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update policy period in H2: " + period.getJobNumber(), e);
        }
    }

    public synchronized PolicyPeriod createSubmission(PolicyPeriod submission) {
        if (submission.getJobNumber() == null || submission.getJobNumber().trim().isEmpty()) {
            submission.setJobNumber("S000" + com.guidewire.pc.util.SequenceGenerator.nextId());
        }
        if (submission.getStatus() == null) submission.setStatus("Draft");
        if (submission.getCreateTime() == null) {
            submission.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        submission.calculatePremium();
        insertSubmissionToDb(submission);
        return submission;
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountNumber(rs.getString("account_number"));
        a.setAccountHolderName(rs.getString("account_holder_name"));
        a.setAccountHolderType(rs.getString("account_holder_type"));
        a.setFein(rs.getString("fein"));
        a.setAddressLine1(rs.getString("address_line1"));
        a.setAddressLine2(rs.getString("address_line2"));
        a.setCity(rs.getString("city"));
        a.setState(rs.getString("state"));
        a.setPostalCode(rs.getString("postal_code"));
        a.setPhone(rs.getString("phone"));
        a.setEmail(rs.getString("email"));
        a.setAccountStatus(rs.getString("account_status"));
        a.setProducerCode(rs.getString("producer_code"));
        a.setIndustryCode(rs.getString("industry_code"));
        a.setOrgType(rs.getString("org_type"));
        a.setCreateTime(rs.getString("create_time"));
        return a;
    }

    private PolicyPeriod mapResultSetToPolicyPeriod(ResultSet rs) throws SQLException {
        PolicyPeriod p = new PolicyPeriod();
        p.setJobNumber(rs.getString("job_number"));
        p.setPolicyNumber(rs.getString("policy_number"));
        p.setProductCode(rs.getString("product_code"));
        p.setStatus(rs.getString("status"));
        p.setJobType(rs.getString("job_type"));
        p.setEffectiveDate(rs.getString("effective_date"));
        p.setExpirationDate(rs.getString("expiration_date"));
        p.setTermMonths(rs.getInt("term_months"));
        p.setBaseState(rs.getString("base_state"));
        p.setProducerCode(rs.getString("producer_code"));
        
        String accNum = rs.getString("account_number");
        if (accNum != null) {
            p.setAccount(findAccount(accNum));
        }

        p.setBodilyInjuryLimit(rs.getString("bodily_injury_limit"));
        p.setPropertyDamageLimit(rs.getString("property_damage_limit"));
        p.setComprehensiveDeductible(rs.getString("comprehensive_deductible"));
        p.setCollisionDeductible(rs.getString("collision_deductible"));
        p.setBasePremium(rs.getBigDecimal("base_premium"));
        p.setTaxesAndFees(rs.getBigDecimal("taxes_and_fees"));
        p.setTotalPremium(rs.getBigDecimal("total_premium"));
        p.setCreateTime(rs.getString("create_time"));
        return p;
    }

    public synchronized Account saveAccount(Account acc) {
        return createAccount(acc);
    }

    public synchronized PolicyPeriod saveSubmission(PolicyPeriod period) {
        if (period.getCreateTime() == null) {
            period.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        insertSubmissionToDb(period);
        return period;
    }
}
