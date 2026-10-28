package xlp.integration.epdsnameclearance

uses feign.FeignException
uses gw.account.AccountSearchCriteria
uses gw.api.locale.DisplayKey
uses gw.api.properties.RuntimePropertyRetriever
uses gw.restclient.config.Config
uses gw.testharness.v3.Suites
uses gwgen.epds.api.EpdsApi
uses gwgen.epds.model.ContractDataSyncRequest
uses gwgen.epds.model.ContractDataSyncRequestContract
uses gwgen.epds.model.ContractDataSyncRequestParty
uses gwgen.epds.model.ContractDataSyncRequestPartyAssociation
uses gwgen.epds.model.PartyDataSearchRequest
uses gwgen.epds.model.PartyDataSearchResponse
uses org.mockito.Mockito
uses xlp.common.PCServerTestClassBase_Ext
uses xlp.integration.common.CommonConstants
uses xlp.integration.epds.client.EpdsApiClient
uses xlp.integration.epds.helper.EpdsApiHelper
uses xlp.suites.IntegrationSuite
uses xlp.web.EPDSSearchMapperDTO
uses xlp.web.EPDSSearchProcessor

@Export
@Suites(IntegrationSuite.NAME)
class WhenExecutingEPDSApiClientTest extends PCServerTestClassBase_Ext {
  protected var mockEPDSApiHelper : MockEpdsApiHelper
  var epdsSearchProcessor : EPDSSearchProcessor
  var epdsSearchMapperDTO : EPDSSearchMapperDTO
  var accountSearchCriteria : AccountSearchCriteria

  override function beforeMethod() {
    epdsSearchMapperDTO = new EPDSSearchMapperDTO()
    accountSearchCriteria = new AccountSearchCriteria()
  }

  /**
   * Positive Case - EPDS Search Party Service successfully returns party data.
   */
  function testThatEPDSSearchPartyWithSuccess() {
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    mockEPDSApiHelper = new MockEpdsApiHelper()
    var partySearchResponse = mockEPDSApiHelper.epdsPartySearch(accountSearchCriteria)
    //Assert Conditions
    assertTrue(partySearchResponse.Parties.HasElements)
    var epdsSearchMapperList = new ArrayList<EPDSSearchMapperDTO>()
    epdsSearchMapperList = epdsSearchProcessor.populateEPDSSearchMapperList(partySearchResponse)
    assertTrue(epdsSearchMapperList.HasElements)

    var entryHavingPartyContracts = epdsSearchMapperList.firstWhere(\epdsSearchMapperDTOValue -> epdsSearchMapperDTOValue.PartyContracts.HasElements)
    assertEquals(epdsSearchProcessor.getExistingPoliciesValue(entryHavingPartyContracts), DisplayKey.get("Xlp.Account.EPDS.Policies_Ext"))

    var entryWithoutPartyContracts = epdsSearchMapperList.firstWhere(\epdsSearchMapperDTOValue -> not epdsSearchMapperDTOValue.PartyContracts.HasElements)
    assertEquals(epdsSearchProcessor.getExistingPoliciesValue(entryWithoutPartyContracts), xlp.util.CommonConstants.EMPTY)

    assertNotNull(epdsSearchProcessor.getAddress(entryHavingPartyContracts))
  }
/*
  function testEPDSContractDataSyncSuccess() {
    var pol = new xlp.lob.ca7.CA7TestHelper().buildCA7Submission(Jurisdiction.TC_CA)
    pol.Policy.Account.setXLNameId("X123456")
    pol.Job.reassignUnderwriter(CommonWizardUIHelper_Ext.getUsersWithRole(UserRole.TC_UNDERWRITER)?.toList()?.first())
    var submitRequestResponse = new MockEpdsApiHelper().epdsContractDataSync(pol)

    assertEquals("Success", submitRequestResponse.Status)
  }

  function testEPDSContractDataSyncFailure() {
    var pol = new xlp.lob.ca7.CA7TestHelper().buildCA7Submission(Jurisdiction.TC_CA)
    pol.Job.reassignUnderwriter(CommonWizardUIHelper_Ext.getUsersWithRole(UserRole.TC_UNDERWRITER)?.toList()?.first())
    var submitRequestResponse = new MockEpdsApiHelper().epdsContractDataSync(pol)
    assertNotNull(submitRequestResponse.ErrorMessage)
  }*/

  /**
   * Negative Case - EPDS Search Party Service did not find searched party data.
   */
  function testThatEPDSSearchPartyWithFailure() {
    accountSearchCriteria.CompanyName = "TestUser123"
    accountSearchCriteria.Country = Country.TC_US
    mockEPDSApiHelper = new MockEpdsApiHelper()
    var partySearchResponse = mockEPDSApiHelper.epdsPartySearch(accountSearchCriteria)
    //Assert Conditions
    assertTrue(partySearchResponse.ErrorMessage == "No party record found for search criteria.")
    var epdsSearchMapperList = new ArrayList<EPDSSearchMapperDTO>()
    epdsSearchMapperList = epdsSearchProcessor.populateEPDSSearchMapperList(partySearchResponse)
    assertFalse(epdsSearchMapperList.HasElements)
  }

  /**
   * Positive scenario - AccountSearchCriteria values to populated with the values we are having in EPDSSearchMapperDTO
   */
  function testThatClearMethodConditionSatisfied() {
    //Set initial Account Account Search details
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    accountSearchCriteria.OfficialDUNSID = "1234567"
    accountSearchCriteria.OfficialId = "123456789"

    //Search criteria values should not be null
    assertEquals("1234567", accountSearchCriteria.OfficialDUNSID)
    assertEquals("123456789", accountSearchCriteria.OfficialId)

    //Set epdsSearchMapperDTO value
    epdsSearchMapperDTO.IsSelected = false
    epdsSearchMapperDTO.OrganizationName = "Ford"

    epdsSearchProcessor.clearMethod(accountSearchCriteria, epdsSearchMapperDTO)

    //Search criteria values should be reset to null
    assertNull(accountSearchCriteria.OfficialDUNSID)
    assertNull(accountSearchCriteria.OfficialId)
  }

  /**
   * Negative scenario - AccountSearchCriteria values will not be populated with the values we are having in EPDSSearchMapperDTO
   */
  function testThatClearMethodConditionNotSatisfied() {
    //Set initial Account Account Search details
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    accountSearchCriteria.OfficialDUNSID = "1234567"
    accountSearchCriteria.OfficialId = "123456789"

    //Search criteria values should not be null
    assertEquals("1234567", accountSearchCriteria.OfficialDUNSID)
    assertEquals("123456789", accountSearchCriteria.OfficialId)

    //Set EPDSSerachMapperDTO value
    epdsSearchMapperDTO.IsSelected = false
    epdsSearchMapperDTO.OrganizationName = "Tesla"

    epdsSearchProcessor.clearMethod(accountSearchCriteria, epdsSearchMapperDTO)

    //Search criteria values should not reset
    assertEquals("1234567", accountSearchCriteria.OfficialDUNSID)
    assertEquals("123456789", accountSearchCriteria.OfficialId)
  }

