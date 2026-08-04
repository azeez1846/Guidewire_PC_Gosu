package com.guidewire.pc.service;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SearchService {
    private static final Logger LOGGER = Logger.getLogger(SearchService.class.getName());
    private static final SearchService instance = new SearchService();

    private final DataStoreService dataStore = DataStoreService.getInstance();

    private SearchService() {
        LOGGER.log(Level.FINE, "→ SearchService.SearchService");}

    public static SearchService getInstance() {
        LOGGER.log(Level.FINE, "→ SearchService.getInstance");
        return instance;
    }

    public enum SearchResultType {
        DIRECT_ACCOUNT,
        DIRECT_SUBMISSION,
        MULTI_MATCH,
        NO_MATCH
    }

    public static class SearchResult {
        private final String query;
        private SearchResultType resultType;
        private Account directAccount;
        private PolicyPeriod directSubmission;
        private List<Account> matchingAccounts = new ArrayList<>();
        private List<PolicyPeriod> matchingSubmissions = new ArrayList<>();

        public SearchResult(String query) {
        LOGGER.log(Level.FINE, "→ SearchService.SearchResult");
            this.query = query != null ? query.trim() : "";
            this.resultType = SearchResultType.NO_MATCH;
        }

        public String getQuery() {
        LOGGER.log(Level.FINE, "→ SearchService.getQuery"); return query; }
        public SearchResultType getResultType() {
        LOGGER.log(Level.FINE, "→ SearchService.getResultType"); return resultType; }
        public void setResultType(SearchResultType resultType) {
        LOGGER.log(Level.FINE, "→ SearchService.setResultType"); this.resultType = resultType; }
        public Account getDirectAccount() {
        LOGGER.log(Level.FINE, "→ SearchService.getDirectAccount"); return directAccount; }
        public void setDirectAccount(Account directAccount) {
        LOGGER.log(Level.FINE, "→ SearchService.setDirectAccount"); this.directAccount = directAccount; }
        public PolicyPeriod getDirectSubmission() {
        LOGGER.log(Level.FINE, "→ SearchService.getDirectSubmission"); return directSubmission; }
        public void setDirectSubmission(PolicyPeriod directSubmission) {
        LOGGER.log(Level.FINE, "→ SearchService.setDirectSubmission"); this.directSubmission = directSubmission; }
        public List<Account> getMatchingAccounts() {
        LOGGER.log(Level.FINE, "→ SearchService.getMatchingAccounts"); return matchingAccounts; }
        public void setMatchingAccounts(List<Account> matchingAccounts) {
        LOGGER.log(Level.FINE, "→ SearchService.setMatchingAccounts"); this.matchingAccounts = matchingAccounts; }
        public List<PolicyPeriod> getMatchingSubmissions() {
        LOGGER.log(Level.FINE, "→ SearchService.getMatchingSubmissions"); return matchingSubmissions; }
        public void setMatchingSubmissions(List<PolicyPeriod> matchingSubmissions) {
        LOGGER.log(Level.FINE, "→ SearchService.setMatchingSubmissions"); this.matchingSubmissions = matchingSubmissions; }

        public String getTargetUrl() {
        LOGGER.log(Level.FINE, "→ SearchService.getTargetUrl");
            if (resultType == SearchResultType.DIRECT_ACCOUNT && directAccount != null) {
                return "/?page=account-detail&accNum=" + directAccount.getAccountNumber();
            } else if (resultType == SearchResultType.DIRECT_SUBMISSION && directSubmission != null) {
                return "/?page=submission-wizard&jobNum=" + directSubmission.getJobNumber() + "&step=step1";
            }
            return "/?page=search&q=" + query;
        }
    }

    public SearchResult executeSearch(String rawQuery) {
        LOGGER.log(Level.FINE, "→ SearchService.executeSearch");
        LOGGER.log(java.util.logging.Level.INFO, "Executing Search & QuickJump query: {0}", rawQuery);
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return new SearchResult("");
        }

        String query = rawQuery.trim();
        String queryUpper = query.toUpperCase();
        SearchResult result = new SearchResult(query);

        // 1. Direct Account Number match check (e.g., A0001001, A000...)
        Account exactAcc = dataStore.getAccounts().stream()
                .filter(a -> a.getAccountNumber() != null && a.getAccountNumber().equalsIgnoreCase(query))
                .findFirst().orElse(null);

        if (exactAcc != null) {
            result.setResultType(SearchResultType.DIRECT_ACCOUNT);
            result.setDirectAccount(exactAcc);
            result.getMatchingAccounts().add(exactAcc);
            return result;
        }

        // 2. Direct Submission Job Number or Policy Number match check (e.g., S0005001, POL-881920)
        PolicyPeriod exactSub = dataStore.getSubmissions().stream()
                .filter(s -> (s.getJobNumber() != null && s.getJobNumber().equalsIgnoreCase(query)) ||
                        (s.getPolicyNumber() != null && s.getPolicyNumber().equalsIgnoreCase(query)))
                .findFirst().orElse(null);

        if (exactSub != null) {
            result.setResultType(SearchResultType.DIRECT_SUBMISSION);
            result.setDirectSubmission(exactSub);
            result.getMatchingSubmissions().add(exactSub);
            return result;
        }

        // 3. Multi-match search across Accounts and Submissions
        List<Account> matchingAccounts = dataStore.getAccounts().stream()
                .filter(a -> matchesAccount(a, queryUpper))
                .toList();

        List<PolicyPeriod> matchingSubmissions = dataStore.getSubmissions().stream()
                .filter(s -> matchesSubmission(s, queryUpper))
                .toList();

        result.setMatchingAccounts(new ArrayList<>(matchingAccounts));
        result.setMatchingSubmissions(new ArrayList<>(matchingSubmissions));

        if (matchingAccounts.size() == 1 && matchingSubmissions.isEmpty()) {
            result.setResultType(SearchResultType.DIRECT_ACCOUNT);
            result.setDirectAccount(matchingAccounts.get(0));
        } else if (matchingSubmissions.size() == 1 && matchingAccounts.isEmpty()) {
            result.setResultType(SearchResultType.DIRECT_SUBMISSION);
            result.setDirectSubmission(matchingSubmissions.get(0));
        } else if (!matchingAccounts.isEmpty() || !matchingSubmissions.isEmpty()) {
            result.setResultType(SearchResultType.MULTI_MATCH);
        } else {
            result.setResultType(SearchResultType.NO_MATCH);
        }

        return result;
    }

    private boolean matchesAccount(Account a, String qUpper) {
        LOGGER.log(Level.FINE, "→ SearchService.matchesAccount");
        if (a == null) return false;
        return (a.getAccountNumber() != null && a.getAccountNumber().toUpperCase().contains(qUpper)) ||
                (a.getAccountHolderName() != null && a.getAccountHolderName().toUpperCase().contains(qUpper)) ||
                (a.getFein() != null && a.getFein().toUpperCase().contains(qUpper)) ||
                (a.getCity() != null && a.getCity().toUpperCase().contains(qUpper)) ||
                (a.getState() != null && a.getState().toUpperCase().contains(qUpper)) ||
                (a.getPostalCode() != null && a.getPostalCode().toUpperCase().contains(qUpper));
    }

    private boolean matchesSubmission(PolicyPeriod s, String qUpper) {
        LOGGER.log(Level.FINE, "→ SearchService.matchesSubmission");
        if (s == null) return false;
        return (s.getJobNumber() != null && s.getJobNumber().toUpperCase().contains(qUpper)) ||
                (s.getPolicyNumber() != null && s.getPolicyNumber().toUpperCase().contains(qUpper)) ||
                (s.getProductCode() != null && s.getProductCode().toUpperCase().contains(qUpper)) ||
                (s.getBaseState() != null && s.getBaseState().toUpperCase().contains(qUpper)) ||
                (s.getAccount() != null && s.getAccount().getAccountNumber() != null && s.getAccount().getAccountNumber().toUpperCase().contains(qUpper)) ||
                (s.getStatus() != null && s.getStatus().toUpperCase().contains(qUpper));
    }
}
