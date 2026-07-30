package com.guidewire.pc;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.service.DataStoreService;
import com.guidewire.pc.service.SearchService;
import com.guidewire.pc.service.SearchService.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RestApiTest {

    private DataStoreService dataStore;

    @BeforeEach
    public void setUp() {
        dataStore = DataStoreService.getInstance();
    }

    @Test
    public void testDataStoreRestIntegration() {
        List<Account> accounts = dataStore.getAccounts();
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());

        List<PolicyPeriod> submissions = dataStore.getSubmissions();
        assertNotNull(submissions);
        assertFalse(submissions.isEmpty());
    }

    @Test
    public void testSearchApiEndpoint() {
        SearchResult result = SearchService.getInstance().executeSearch("A0001001");
        assertNotNull(result);
        assertEquals(SearchService.SearchResultType.DIRECT_ACCOUNT, result.getResultType());
        assertNotNull(result.getDirectAccount());
    }
}