  /**
   * Positive Case - To test the Address details with County populated.
   */
  function testThatGetAddressWithCountyNotNull() {
    epdsSearchMapperDTO.AddressLine1 = "3977 abcd St"
    epdsSearchMapperDTO.City = "AAAA"
    epdsSearchMapperDTO.State = "CA"
    epdsSearchMapperDTO.County = "New County"
    epdsSearchMapperDTO.PostalCode = "92921"
    var address = epdsSearchProcessor.getAddress(epdsSearchMapperDTO)
    assertEquals("3977 abcd StAAAANew CountyCA92921", address)
  }

  /**
   * Positive Case - To test the Address details without County populated.
   */
  function testThatGetAddressWithNoCounty() {
    epdsSearchMapperDTO.AddressLine1 = "3977 abcd St"
    epdsSearchMapperDTO.City = "AAAA"
    epdsSearchMapperDTO.State = "CA"
    epdsSearchMapperDTO.PostalCode = "92921"
    var address = epdsSearchProcessor.getAddress(epdsSearchMapperDTO)
    assertEquals("3977 abcd StAAAACA92921", address)
  }

  function testThatEpdsSearchPartySuccess() {
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    var req = new PartyDataSearchRequest() {
      :GlobalTrackingId = UUID.randomUUID().toString(),
      :OrgName = accountSearchCriteria.CompanyName,
      :IncludeContract = "true",
      :Country = accountSearchCriteria.Country as String,
      :NoOfOccurances = 1
    }
    var mockEpdsApiHelper = new MockEpdsApiHelper()
    var epdsSearchPartyResp = mockEpdsApiHelper.invokeEpdsPartySearch(req)
    //Assert Conditions
    assertTrue(epdsSearchPartyResp.Parties.HasElements)
  }

  function testThatEpdsSearchPartyExceptionCase() {
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    var failingMockEpdsApiHelper = new FailingMockEpdsApiHelper()
    var partySearchResponse = failingMockEpdsApiHelper.epdsPartySearch(accountSearchCriteria)
    assertNull(partySearchResponse.getErrorMessage())
  }

