package com.guidewire.pc;

import com.guidewire.pc.service.SearchService;
import com.guidewire.pc.service.SearchService.SearchResult;
import com.guidewire.pc.service.SearchService.SearchResultType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SearchServiceTest {

    private SearchService searchService;

    @BeforeEach
    public void setUp() {
        searchService = SearchService.getInstance();
    }

    @Test
    public void testExactAccountQuickJump() {
        SearchResult result = searchService.executeSearch("A0001001");
        assertNotNull(result);
        assertEquals(SearchResultType.DIRECT_ACCOUNT, result.getResultType());
        assertNotNull(result.getDirectAccount());
        assertEquals("A0001001", result.getDirectAccount().getAccountNumber());
        assertTrue(result.getTargetUrl().contains("account-detail"));
    }

    @Test
    public void testCaseInsensitiveAccountQuickJump() {
        SearchResult result = searchService.executeSearch("a0001001");
        assertNotNull(result);
        assertEquals(SearchResultType.DIRECT_ACCOUNT, result.getResultType());
        assertNotNull(result.getDirectAccount());
        assertEquals("A0001001", result.getDirectAccount().getAccountNumber());
    }

    @Test
    public void testExactSubmissionQuickJump() {
        SearchResult result = searchService.executeSearch("S0005001");
        assertNotNull(result);
        assertEquals(SearchResultType.DIRECT_SUBMISSION, result.getResultType());
        assertNotNull(result.getDirectSubmission());
        assertEquals("S0005001", result.getDirectSubmission().getJobNumber());
        assertTrue(result.getTargetUrl().contains("submission-wizard"));
    }

    @Test
    public void testExactPolicyNumberQuickJump() {
        SearchResult result = searchService.executeSearch("POL-849102");
        assertNotNull(result);
        assertEquals(SearchResultType.DIRECT_SUBMISSION, result.getResultType());
        assertNotNull(result.getDirectSubmission());
        assertEquals("POL-849102", result.getDirectSubmission().getPolicyNumber());
    }

    @Test
    public void testPartialNameMultiMatch() {
        SearchResult result = searchService.executeSearch("Acme");
        assertNotNull(result);
        assertTrue(result.getResultType() == SearchResultType.MULTI_MATCH || result.getResultType() == SearchResultType.DIRECT_ACCOUNT);
        assertFalse(result.getMatchingAccounts().isEmpty());
    }

    @Test
    public void testNonExistentQueryNoMatch() {
        SearchResult result = searchService.executeSearch("NONEXISTENT999999");
        assertNotNull(result);
        assertEquals(SearchResultType.NO_MATCH, result.getResultType());
        assertTrue(result.getMatchingAccounts().isEmpty());
        assertTrue(result.getMatchingSubmissions().isEmpty());
    }

    @Test
    public void testNullOrEmptyQuery() {
        SearchResult nullResult = searchService.executeSearch(null);
        assertNotNull(nullResult);
        assertEquals(SearchResultType.NO_MATCH, nullResult.getResultType());

        SearchResult emptyResult = searchService.executeSearch("   ");
        assertNotNull(emptyResult);
        assertEquals(SearchResultType.NO_MATCH, emptyResult.getResultType());
    }
}