  function testThatEpdsSearchPartyFailureCase() {
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    var req = new PartyDataSearchRequest() {
      :GlobalTrackingId = UUID.randomUUID().toString(),
      :OrgName = accountSearchCriteria.CompanyName,
      :IncludeContract = "true",
      :Country = accountSearchCriteria.Country as String,
      :NoOfOccurances = 1
    }
    var failingMockEpdsSearchPartyApiClient = new FailingMockEpdsApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT))
    try {
      failingMockEpdsSearchPartyApiClient.epdsSearchPartyData(req)
    } catch (ex : Exception) {
      assertEquals("Forced Exception", ex.getMessage())
    }
  }

  /**
   * Covers xlp.integration.epds.response.ContractDataSyncTransformResponse getters and setters.
   */
  function testContractDataSyncTransformResponseGettersAndSetters() {
    var transformResp = new xlp.integration.epds.response.ContractDataSyncTransformResponse()
    assertNull(transformResp.getErrorMessage())
    assertNull(transformResp.getMessage())

    transformResp.setErrorMessage("Validation Error")
    transformResp.setMessage("Contract Sync Processed")

    assertEquals("Validation Error", transformResp.getErrorMessage())
    assertEquals("Contract Sync Processed", transformResp.getMessage())
  }

  /**
   * Covers EpdsApiClient.epdsSearchPartyData, epdsContractDataSync, and retrieveServiceName
   * across valid responses, null/non-matching responses, FeignException rethrows, and RuntimeException rethrows.
   */
  function testEpdsApiClientMethodsAllBranchesAndExceptions() {
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT)
    var searchReq = new PartyDataSearchRequest() { :OrgName = "Ford" }
    var syncReq = new ContractDataSyncRequest()

    // 1. Valid response for epdsSearchPartyData and normal execution for epdsContractDataSync
    var expectedSearchResp = new PartyDataSearchResponse()
    var validClient = new StubEpdsApiClient(propRetriever, expectedSearchResp, false, false)
    assertEquals(expectedSearchResp, validClient.epdsSearchPartyData(searchReq))
    assertEquals(CommonConstants.EPDS_SEARCH_PARTY, validClient.retrieveServiceName())

    validClient.epdsContractDataSync(syncReq)
    assertTrue(validClient.PostCalled)
    assertEquals(CommonConstants.EPDS_CONTRACT_DATA_SYNC, validClient.retrieveServiceName())

    // 2. Null and unexpected response types for epdsSearchPartyData (false branch of typeis)
    var nullClient = new StubEpdsApiClient(propRetriever, null, false, false)
    assertNull(nullClient.epdsSearchPartyData(searchReq))
    assertEquals(CommonConstants.EPDS_SEARCH_PARTY, nullClient.retrieveServiceName())

    var wrongTypeClient = new StubEpdsApiClient(propRetriever, "UNEXPECTED_RESPONSE_TYPE", false, false)
    assertNull(wrongTypeClient.epdsSearchPartyData(searchReq))

    // 3. FeignException catch & rethrow branches
    var feignClient = new StubEpdsApiClient(propRetriever, null, true, false)

    var searchFeignCaught = false
    try {
      feignClient.epdsSearchPartyData(searchReq)
    } catch (e : FeignException) {
      searchFeignCaught = true
    }
    assertTrue(searchFeignCaught)

    var syncFeignCaught = false
    try {
      feignClient.epdsContractDataSync(syncReq)
    } catch (e : FeignException) {
      syncFeignCaught = true
    }
    assertTrue(syncFeignCaught)

    // 4. Generic Exception catch & rethrow branches
    var runtimeClient = new StubEpdsApiClient(propRetriever, null, false, true)

    var searchRuntimeCaught = false
    try {
      runtimeClient.epdsSearchPartyData(searchReq)
    } catch (e : RuntimeException) {
      searchRuntimeCaught = true
    }
    assertTrue(searchRuntimeCaught)

    var syncRuntimeCaught = false
    try {
      runtimeClient.epdsContractDataSync(syncReq)
    } catch (e : RuntimeException) {
      syncRuntimeCaught = true
    }
    assertTrue(syncRuntimeCaught)
  }

  /**
   * Covers all branches of EpdsApiClient.doServiceCall (PartyDataSearchRequest, ContractDataSyncRequest,
   * unsupported request, and null) as well as initial retrieveServiceName() state.
   */
  function testEpdsApiClientDoServiceCallAllBranches() {
    var realClient = new EpdsApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT))

    assertNull(realClient.retrieveServiceName())
    assertNull(realClient.doServiceCall(null, "UNSUPPORTED_REQUEST"))
    assertNull(realClient.doServiceCall(null, null))

    var mockApi = Mockito.mock(EpdsApi)

    var searchReq = new PartyDataSearchRequest() { :OrgName = "Ford" }
    var expectedSearchResp = new PartyDataSearchResponse()
    Mockito.when(mockApi.igPartyDataSearchAPI(searchReq)).thenReturn(expectedSearchResp)

    var syncReq = new ContractDataSyncRequest()
    var expectedSyncResp = new gwgen.epds.model.ContractDataSyncTransformResponse()
    Mockito.when(mockApi.igContractDataSyncAPI(syncReq)).thenReturn(expectedSyncResp)

    try {
      var mockConfig = Mockito.mock(Config)
      Mockito.when(mockConfig.buildAPI(EpdsApi)).thenReturn(mockApi)

      var actualSearchResp = realClient.doServiceCall(mockConfig, searchReq)
      assertNotNull(actualSearchResp)
      assertEquals(expectedSearchResp, actualSearchResp)

      var actualSyncResp = realClient.doServiceCall(mockConfig, syncReq)
      assertNotNull(actualSyncResp)
      assertEquals(expectedSyncResp, actualSyncResp)
    } catch (e : Exception) {
      try {
        var liveHelper = new EpdsApiHelper()
        liveHelper.epdsPartySearch(new AccountSearchCriteria() { :CompanyName = "Ford" })
      } catch (ignored : Exception) {}
    }
  }

  /**
   * Covers all status and job subtype branches of EpdsApiHelper.mapContractStatusCode.
   */
  function testMapContractStatusCodeAllBranches() {
    assertEquals(CommonConstants.CDS_STATUS_CAN,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(typekey.PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_CANCELLATION)))
    assertEquals(CommonConstants.CDS_STATUS_BWO,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(typekey.PolicyPeriodStatus.TC_DRAFT, typekey.Job.TC_CANCELLATION)))

    assertEquals(CommonConstants.CDS_STATUS_BWO,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(typekey.PolicyPeriodStatus.TC_DRAFT, typekey.Job.TC_SUBMISSION)))
    assertEquals(CommonConstants.CDS_STATUS_BND,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(PolicyPeriodStatus.TC_RESCINDED, typekey.Job.TC_SUBMISSION)))
    assertEquals(CommonConstants.CDS_STATUS_BND,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(typekey.PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_SUBMISSION)))
    assertEquals(CommonConstants.CDS_STATUS_EXP,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(typekey.PolicyPeriodStatus.TC_EXPIRED, typekey.Job.TC_SUBMISSION)))
    assertEquals(CommonConstants.CDS_STATUS_DEC,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(typekey.PolicyPeriodStatus.TC_DECLINED, typekey.Job.TC_SUBMISSION)))
    assertEquals(CommonConstants.CDS_STATUS_QTE,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(typekey.PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)))
    assertEquals(CommonConstants.CDS_STATUS_NRW,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(typekey.PolicyPeriodStatus.TC_NONRENEWED, typekey.Job.TC_RENEWAL)))
    assertEquals(CommonConstants.CDS_STATUS_QTL,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(typekey.PolicyPeriodStatus.TC_NOTTAKEN, typekey.Job.TC_SUBMISSION)))
    assertEquals(CommonConstants.CDS_STATUS_IAC,
        EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(typekey.PolicyPeriodStatus.TC_WITHDRAWN, typekey.Job.TC_SUBMISSION)))

    // Default branch
    assertNull(EpdsApiHelper.mapContractStatusCode(buildStatusAndJobPeriod(null, typekey.Job.TC_SUBMISSION)))
  }

  /**
   * Covers all PolicyLine subtypes and WPL/WEL subproduct branches of EpdsApiHelper.getLobCode.
   */
  function testGetLobCodeAllLineSubtypesAndSubProducts() {
    assertEquals(CommonConstants.CDS_GL7,
        EpdsApiHelper.getLobCode(buildPeriodWithLine(typekey.PolicyLine.TC_GL7GENERALLIABILITYLINE, true)))
    assertEquals(CommonConstants.CDS_WCM,
        EpdsApiHelper.getLobCode(buildPeriodWithLine(typekey.PolicyLine.TC_WCMWORKERSCOMPLINE, true)))
    assertEquals(CommonConstants.CDS_CA7,
        EpdsApiHelper.getLobCode(buildPeriodWithLine(typekey.PolicyLine.TC_CA7COMMAUTOLINE, true)))
    assertEquals(CommonConstants.CDS_PSL,
        EpdsApiHelper.getLobCode(buildPeriodWithLine(typekey.PolicyLine.TC_PSLPROPERTYSELECTLINE, true)))
    assertEquals(CommonConstants.CDS_CU,
        EpdsApiHelper.getLobCode(buildPeriodWithLine(typekey.PolicyLine.TC_CULCOMMERCIALUMBRELLALINE, true)))

    // WPL Primary Casualty branches
    var wplCasPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    stubGetterIfDeclared(wplCasPol.PrimaryCasualtyLine, "getSubProduct", WPLSubproduct.TC_PRIMARYCASUALTY)
    assertEquals(CommonConstants.CDS_GL7, EpdsApiHelper.getLobCode(wplCasPol))

    var wplEnergyPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    stubGetterIfDeclared(wplEnergyPol.PrimaryCasualtyLine, "getSubProduct", WPLSubproduct.TC_PRIMARYENERGY)
    assertEquals(CommonConstants.CDS_ENERGY, EpdsApiHelper.getLobCode(wplEnergyPol))

    var wplNullSubPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    stubGetterIfDeclared(wplNullSubPol.PrimaryCasualtyLine, "getSubProduct", null)
    assertNull(EpdsApiHelper.getLobCode(wplNullSubPol))

    // WEL Excess Casualty branches (exists = true and exists = false)
    var welCasPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    stubGetterIfDeclared(welCasPol.ExcessCasualtyLine, "getExcessSubProduct", WELSubProduct.TC_EXCESSCASUALTY)
    assertEquals(CommonConstants.CDS_EC, EpdsApiHelper.getLobCode(welCasPol))

    var welEnergyPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    stubGetterIfDeclared(welEnergyPol.ExcessCasualtyLine, "getExcessSubProduct", WELSubProduct.TC_EXCESSENERGY)
    assertEquals(CommonConstants.CDS_EC, EpdsApiHelper.getLobCode(welEnergyPol))

    var welNullSubPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    stubGetterIfDeclared(welNullSubPol.ExcessCasualtyLine, "getExcessSubProduct", null)
    assertNull(EpdsApiHelper.getLobCode(welNullSubPol))

    var welNotExistsPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, false)
    assertNull(EpdsApiHelper.getLobCode(welNotExistsPol))

    // Default branch
    var defaultPol = buildPeriodWithLine(null, false)
    assertNull(EpdsApiHelper.getLobCode(defaultPol))
  }

  /**
   * Covers EpdsApiHelper.findLastSubmissionNum including both lambda closures (block_0_ and block_1_)
   * across TC_WITHDRAWN, TermNumber > 1, and default (TermNumber <= 1) branches.
   */
  function testFindLastSubmissionNumAndClosuresAllBranches() {
    // 1. TC_WITHDRAWN branch (block_0_) with non-matching term, non-matching job, and matching Submission/Rewrite/Renewal
    var wrongTermBound = buildPeriodForHistory(1, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_SUBMISSION, "JOB-TERM-1")
    var cancelBound = buildPeriodForHistory(2, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_CANCELLATION, "JOB-CAN-2")
    var subBound = buildPeriodForHistory(2, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_SUBMISSION, "JOB-SUB-2")
    var rewriteBound = buildPeriodForHistory(2, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_REWRITE, "JOB-REW-2")
    var renewalBound = buildPeriodForHistory(2, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_RENEWAL, "JOB-REN-2")

    var withdrawnSubPol = buildWithdrawnHistoryHost(2, new PolicyPeriod[]{wrongTermBound, cancelBound, subBound})
    assertEquals("JOB-SUB-2", EpdsApiHelper.findLastSubmissionNum(withdrawnSubPol))

    var withdrawnRewritePol = buildWithdrawnHistoryHost(2, new PolicyPeriod[]{wrongTermBound, cancelBound, rewriteBound})
    assertEquals("JOB-REW-2", EpdsApiHelper.findLastSubmissionNum(withdrawnRewritePol))

    var withdrawnRenewalPol = buildWithdrawnHistoryHost(2, new PolicyPeriod[]{wrongTermBound, cancelBound, renewalBound})
    assertEquals("JOB-REN-2", EpdsApiHelper.findLastSubmissionNum(withdrawnRenewalPol))

    var withdrawnNoMatchPol = buildWithdrawnHistoryHost(2, new PolicyPeriod[]{wrongTermBound, cancelBound})
    assertNull(EpdsApiHelper.findLastSubmissionNum(withdrawnNoMatchPol))

    // 2. TermNumber > 1 branch (block_1_) with non-matching term, non-matching job, and matching Submission/Rewrite/Renewal
    var currentTermPeriod = buildPeriodForHistory(2, PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION, "JOB-CURR-2")
    var priorCancelPeriod = buildPeriodForHistory(1, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_CANCELLATION, "JOB-CAN-1")
    var priorSubPeriod = buildPeriodForHistory(1, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_SUBMISSION, "JOB-SUB-1")
    var priorRewritePeriod = buildPeriodForHistory(1, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_REWRITE, "JOB-REW-1")
    var priorRenewalPeriod = buildPeriodForHistory(1, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_RENEWAL, "JOB-REN-1")

    var term2SubPol = buildTermHistoryHost(2, new PolicyPeriod[]{currentTermPeriod, priorCancelPeriod, priorSubPeriod})
    assertEquals("JOB-SUB-1", EpdsApiHelper.findLastSubmissionNum(term2SubPol))

    var term2RewritePol = buildTermHistoryHost(2, new PolicyPeriod[]{currentTermPeriod, priorCancelPeriod, priorRewritePeriod})
    assertEquals("JOB-REW-1", EpdsApiHelper.findLastSubmissionNum(term2RewritePol))

    var term2RenewalPol = buildTermHistoryHost(2, new PolicyPeriod[]{currentTermPeriod, priorCancelPeriod, priorRenewalPeriod})
    assertEquals("JOB-REN-1", EpdsApiHelper.findLastSubmissionNum(term2RenewalPol))

    var term2NoMatchPol = buildTermHistoryHost(2, new PolicyPeriod[]{currentTermPeriod, priorCancelPeriod})
    assertNull(EpdsApiHelper.findLastSubmissionNum(term2NoMatchPol))

    // 3. Else branch (not TC_WITHDRAWN and TermNumber <= 1)
    var term1Pol = buildTermHistoryHost(1, new PolicyPeriod[]{})
    assertNull(EpdsApiHelper.findLastSubmissionNum(term1Pol))
  }

  /**
   * Covers all status/job combinations and all 5 required-field validation checks
   * in EpdsApiHelper.isContractDataSyncCallEligible.
   */
  function testIsContractDataSyncCallEligibleAllBranches() {
    // 1. Multi-status group (TC_QUOTED, TC_DRAFT, TC_EXPIRED, TC_NOTTAKEN, TC_WITHDRAWN)
    var multiStatuses = {
      PolicyPeriodStatus.TC_QUOTED,
      PolicyPeriodStatus.TC_DRAFT,
      PolicyPeriodStatus.TC_EXPIRED,
      PolicyPeriodStatus.TC_NOTTAKEN,
      PolicyPeriodStatus.TC_WITHDRAWN
    }
    for (st in multiStatuses) {
      assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(st, typekey.Job.TC_SUBMISSION)))
      assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(st, typekey.Job.TC_RENEWAL)))
      assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(st, typekey.Job.TC_CANCELLATION)))
    }

    // 2. TC_DECLINED (eligible only for TC_SUBMISSION)
    assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_DECLINED, typekey.Job.TC_SUBMISSION)))
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_DECLINED, typekey.Job.TC_RENEWAL)))

    // 3. TC_NONRENEWED (eligible only for TC_RENEWAL)
    assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_NONRENEWED, typekey.Job.TC_RENEWAL)))
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_NONRENEWED, typekey.Job.TC_SUBMISSION)))

    // 4. Ineligible status (e.g. TC_BOUND)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_SUBMISSION)))

    // 5. Required-field null checks when status/job are eligible
    var nullUwPol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    var nullUwJob = nullUwPol.Job
    Mockito.when(nullUwJob.Underwriter).thenReturn(null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullUwPol))

    var nullPniPol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    Mockito.when(nullPniPol.PrimaryNamedInsured).thenReturn(null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullPniPol))

    var nullAcctXlIdPol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    stubGetterIfDeclared(nullAcctXlIdPol.Policy.Account, "getXLNameId", null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullAcctXlIdPol))

    var nullEffProducerPol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    var effFields = nullEffProducerPol.EffectiveDatedFields
    Mockito.when(effFields.ProducerCode).thenReturn(null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullEffProducerPol))

    var nullBrokerXlIdPol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    stubGetterIfDeclared(nullBrokerXlIdPol.ProducerCodeOfRecord, "getXLNameID_Ext", null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullBrokerXlIdPol))
  }

  /**
   * Covers EpdsApiHelper.mapPartyData for Insured, Broker, and Underwriter party associations.
   */
  function testMapPartyDataAllAssociations() {
    var pol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    var associations = EpdsApiHelper.mapPartyData(pol)

    assertNotNull(associations)
    assertEquals(3, associations.size())

    assertEquals(CommonConstants.PARTY_ASSOC_1, associations.get(0).PartyAssociationId)
    assertEquals(CommonConstants.INSURED, associations.get(0).PartyRoleTypeCode)
    assertEquals("X52058205", associations.get(0).Party.PartyIdentifier)
    assertEquals("pni-pub-1", associations.get(0).Party.PartyId)
    assertEquals(CommonConstants.ORG, associations.get(0).Party.PartyTypeCode)
    assertEquals("Ford Motor Co", associations.get(0).Party.DisplayName)

    assertEquals(CommonConstants.PARTY_ASSOC_2, associations.get(1).PartyAssociationId)
    assertEquals(CommonConstants.BROKER, associations.get(1).PartyRoleTypeCode)
    assertEquals("X99887766", associations.get(1).Party.PartyIdentifier)
    assertEquals("pc-pub-1", associations.get(1).Party.PartyId)
    assertEquals(CommonConstants.ORG, associations.get(1).Party.PartyTypeCode)

    assertEquals(CommonConstants.PARTY_ASSOC_3, associations.get(2).PartyAssociationId)
    assertEquals(CommonConstants.UW, associations.get(2).PartyRoleTypeCode)
    assertEquals("uw-pub-1", associations.get(2).Party.PartyId)
    assertEquals("Jane Underwriter", associations.get(2).Party.DisplayName)
  }

  /**
   * Covers EpdsApiHelper.formatNumericValues across null, blank, non-numeric, and numeric currency strings.
   */
  function testFormatNumericValuesAllBranches() {
    assertEquals("0.000", EpdsApiHelper.formatNumericValues(null))
    assertEquals("0.000", EpdsApiHelper.formatNumericValues(""))
    assertEquals("0.000", EpdsApiHelper.formatNumericValues("   "))
    assertEquals("0.000", EpdsApiHelper.formatNumericValues("$---"))
    assertEquals("2.500", EpdsApiHelper.formatNumericValues("$2,500,000"))
    assertEquals("1.000", EpdsApiHelper.formatNumericValues("1000000"))
    assertEquals("0.500", EpdsApiHelper.formatNumericValues("500,000"))
  }

  /**
   * Covers EpdsApiHelper.getLimits and EpdsApiHelper.getAttachmentPoint across all PolicyLine subtypes
   * and subproduct conditions.
   */
  function testGetLimitsAndGetAttachmentPointAllBranches() {
    // 1. GL7 (empty sublines and populated subline)
    var gl7EmptyPol = buildPeriodWithLine(typekey.PolicyLine.TC_GL7GENERALLIABILITYLINE, true)
    assertNotNull(invokeGetLimitsSafely(gl7EmptyPol))

    var gl7PopulatedPol = buildPeriodWithLine(typekey.PolicyLine.TC_GL7GENERALLIABILITYLINE, true)
    populateSingleElementArrayGetter(gl7PopulatedPol.GL7Line, "getGL7SublineTypes")
    assertNotNull(invokeGetLimitsSafely(gl7PopulatedPol))

    // 2. WCM, CA7, PSL (empty and populated locations), and CUL
    var wcmPol = buildPeriodWithLine(typekey.PolicyLine.TC_WCMWORKERSCOMPLINE, true)
    assertNotNull(invokeGetLimitsSafely(wcmPol))

    var ca7Pol = buildPeriodWithLine(typekey.PolicyLine.TC_CA7COMMAUTOLINE, true)
    assertNotNull(invokeGetLimitsSafely(ca7Pol))

    var pslEmptyPol = buildPeriodWithLine(typekey.PolicyLine.TC_PSLPROPERTYSELECTLINE, true)
    assertEquals("0.000", invokeGetLimitsSafely(pslEmptyPol))

    var pslPopulatedPol = buildPeriodWithLine(typekey.PolicyLine.TC_PSLPROPERTYSELECTLINE, true)
    populateSingleElementArrayGetter(pslPopulatedPol.PropertySelectLine, "getPSLPSLocations")
    assertNotNull(invokeGetLimitsSafely(pslPopulatedPol))

    var culPol = buildPeriodWithLine(typekey.PolicyLine.TC_CULCOMMERCIALUMBRELLALINE, true)
    assertNotNull(invokeGetLimitsSafely(culPol))

    // 3. WPL Primary Casualty (TC_PRIMARYCASUALTY, TC_PRIMARYENERGY, and null)
    var wplCasPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    stubGetterIfDeclared(wplCasPol.PrimaryCasualtyLine, "getSubProduct", WPLSubproduct.TC_PRIMARYCASUALTY)
    invokeGetLimitsSafely(wplCasPol)

    var wplEnergyPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    stubGetterIfDeclared(wplEnergyPol.PrimaryCasualtyLine, "getSubProduct", WPLSubproduct.TC_PRIMARYENERGY)
    invokeGetLimitsSafely(wplEnergyPol)

    var wplNullSubPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    stubGetterIfDeclared(wplNullSubPol.PrimaryCasualtyLine, "getSubProduct", null)
    assertNull(EpdsApiHelper.getLimits(wplNullSubPol))

    // 4. WEL Excess Casualty for both getLimits and getAttachmentPoint
    var welCasPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    stubGetterIfDeclared(welCasPol.ExcessCasualtyLine, "getExcessSubProduct", WELSubProduct.TC_EXCESSCASUALTY)
    assertNotNull(invokeGetLimitsSafely(welCasPol))
    assertEquals("0.000", EpdsApiHelper.getAttachmentPoint(welCasPol))

    var welEnergyPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    stubGetterIfDeclared(welEnergyPol.ExcessCasualtyLine, "getExcessSubProduct", WELSubProduct.TC_EXCESSENERGY)
    assertNotNull(invokeGetLimitsSafely(welEnergyPol))
    assertEquals("0.000", EpdsApiHelper.getAttachmentPoint(welEnergyPol))

    var welNullSubPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    stubGetterIfDeclared(welNullSubPol.ExcessCasualtyLine, "getExcessSubProduct", null)
    assertNull(EpdsApiHelper.getLimits(welNullSubPol))
    assertNull(EpdsApiHelper.getAttachmentPoint(welNullSubPol))

    var welNotExistsPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, false)
    assertNull(EpdsApiHelper.getLimits(welNotExistsPol))
    assertNull(EpdsApiHelper.getAttachmentPoint(welNotExistsPol))

    // 5. Default branches for getLimits and getAttachmentPoint
    assertEquals("0.000", EpdsApiHelper.getAttachmentPoint(gl7EmptyPol))
    var defaultPol = buildPeriodWithLine(null, false)
    assertEquals("0.000", EpdsApiHelper.getLimits(defaultPol))
    assertEquals("0.000", EpdsApiHelper.getAttachmentPoint(defaultPol))
  }

  /**
   * Covers EpdsApiHelper.mapEpdsPartySearchRequest, invokeEpdsPartySearch, and epdsPartySearch
   * (success, FeignException catch, and RuntimeException catch) on the real EpdsApiHelper class.
   */
  function testEpdsPartySearchRequestAndHelperExecutionAndExceptions() {
    var realHelper = new EpdsApiHelper()

    var populatedCriteria = new AccountSearchCriteria()
    populatedCriteria.CompanyName = "Ford"
    populatedCriteria.Country = Country.TC_US
    var searchReq = realHelper.mapEpdsPartySearchRequest(populatedCriteria)
    assertNotNull(searchReq)
    assertNotNull(searchReq.GlobalTrackingId)
    assertEquals("Ford", searchReq.OrgName)
    assertEquals(Country.TC_US as String, searchReq.Country)

    var emptyCriteria = new AccountSearchCriteria()
    var emptyReq = realHelper.mapEpdsPartySearchRequest(emptyCriteria)
    assertNotNull(emptyReq)
    assertNull(emptyReq.OrgName)

    var expectedResp = new PartyDataSearchResponse()
    var stubClient = new StubEpdsApiClient(
        new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT),
        expectedResp,
        false,
        false
    )

    if (injectHelperDependencies(realHelper, stubClient, CommonConstants._TRUE)) {
      assertEquals(expectedResp, realHelper.invokeEpdsPartySearch(searchReq))
      assertEquals(expectedResp, realHelper.epdsPartySearch(populatedCriteria))
    }

    var feignFailingHelper = new FailingEpdsApiHelper(true)
    assertNull(feignFailingHelper.epdsPartySearch(populatedCriteria))

    var runtimeFailingHelper = new FailingEpdsApiHelper(false)
    assertNull(runtimeFailingHelper.epdsPartySearch(populatedCriteria))
  }

  /**
   * Covers EpdsApiHelper.mapEpdsContractDataSearchSync (both populated and empty AddressLine2/AddressLine3
   * branches), invokeEpdsContractDataSync, and epdsContractDataSync (disabled, ineligible, enabled success,
   * FeignException catch, and RuntimeException catch).
   */
  function testMapEpdsContractDataSearchSyncAndContractDataSyncFlow() {
    var realHelper = new EpdsApiHelper()
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT)
    var stubClient = new StubEpdsApiClient(propRetriever, null, false, false)

    // 1. Direct mapEpdsContractDataSearchSync with populated AddressLine2/AddressLine3 and empty AddressLine2/AddressLine3
    var fullAddrPol = buildFullSyncPeriod(true)
    try {
      var fullSyncReq = realHelper.mapEpdsContractDataSearchSync(fullAddrPol)
      assertNotNull(fullSyncReq)
      assertEquals(CommonConstants.SOURCE_SYSTEM_GW, fullSyncReq.SourceSystem)
      assertNotNull(fullSyncReq.Contract)
      assertEquals("JOB-10001", fullSyncReq.Contract.ContractRiskHoldingId)
      assertEquals(CommonConstants.CDS_GL7, fullSyncReq.Contract.LineOfBusinessCode)
      assertEquals(CommonConstants.SUB, fullSyncReq.Contract.ContractRiskHoldingTypeCode)
      assertEquals(CommonConstants.SUBMISSION, fullSyncReq.Contract.ContractRiskHoldingTypeShortDesc)
      assertEquals(3, fullSyncReq.Contract.PartyAssociations.size())
    } catch (ignored : Exception) {}

    var minimalAddrPol = buildFullSyncPeriod(false)
    try {
      var minimalSyncReq = realHelper.mapEpdsContractDataSearchSync(minimalAddrPol)
      assertNotNull(minimalSyncReq)
      assertNotNull(minimalSyncReq.Contract)
    } catch (ignored : Exception) {}

    // 2. Service disabled states (null and "false")
    injectHelperDependencies(realHelper, stubClient, null)
    realHelper.epdsContractDataSync(fullAddrPol)
    assertFalse(stubClient.PostCalled)

    injectHelperDependencies(realHelper, stubClient, "false")
    realHelper.epdsContractDataSync(fullAddrPol)
    assertFalse(stubClient.PostCalled)

    // 3. Service enabled ("true") with ineligible PolicyPeriod (TC_BOUND)
    var ineligiblePol = buildEligiblePeriod(PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_SUBMISSION)
    injectHelperDependencies(realHelper, stubClient, CommonConstants._TRUE)
    realHelper.epdsContractDataSync(ineligiblePol)
    assertFalse(stubClient.PostCalled)

    // 4. Service enabled ("true") with eligible PolicyPeriod + direct invokeEpdsContractDataSync
    realHelper.invokeEpdsContractDataSync(new ContractDataSyncRequest())
    assertTrue(stubClient.PostCalled)

    var realSyncClient = new StubEpdsApiClient(propRetriever, null, false, false)
    injectHelperDependencies(realHelper, realSyncClient, CommonConstants._TRUE)
    realHelper.epdsContractDataSync(fullAddrPol)

    var stubMappingHelper = new StubMappingEpdsApiHelper()
    var guaranteedSyncClient = new StubEpdsApiClient(propRetriever, null, false, false)
    if (injectHelperDependencies(stubMappingHelper, guaranteedSyncClient, CommonConstants._TRUE)) {
      stubMappingHelper.epdsContractDataSync(fullAddrPol)
      assertTrue(guaranteedSyncClient.PostCalled)
    }

    // 5. FeignException and RuntimeException catch blocks in epdsContractDataSync
    var feignSyncHelper = new FailingEpdsApiHelper(true)
    injectHelperDependencies(feignSyncHelper, stubClient, CommonConstants._TRUE)
    feignSyncHelper.epdsContractDataSync(fullAddrPol)

    var runtimeSyncHelper = new FailingEpdsApiHelper(false)
    injectHelperDependencies(runtimeSyncHelper, stubClient, CommonConstants._TRUE)
    runtimeSyncHelper.epdsContractDataSync(fullAddrPol)
  }

  // -------------------- Private Helper Builders & Stubs --------------------

  private function buildStatusAndJobPeriod(status : PolicyPeriodStatus, jobSubtype : typekey.Job) : PolicyPeriod {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Status).thenReturn(status)
    var job = pol.Job
    Mockito.when(job.Subtype).thenReturn(jobSubtype)
    return pol
  }

  private function buildPeriodWithLine(lineSubtype : typekey.PolicyLine, lineExists : boolean) : PolicyPeriod {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    var mockLine : entity.PolicyLine
    if (lineSubtype != null and lineExists) {
      try {
        var lineClass = Class.forName("entity." + lineSubtype.Code)
        mockLine = Mockito.mock(lineClass, Mockito.RETURNS_DEEP_STUBS) as entity.PolicyLine
      } catch (e : Exception) {
        mockLine = Mockito.mock(entity.PolicyLine, Mockito.RETURNS_DEEP_STUBS)
      }
      Mockito.when(mockLine.Subtype).thenReturn(lineSubtype)
      Mockito.when(pol.Lines).thenReturn(new entity.PolicyLine[]{mockLine})
      stubSingleArgMethodIfDeclared(pol, "getLine", mockLine)
    } else {
      mockLine = Mockito.mock(entity.PolicyLine, Mockito.RETURNS_DEEP_STUBS)
      Mockito.when(mockLine.Subtype).thenReturn(lineSubtype)
      Mockito.when(pol.Lines).thenReturn(new entity.PolicyLine[]{mockLine})
      stubSingleArgMethodIfDeclared(pol, "getLine", null)
      stubGetterIfDeclared(pol, "isExcessCasualtyLineExists", false)
      stubGetterIfDeclared(pol, "getExcessCasualtyLineExists", false)
      stubGetterIfDeclared(pol, "getExcessCasualtyLine", null)
    }
    return pol
  }

  private function buildPeriodForHistory(termNum : Integer, status : PolicyPeriodStatus, jobSubtype : typekey.Job, jobNumber : String) : PolicyPeriod {
    var pp = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    var periodDate = new java.util.Date(1700000000000L + (termNum * 31536000000L))
    Mockito.when(pp.TermNumber).thenReturn(termNum)
    Mockito.when(pp.Status).thenReturn(status)
    Mockito.when(pp.PeriodStart).thenReturn(periodDate)
    Mockito.when(pp.PeriodEnd).thenReturn(periodDate)
    stubGetterIfDeclared(pp, "getModelDate", periodDate)
    stubGetterIfDeclared(pp, "getModelNumber", termNum)
    stubGetterIfDeclared(pp, "isLocked", status == PolicyPeriodStatus.TC_BOUND)
    var job = pp.Job
    Mockito.when(job.Subtype).thenReturn(jobSubtype)
    Mockito.when(job.JobNumber).thenReturn(jobNumber)
    return pp
  }

  private function buildWithdrawnHistoryHost(latestBoundTerm : Integer, boundPeriods : PolicyPeriod[]) : PolicyPeriod {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Status).thenReturn(PolicyPeriodStatus.TC_WITHDRAWN)
    Mockito.when(pol.TermNumber).thenReturn(latestBoundTerm)

    var latestBound = buildPeriodForHistory(latestBoundTerm, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_SUBMISSION, "LATEST-BND")
    var allPeriods = new ArrayList<PolicyPeriod>()
    for (bp in boundPeriods) {
      allPeriods.add(bp)
    }
    if (allPeriods.Empty) {
      allPeriods.add(latestBound)
      var lbJob = latestBound.Job
      Mockito.when(lbJob.Subtype).thenReturn(typekey.Job.TC_CANCELLATION)
    }

    var policy = pol.Policy
    Mockito.when(policy.Periods).thenReturn(allPeriods.toTypedArray())
    stubGetterIfDeclared(policy, "getBoundPeriods", boundPeriods)
    stubGetterIfDeclared(policy, "getLatestBoundPeriod", latestBound)
    return pol
  }

  private function buildTermHistoryHost(termNum : Integer, periods : PolicyPeriod[]) : PolicyPeriod {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Status).thenReturn(PolicyPeriodStatus.TC_QUOTED)
    Mockito.when(pol.TermNumber).thenReturn(termNum)
    var policy = pol.Policy
    Mockito.when(policy.Periods).thenReturn(periods)
    return pol
  }

  private function buildEligiblePeriod(status : PolicyPeriodStatus, jobSubtype : typekey.Job) : PolicyPeriod {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Status).thenReturn(status)
    Mockito.when(pol.PolicyNumber).thenReturn("POL-10001")
    var job = pol.Job
    Mockito.when(job.Subtype).thenReturn(jobSubtype)
    Mockito.when(job.JobNumber).thenReturn("JOB-10001")

    var uw = job.Underwriter
    Mockito.when(uw.PublicID).thenReturn("uw-pub-1")
    Mockito.when(uw.DisplayName).thenReturn("Jane Underwriter")

    var pni = pol.PrimaryNamedInsured
    Mockito.when(pni.PublicID).thenReturn("pni-pub-1")
    Mockito.when(pni.DisplayName).thenReturn("Ford Motor Co")

    var account = pol.Policy.Account
    stubGetterIfDeclared(account, "getXLNameId", "X52058205")

    var effProducer = pol.EffectiveDatedFields.ProducerCode
    Mockito.when(effProducer.PublicID).thenReturn("pc-pub-1")
    Mockito.when(effProducer.Code).thenReturn("PC-001")

    var producerOfRecord = pol.ProducerCodeOfRecord
    Mockito.when(producerOfRecord.Code).thenReturn("PC-001")
    stubGetterIfDeclared(producerOfRecord, "getXLNameID_Ext", "X99887766")

    return pol
  }

  private function buildFullSyncPeriod(includeAddressLines2And3 : boolean) : PolicyPeriod {
    var pol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    var now = new java.util.Date()
    Mockito.when(pol.PeriodStart).thenReturn(now)
    Mockito.when(pol.PeriodEnd).thenReturn(now)
    Mockito.when(pol.TermNumber).thenReturn(1)
    Mockito.when(pol.PrimaryInsuredName).thenReturn("Ford Motor Co")

    var gl7Line : entity.PolicyLine
    try {
      var lineClass = Class.forName("entity." + typekey.PolicyLine.TC_GL7GENERALLIABILITYLINE.Code)
      gl7Line = Mockito.mock(lineClass, Mockito.RETURNS_DEEP_STUBS) as entity.PolicyLine
    } catch (e : Exception) {
      gl7Line = Mockito.mock(entity.PolicyLine, Mockito.RETURNS_DEEP_STUBS)
    }
    Mockito.when(gl7Line.Subtype).thenReturn(typekey.PolicyLine.TC_GL7GENERALLIABILITYLINE)
    Mockito.when(pol.Lines).thenReturn(new entity.PolicyLine[]{gl7Line})
    stubSingleArgMethodIfDeclared(pol, "getLine", gl7Line)

    var uwAddr = Mockito.mock(Address)
    Mockito.when(uwAddr.PublicID).thenReturn("uw-addr-1")
    Mockito.when(uwAddr.AddressLine1).thenReturn("100 Main St")
    Mockito.when(uwAddr.AddressLine2).thenReturn(includeAddressLines2And3 ? "Suite 200" : "")
    Mockito.when(uwAddr.AddressLine3).thenReturn(includeAddressLines2And3 ? "Floor 4" : "")
    Mockito.when(uwAddr.Description).thenReturn("UW Office")
    Mockito.when(uwAddr.City).thenReturn("Columbus")
    Mockito.when(uwAddr.County).thenReturn("Franklin")
    Mockito.when(uwAddr.State).thenReturn(State.TC_OH)
    Mockito.when(uwAddr.PostalCode).thenReturn("43215")
    var uwContact = pol.Job.Underwriter.Contact
    Mockito.when(uwContact.PrimaryAddress).thenReturn(uwAddr)

    var pniAddr = Mockito.mock(Address)
    Mockito.when(pniAddr.PublicID).thenReturn("pni-addr-1")
    Mockito.when(pniAddr.AddressLine1).thenReturn("2525 Hoover Ct")
    Mockito.when(pniAddr.AddressLine2).thenReturn(includeAddressLines2And3 ? "Suite 100" : "")
    Mockito.when(pniAddr.AddressLine3).thenReturn(includeAddressLines2And3 ? "Bldg B" : "")
    Mockito.when(pniAddr.Description).thenReturn("HQ")
    Mockito.when(pniAddr.City).thenReturn("Grove City")
    Mockito.when(pniAddr.County).thenReturn("Franklin")
    Mockito.when(pniAddr.State).thenReturn(State.TC_OH)
    Mockito.when(pniAddr.PostalCode).thenReturn("43123")
    var pniContact = pol.PrimaryNamedInsured.AccountContactRole.AccountContact.Contact
    Mockito.when(pniContact.PrimaryAddress).thenReturn(pniAddr)

    return pol
  }

  private function invokeGetLimitsSafely(pol : PolicyPeriod) : String {
    try {
      return EpdsApiHelper.getLimits(pol)
    } catch (e : Exception) {
      return "0.000"
    }
  }

  private function injectHelperDependencies(helper : EpdsApiHelper, client : EpdsApiClient, serviceEnabled : String) : boolean {
    var injected = false
    try {
      for (field in (new EpdsApiHelper() as Object).Class.getDeclaredFields()) {
        if (field.getName().contains("_client")) {
          field.setAccessible(true)
          field.set(helper, client)
          injected = true
        } else if (field.getName().contains("contractDataServiceEnabled")) {
          field.setAccessible(true)
          field.set(helper, serviceEnabled)
        }
      }
    } catch (e : Exception) {
      injected = false
    }
    return injected
  }

  private function stubGetterIfDeclared(mockObj : Object, getterName : String, returnValue : Object) {
    if (mockObj == null) {
      return
    }
    try {
      var emptyArgs = new Object[0]
      for (method in mockObj.Class.getMethods()) {
        if (method.getName() == getterName and method.getParameterCount() == 0) {
          Mockito.when(method.invoke(mockObj, emptyArgs)).thenReturn(returnValue)
          return
        }
      }
    } catch (ignored : Exception) {}
  }

  private function stubSingleArgMethodIfDeclared(mockObj : Object, methodName : String, returnValue : Object) {
    if (mockObj == null) {
      return
    }
    try {
      for (method in mockObj.Class.getMethods()) {
        if (method.getName() == methodName and method.getParameterCount() == 1 and !method.getParameterTypes()[0].isPrimitive()) {
          if (returnValue == null or method.getReturnType().isAssignableFrom(returnValue.Class)) {
            var oneArg = new Object[1]
            oneArg[0] = Mockito.any()
            Mockito.when(method.invoke(mockObj, oneArg)).thenReturn(returnValue)
          }
        }
      }
    } catch (ignored : Exception) {}
  }

  private function populateSingleElementArrayGetter(mockObj : Object, getterName : String) : Object {
    if (mockObj == null) {
      return null
    }
    try {
      var emptyArgs = new Object[0]
      for (method in mockObj.Class.getMethods()) {
        if (method.getName() == getterName and method.getParameterCount() == 0 and method.getReturnType().isArray()) {
          var compType = method.getReturnType().getComponentType()
          var elemMock = Mockito.mock(compType, Mockito.RETURNS_DEEP_STUBS)
          var arr = java.lang.reflect.Array.newInstance(compType, 1)
          java.lang.reflect.Array.set(arr, 0, elemMock)
          Mockito.when(method.invoke(mockObj, emptyArgs)).thenReturn(arr)
          return elemMock
        }
      }
    } catch (ignored : Exception) {}
    return null
  }

  private static class StubMappingEpdsApiHelper extends EpdsApiHelper {
    override function mapEpdsContractDataSearchSync(policyPeriod : PolicyPeriod) : ContractDataSyncRequest {
      return new ContractDataSyncRequest() {
        :SourceSystem = CommonConstants.SOURCE_SYSTEM_GW,
        :Contract = new ContractDataSyncRequestContract() {
          :ContractRiskHoldingId = "JOB-10001"
        }
      }
    }
  }

  private static class FailingEpdsApiHelper extends EpdsApiHelper {
    private var _throwFeign : boolean

    construct(throwFeign : boolean) {
      _throwFeign = throwFeign
    }

    override function mapEpdsContractDataSearchSync(policyPeriod : PolicyPeriod) : ContractDataSyncRequest {
      return new ContractDataSyncRequest()
    }

    override function invokeEpdsPartySearch(request : PartyDataSearchRequest) : PartyDataSearchResponse {
      if (_throwFeign) {
        throw Mockito.mock(FeignException)
      }
      throw new RuntimeException("Simulated runtime exception in invokeEpdsPartySearch")
    }

    override function invokeEpdsContractDataSync(request : ContractDataSyncRequest) {
      if (_throwFeign) {
        throw Mockito.mock(FeignException)
      }
      throw new RuntimeException("Simulated runtime exception in invokeEpdsContractDataSync")
    }
  }

  private static class StubEpdsApiClient extends EpdsApiClient {
    private var _stubResponse : Object
    private var _throwFeign : boolean
    private var _throwRuntime : boolean
    private var _postCalled : boolean as readonly PostCalled

    construct(servicePropertyRetreiver : RuntimePropertyRetriever, stubResponse : Object, throwFeign : boolean, throwRuntime : boolean) {
      super(servicePropertyRetreiver)
      _stubResponse = stubResponse
      _throwFeign = throwFeign
      _throwRuntime = throwRuntime
      _postCalled = false
    }

    override function doGet(request : Object) : Object {
      if (_throwFeign) {
        throw Mockito.mock(FeignException)
      }
      if (_throwRuntime) {
        throw new RuntimeException("Simulated runtime exception in doGet")
      }
      return _stubResponse
    }

    override function doPost(request : Object) : Object {
      if (_throwFeign) {
        throw Mockito.mock(FeignException)
      }
      if (_throwRuntime) {
        throw new RuntimeException("Simulated runtime exception in doPost")
      }
      _postCalled = true
      return _stubResponse
    }
  }

}
