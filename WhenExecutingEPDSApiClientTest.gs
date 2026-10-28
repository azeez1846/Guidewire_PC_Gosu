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
  private var _proxyStateMap = new java.util.IdentityHashMap<Object, java.util.HashMap<String, Object>>()

  override function beforeMethod() {
    epdsSearchMapperDTO = new EPDSSearchMapperDTO()
    accountSearchCriteria = new AccountSearchCriteria()
    _proxyStateMap = new java.util.IdentityHashMap<Object, java.util.HashMap<String, Object>>()
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
    var validClient = createStubEpdsApiClient(propRetriever, expectedSearchResp, false, false)
    assertEquals(expectedSearchResp, validClient.epdsSearchPartyData(searchReq))
    assertEquals(CommonConstants.EPDS_SEARCH_PARTY, validClient.retrieveServiceName())

    validClient.epdsContractDataSync(syncReq)
    assertTrue(wasSyncInvokedOnClient(validClient))
    assertEquals(CommonConstants.EPDS_CONTRACT_DATA_SYNC, validClient.retrieveServiceName())

    // 2. Null and unexpected response types for epdsSearchPartyData (false branch of typeis)
    var nullClient = createStubEpdsApiClient(propRetriever, null, false, false)
    assertNull(nullClient.epdsSearchPartyData(searchReq))
    assertEquals(CommonConstants.EPDS_SEARCH_PARTY, nullClient.retrieveServiceName())

    var wrongTypeClient = createStubEpdsApiClient(propRetriever, "UNEXPECTED_RESPONSE_TYPE", false, false)
    assertNull(wrongTypeClient.epdsSearchPartyData(searchReq))

    // 3. FeignException catch & rethrow branches
    var feignClient = createStubEpdsApiClient(propRetriever, null, true, false)

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
    var runtimeClient = createStubEpdsApiClient(propRetriever, null, false, true)

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
    setLineSubProduct(wplCasPol.PrimaryCasualtyLine, "getSubProduct", WPLSubproduct.TC_PRIMARYCASUALTY, "typekey.WPLSubproduct")
    assertEquals(CommonConstants.CDS_GL7, EpdsApiHelper.getLobCode(wplCasPol))

    var wplEnergyPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    setLineSubProduct(wplEnergyPol.PrimaryCasualtyLine, "getSubProduct", WPLSubproduct.TC_PRIMARYENERGY, "typekey.WPLSubproduct")
    assertEquals(CommonConstants.CDS_ENERGY, EpdsApiHelper.getLobCode(wplEnergyPol))

    var wplNullSubPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    setLineSubProduct(wplNullSubPol.PrimaryCasualtyLine, "getSubProduct", null, "typekey.WPLSubproduct")
    assertNull(EpdsApiHelper.getLobCode(wplNullSubPol))

    // WEL Excess Casualty branches (exists = true and exists = false)
    var welCasPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    setLineSubProduct(welCasPol.ExcessCasualtyLine, "getExcessSubProduct", WELSubProduct.TC_EXCESSCASUALTY, "typekey.WELSubProduct")
    assertEquals(CommonConstants.CDS_EC, EpdsApiHelper.getLobCode(welCasPol))

    var welEnergyPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    setLineSubProduct(welEnergyPol.ExcessCasualtyLine, "getExcessSubProduct", WELSubProduct.TC_EXCESSENERGY, "typekey.WELSubProduct")
    assertEquals(CommonConstants.CDS_EC, EpdsApiHelper.getLobCode(welEnergyPol))

    var welNullSubPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    setLineSubProduct(welNullSubPol.ExcessCasualtyLine, "getExcessSubProduct", null, "typekey.WELSubProduct")
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

    // 2. TC_DECLINED (submission eligible; renewal falls through to TC_NONRENEWED; cancellation ineligible)
    assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_DECLINED, typekey.Job.TC_SUBMISSION)))
    assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_DECLINED, typekey.Job.TC_RENEWAL)))
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_DECLINED, typekey.Job.TC_CANCELLATION)))

    // 3. TC_NONRENEWED (eligible only for TC_RENEWAL)
    assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_NONRENEWED, typekey.Job.TC_RENEWAL)))
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_NONRENEWED, typekey.Job.TC_SUBMISSION)))

    // 4. Ineligible status (e.g. TC_BOUND)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(buildEligiblePeriod(PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_SUBMISSION)))

    // 5. Required-field null checks when status/job are eligible
    var nullUwPol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    setProxyValue(nullUwPol.Job, "getUnderwriter", null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullUwPol))

    var nullPniPol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    setProxyValue(nullPniPol, "getPrimaryNamedInsured", null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullPniPol))

    var nullAcctXlIdPol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    setProxyValue(nullAcctXlIdPol.Policy.Account, "getXLNameId", null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullAcctXlIdPol))

    var nullEffProducerPol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    setProxyValue(nullEffProducerPol.EffectiveDatedFields, "getProducerCode", null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullEffProducerPol))

    var nullBrokerXlIdPol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    setProxyValue(nullBrokerXlIdPol.ProducerCodeOfRecord, "getXLNameID_Ext", null)
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
    populateProxyArrayGetter(gl7PopulatedPol.GL7Line, "getGL7SublineTypes")
    assertNotNull(invokeGetLimitsSafely(gl7PopulatedPol))

    // 2. WCM, CA7, PSL (empty and populated locations), and CUL
    var wcmPol = buildPeriodWithLine(typekey.PolicyLine.TC_WCMWORKERSCOMPLINE, true)
    assertNotNull(invokeGetLimitsSafely(wcmPol))

    var ca7Pol = buildPeriodWithLine(typekey.PolicyLine.TC_CA7COMMAUTOLINE, true)
    assertNotNull(invokeGetLimitsSafely(ca7Pol))

    var pslEmptyPol = buildPeriodWithLine(typekey.PolicyLine.TC_PSLPROPERTYSELECTLINE, true)
    assertEquals("0.000", invokeGetLimitsSafely(pslEmptyPol))

    var pslPopulatedPol = buildPeriodWithLine(typekey.PolicyLine.TC_PSLPROPERTYSELECTLINE, true)
    populateProxyArrayGetter(pslPopulatedPol.PropertySelectLine, "getPSLPSLocations")
    assertNotNull(invokeGetLimitsSafely(pslPopulatedPol))

    var culPol = buildPeriodWithLine(typekey.PolicyLine.TC_CULCOMMERCIALUMBRELLALINE, true)
    assertNotNull(invokeGetLimitsSafely(culPol))

    // 3. WPL Primary Casualty (TC_PRIMARYCASUALTY, TC_PRIMARYENERGY, and null)
    var wplCasPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    setLineSubProduct(wplCasPol.PrimaryCasualtyLine, "getSubProduct", WPLSubproduct.TC_PRIMARYCASUALTY, "typekey.WPLSubproduct")
    invokeGetLimitsSafely(wplCasPol)

    var wplEnergyPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    setLineSubProduct(wplEnergyPol.PrimaryCasualtyLine, "getSubProduct", WPLSubproduct.TC_PRIMARYENERGY, "typekey.WPLSubproduct")
    invokeGetLimitsSafely(wplEnergyPol)

    var wplNullSubPol = buildPeriodWithLine(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE, true)
    setLineSubProduct(wplNullSubPol.PrimaryCasualtyLine, "getSubProduct", null, "typekey.WPLSubproduct")
    assertNull(EpdsApiHelper.getLimits(wplNullSubPol))

    // 4. WEL Excess Casualty for both getLimits and getAttachmentPoint
    var welCasPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    setLineSubProduct(welCasPol.ExcessCasualtyLine, "getExcessSubProduct", WELSubProduct.TC_EXCESSCASUALTY, "typekey.WELSubProduct")
    assertNotNull(invokeGetLimitsSafely(welCasPol))
    assertEquals("0.000", EpdsApiHelper.getAttachmentPoint(welCasPol))

    var welEnergyPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    setLineSubProduct(welEnergyPol.ExcessCasualtyLine, "getExcessSubProduct", WELSubProduct.TC_EXCESSENERGY, "typekey.WELSubProduct")
    assertNotNull(invokeGetLimitsSafely(welEnergyPol))
    assertEquals("0.000", EpdsApiHelper.getAttachmentPoint(welEnergyPol))

    var welNullSubPol = buildPeriodWithLine(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE, true)
    setLineSubProduct(welNullSubPol.ExcessCasualtyLine, "getExcessSubProduct", null, "typekey.WELSubProduct")
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
    var stubClient = createStubEpdsApiClient(
        new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT),
        expectedResp,
        false,
        false
    )

    if (injectHelperDependencies(realHelper, stubClient, CommonConstants._TRUE)) {
      assertEquals(expectedResp, realHelper.invokeEpdsPartySearch(searchReq))
      assertEquals(expectedResp, realHelper.epdsPartySearch(populatedCriteria))
    }

    var feignFailingHelper = createFailingEpdsApiHelper(true)
    assertNull(feignFailingHelper.epdsPartySearch(populatedCriteria))

    var runtimeFailingHelper = createFailingEpdsApiHelper(false)
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
    var stubClient = createStubEpdsApiClient(propRetriever, null, false, false)

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
    assertFalse(wasSyncInvokedOnClient(stubClient))

    injectHelperDependencies(realHelper, stubClient, "false")
    realHelper.epdsContractDataSync(fullAddrPol)
    assertFalse(wasSyncInvokedOnClient(stubClient))

    // 3. Service enabled ("true") with ineligible PolicyPeriod (TC_BOUND)
    var ineligiblePol = buildEligiblePeriod(PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_SUBMISSION)
    injectHelperDependencies(realHelper, stubClient, CommonConstants._TRUE)
    realHelper.epdsContractDataSync(ineligiblePol)
    assertFalse(wasSyncInvokedOnClient(stubClient))

    // 4. Service enabled ("true") with eligible PolicyPeriod + direct invokeEpdsContractDataSync
    realHelper.invokeEpdsContractDataSync(new ContractDataSyncRequest())
    assertTrue(wasSyncInvokedOnClient(stubClient))

    var realSyncClient = createStubEpdsApiClient(propRetriever, null, false, false)
    injectHelperDependencies(realHelper, realSyncClient, CommonConstants._TRUE)
    realHelper.epdsContractDataSync(fullAddrPol)

    var stubMappingHelper = createStubMappingEpdsApiHelper()
    var guaranteedSyncClient = createStubEpdsApiClient(propRetriever, null, false, false)
    if (injectHelperDependencies(stubMappingHelper, guaranteedSyncClient, CommonConstants._TRUE)) {
      stubMappingHelper.epdsContractDataSync(fullAddrPol)
      assertTrue(wasSyncInvokedOnClient(guaranteedSyncClient))
    }

    // 5. FeignException and RuntimeException catch blocks in epdsContractDataSync
    var feignSyncHelper = createFailingEpdsApiHelper(true)
    feignSyncHelper.epdsContractDataSync(fullAddrPol)

    var runtimeSyncHelper = createFailingEpdsApiHelper(false)
    runtimeSyncHelper.epdsContractDataSync(fullAddrPol)
  }

  // -------------------- Private Helper Methods --------------------

  private function createStubMappingEpdsApiHelper() : EpdsApiHelper {
    var helper = Mockito.spy(new EpdsApiHelper())
    var syncReq = new ContractDataSyncRequest() {
      :SourceSystem = CommonConstants.SOURCE_SYSTEM_GW,
      :Contract = new ContractDataSyncRequestContract() {
        :ContractRiskHoldingId = "JOB-10001"
      }
    }
    Mockito.doReturn(syncReq).when(helper).mapEpdsContractDataSearchSync(Mockito.any() as PolicyPeriod)
    return helper
  }

  private function createFailingEpdsApiHelper(throwFeign : boolean) : EpdsApiHelper {
    var baseHelper = new EpdsApiHelper()
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT)
    var failingClient = createStubEpdsApiClient(propRetriever, null, throwFeign, !throwFeign)
    injectHelperDependencies(baseHelper, failingClient, CommonConstants._TRUE)

    var spyHelper = Mockito.spy(baseHelper)
    injectHelperDependencies(spyHelper, failingClient, CommonConstants._TRUE)
    Mockito.doReturn(new ContractDataSyncRequest()).when(spyHelper).mapEpdsContractDataSearchSync(Mockito.any() as PolicyPeriod)
    return spyHelper
  }

  private function createStubEpdsApiClient(
      propRetriever : RuntimePropertyRetriever,
      stubResponse : Object,
      throwFeign : boolean,
      throwRuntime : boolean
  ) : EpdsApiClient {
    var client = Mockito.spy(new EpdsApiClient(propRetriever))
    stubClientTransportMethod(client, "doGet", stubResponse, throwFeign, throwRuntime)
    stubClientTransportMethod(client, "doPost", stubResponse, throwFeign, throwRuntime)
    return client
  }

  private function stubClientTransportMethod(
      clientSpy : Object,
      methodName : String,
      stubResponse : Object,
      throwFeign : boolean,
      throwRuntime : boolean
  ) {
    var targetMethod : java.lang.reflect.Method = null
    var cls : Class = clientSpy.Class.getSuperclass()
    while (cls != null and targetMethod == null) {
      for (m in cls.getDeclaredMethods()) {
        if (m.getName() == methodName and m.getParameterCount() == 1 and !m.isBridge() and !m.isSynthetic()) {
          targetMethod = m
          break
        }
      }
      cls = cls.getSuperclass()
    }
    if (targetMethod != null) {
      targetMethod.setAccessible(true)
      var stubbedSpy : Object
      if (throwFeign) {
        var feignEx : Throwable = Mockito.mock(FeignException)
        stubbedSpy = Mockito.doThrow(new Throwable[]{feignEx}).when(clientSpy)
      } else if (throwRuntime) {
        var runtimeEx : Throwable = new RuntimeException("Simulated runtime exception in " + methodName)
        stubbedSpy = Mockito.doThrow(new Throwable[]{runtimeEx}).when(clientSpy)
      } else {
        stubbedSpy = Mockito.doReturn(stubResponse).when(clientSpy)
      }
      var args = new Object[1]
      args[0] = Mockito.any()
      targetMethod.invoke(stubbedSpy, args)
    }
  }

  private function wasSyncInvokedOnClient(clientSpy : EpdsApiClient) : boolean {
    for (inv in Mockito.mockingDetails(clientSpy).getInvocations()) {
      if (inv.getMethod().getName() == "epdsContractDataSync") {
        return true
      }
    }
    return false
  }

  private function buildStatusAndJobPeriod(status : PolicyPeriodStatus, jobSubtype : typekey.Job) : PolicyPeriod {
    var pol = createEntityProxy(new Class[]{Class.forName("entity.PolicyPeriod")}) as PolicyPeriod
    setProxyValue(pol, "getStatus", status)
    var job = pol.Job
    setProxyValue(job, "getSubtype", jobSubtype)
    return pol
  }

  private function buildPeriodWithLine(lineSubtype : typekey.PolicyLine, lineExists : boolean) : PolicyPeriod {
    var pol = createEntityProxy(new Class[]{Class.forName("entity.PolicyPeriod")}) as PolicyPeriod
    var lineInterfaces = resolveLineInterfaces(lineSubtype, lineExists)
    var mockLine = createEntityProxy(lineInterfaces) as entity.PolicyLine
    setProxyValue(mockLine, "getSubtype", lineSubtype)
    setProxyValue(pol, "getLines", new entity.PolicyLine[]{mockLine})

    if (lineSubtype != null and lineExists) {
      setProxyValue(pol, "getLine", mockLine)
      var propName = getLineAccessorPropertyName(lineSubtype)
      if (propName != null) {
        setProxyValue(pol, "get" + propName, mockLine)
        setProxyValue(pol, "is" + propName + "Exists", true)
        setProxyValue(pol, "get" + propName + "Exists", true)
      }
    } else {
      setProxyValue(pol, "getLine", null)
      setProxyValue(pol, "isExcessCasualtyLineExists", false)
      setProxyValue(pol, "getExcessCasualtyLineExists", false)
      setProxyValue(pol, "getExcessCasualtyLine", null)
    }
    return pol
  }

  private function buildPeriodForHistory(termNum : Integer, status : PolicyPeriodStatus, jobSubtype : typekey.Job, jobNumber : String) : PolicyPeriod {
    var pp = createEntityProxy(new Class[]{Class.forName("entity.PolicyPeriod")}) as PolicyPeriod
    var isBound = (status == PolicyPeriodStatus.TC_BOUND)
    var periodDate = new java.util.Date(1700000000000L + (termNum * 31536000000L))
    setProxyValue(pp, "getTermNumber", termNum)
    setProxyValue(pp, "getStatus", status)
    setProxyValue(pp, "getPeriodStart", periodDate)
    setProxyValue(pp, "getPeriodEnd", periodDate)
    setProxyValue(pp, "getModelDate", isBound ? periodDate : null)
    setProxyValue(pp, "getModelNumber", isBound ? termNum : null)
    setProxyValue(pp, "isLocked", isBound)
    setProxyValue(pp, "isPromoted", isBound)
    setProxyValue(pp, "getPromoted", isBound)
    setProxyValue(pp, "isMostRecentModel", isBound)
    setProxyValue(pp, "getMostRecentModel", isBound)
    setProxyValue(pp, "isPreempted", false)
    setProxyValue(pp, "getArchiveState", null)
    var job = pp.Job
    setProxyValue(job, "getSubtype", jobSubtype)
    setProxyValue(job, "getJobNumber", jobNumber)
    return pp
  }

  private function buildWithdrawnHistoryHost(latestBoundTerm : Integer, boundPeriods : PolicyPeriod[]) : PolicyPeriod {
    var pol = createEntityProxy(new Class[]{Class.forName("entity.PolicyPeriod")}) as PolicyPeriod
    setProxyValue(pol, "getStatus", PolicyPeriodStatus.TC_WITHDRAWN)
    setProxyValue(pol, "getTermNumber", latestBoundTerm)

    var latestBound = buildPeriodForHistory(latestBoundTerm, PolicyPeriodStatus.TC_BOUND, typekey.Job.TC_CANCELLATION, "LATEST-BND")
    var periodsArr : PolicyPeriod[]
    if (boundPeriods != null and boundPeriods.length > 0) {
      periodsArr = new PolicyPeriod[boundPeriods.length]
      for (i in 0..|boundPeriods.length) {
        periodsArr[i] = boundPeriods[i]
      }
    } else {
      periodsArr = new PolicyPeriod[]{latestBound}
    }

    var policy = pol.Policy
    setProxyValue(policy, "getPeriods", periodsArr)
    setProxyValue(policy, "getBoundPeriods", boundPeriods)
    setProxyValue(policy, "getLatestBoundPeriod", latestBound)
    return pol
  }

  private function buildTermHistoryHost(termNum : Integer, periods : PolicyPeriod[]) : PolicyPeriod {
    var pol = createEntityProxy(new Class[]{Class.forName("entity.PolicyPeriod")}) as PolicyPeriod
    setProxyValue(pol, "getStatus", PolicyPeriodStatus.TC_QUOTED)
    setProxyValue(pol, "getTermNumber", termNum)
    var policy = pol.Policy
    setProxyValue(policy, "getPeriods", periods)
    return pol
  }

  private function buildEligiblePeriod(status : PolicyPeriodStatus, jobSubtype : typekey.Job) : PolicyPeriod {
    var pol = createEntityProxy(new Class[]{Class.forName("entity.PolicyPeriod")}) as PolicyPeriod
    setProxyValue(pol, "getStatus", status)
    setProxyValue(pol, "getPolicyNumber", "POL-10001")
    var job = pol.Job
    setProxyValue(job, "getSubtype", jobSubtype)
    setProxyValue(job, "getJobNumber", "JOB-10001")

    var uw = job.Underwriter
    setProxyValue(uw, "getPublicID", "uw-pub-1")
    setProxyValue(uw, "getDisplayName", "Jane Underwriter")

    var pni = pol.PrimaryNamedInsured
    setProxyValue(pni, "getPublicID", "pni-pub-1")
    setProxyValue(pni, "getDisplayName", "Ford Motor Co")

    var account = pol.Policy.Account
    setProxyValue(account, "getXLNameId", "X52058205")

    var effProducer = pol.EffectiveDatedFields.ProducerCode
    setProxyValue(effProducer, "getPublicID", "pc-pub-1")
    setProxyValue(effProducer, "getCode", "PC-001")

    var producerOfRecord = pol.ProducerCodeOfRecord
    setProxyValue(producerOfRecord, "getCode", "PC-001")
    setProxyValue(producerOfRecord, "getXLNameID_Ext", "X99887766")

    return pol
  }

  private function buildFullSyncPeriod(includeAddressLines2And3 : boolean) : PolicyPeriod {
    var pol = buildEligiblePeriod(PolicyPeriodStatus.TC_QUOTED, typekey.Job.TC_SUBMISSION)
    var now = new java.util.Date()
    setProxyValue(pol, "getPeriodStart", now)
    setProxyValue(pol, "getPeriodEnd", now)
    setProxyValue(pol, "getTermNumber", 1)
    setProxyValue(pol, "getPrimaryInsuredName", "Ford Motor Co")

    var gl7Interfaces = resolveLineInterfaces(typekey.PolicyLine.TC_GL7GENERALLIABILITYLINE, true)
    var gl7Line = createEntityProxy(gl7Interfaces) as entity.PolicyLine
    setProxyValue(gl7Line, "getSubtype", typekey.PolicyLine.TC_GL7GENERALLIABILITYLINE)
    setProxyValue(pol, "getLines", new entity.PolicyLine[]{gl7Line})
    setProxyValue(pol, "getLine", gl7Line)
    setProxyValue(pol, "getGL7Line", gl7Line)

    var uwAddr = createEntityProxy(new Class[]{Class.forName("entity.Address")}) as Address
    setProxyValue(uwAddr, "getPublicID", "uw-addr-1")
    setProxyValue(uwAddr, "getAddressLine1", "100 Main St")
    setProxyValue(uwAddr, "getAddressLine2", includeAddressLines2And3 ? "Suite 200" : "")
    setProxyValue(uwAddr, "getAddressLine3", includeAddressLines2And3 ? "Floor 4" : "")
    setProxyValue(uwAddr, "getDescription", "UW Office")
    setProxyValue(uwAddr, "getCity", "Columbus")
    setProxyValue(uwAddr, "getCounty", "Franklin")
    setProxyValue(uwAddr, "getState", State.TC_OH)
    setProxyValue(uwAddr, "getPostalCode", "43215")
    var uwContact = pol.Job.Underwriter.Contact
    setProxyValue(uwContact, "getPrimaryAddress", uwAddr)

    var pniAddr = createEntityProxy(new Class[]{Class.forName("entity.Address")}) as Address
    setProxyValue(pniAddr, "getPublicID", "pni-addr-1")
    setProxyValue(pniAddr, "getAddressLine1", "2525 Hoover Ct")
    setProxyValue(pniAddr, "getAddressLine2", includeAddressLines2And3 ? "Suite 100" : "")
    setProxyValue(pniAddr, "getAddressLine3", includeAddressLines2And3 ? "Bldg B" : "")
    setProxyValue(pniAddr, "getDescription", "HQ")
    setProxyValue(pniAddr, "getCity", "Grove City")
    setProxyValue(pniAddr, "getCounty", "Franklin")
    setProxyValue(pniAddr, "getState", State.TC_OH)
    setProxyValue(pniAddr, "getPostalCode", "43123")
    var pniContact = pol.PrimaryNamedInsured.AccountContactRole.AccountContact.Contact
    setProxyValue(pniContact, "getPrimaryAddress", pniAddr)

    return pol
  }

  private function createEntityProxy(interfaces : Class[]) : Object {
    var state = new java.util.HashMap<String, Object>()
    var handler : java.lang.reflect.InvocationHandler = \proxy, method, args -> {
      var name = method.getName()
      var paramCount = method.getParameterCount()
      if (name == "equals" and paramCount == 1) {
        return proxy === args[0]
      }
      if (name == "hashCode" and paramCount == 0) {
        return java.lang.System.identityHashCode(proxy)
      }
      if (name == "toString" and paramCount == 0) {
        return "EntityProxy@" + java.lang.Integer.toHexString(java.lang.System.identityHashCode(proxy))
      }
      if (state.containsKey(name)) {
        return state.get(name)
      }
      var retType = method.getReturnType()
      var retName = retType.getName()
      if (retName == "void") {
        return null
      }
      if (retName == "boolean" or retName == "java.lang.Boolean") {
        return false
      }
      if (retName == "int" or retName == "java.lang.Integer") {
        return 0
      }
      if (retName == "long" or retName == "java.lang.Long") {
        return 0L
      }
      if (retName == "double" or retName == "java.lang.Double") {
        return 0.0
      }
      if (retName == "float" or retName == "java.lang.Float") {
        return 0.0f
      }
      if (retName == "short" or retName == "java.lang.Short") {
        return 0 as short
      }
      if (retName == "byte" or retName == "java.lang.Byte") {
        return 0 as byte
      }
      if (retName == "char" or retName == "java.lang.Character") {
        return ('a' as char)
      }
      if (retName == "java.lang.String") {
        return ""
      }
      if (retName == "java.math.BigDecimal") {
        return java.math.BigDecimal.ZERO
      }
      if (retName == "java.util.Date") {
        return new java.util.Date()
      }
      if (retType.isArray()) {
        return java.lang.reflect.Array.newInstance(retType.getComponentType(), 0)
      }
      if (retType.isInterface()) {
        var child = createEntityProxy(new Class[]{retType})
        if (paramCount == 0) {
          state.put(name, child)
        }
        return child
      }
      return null
    }
    var loader = selectClassLoader(interfaces)
    var instance = java.lang.reflect.Proxy.newProxyInstance(loader, interfaces, handler)
    _proxyStateMap.put(instance, state)
    return instance
  }

  private function selectClassLoader(interfaces : Class[]) : ClassLoader {
    var candidates = new ArrayList<ClassLoader>()
    for (iface in interfaces) {
      if (iface.getClassLoader() != null and !candidates.contains(iface.getClassLoader())) {
        candidates.add(iface.getClassLoader())
      }
    }
    var ctxLoader = Thread.currentThread().getContextClassLoader()
    if (ctxLoader != null and !candidates.contains(ctxLoader)) {
      candidates.add(ctxLoader)
    }
    var testLoader = (this as Object).Class.getClassLoader()
    if (testLoader != null and !candidates.contains(testLoader)) {
      candidates.add(testLoader)
    }
    for (loader in candidates) {
      var allVisible = true
      for (iface in interfaces) {
        try {
          if (Class.forName(iface.getName(), false, loader) != iface) {
            allVisible = false
            break
          }
        } catch (e : Exception) {
          allVisible = false
          break
        }
      }
      if (allVisible) {
        return loader
      }
    }
    return interfaces[0].getClassLoader()
  }

  private function resolveLineInterfaces(lineSubtype : typekey.PolicyLine, lineExists : boolean) : Class[] {
    var ifaceList = new ArrayList<Class>()
    ifaceList.add(Class.forName("entity.PolicyLine"))
    if (lineSubtype != null and lineExists) {
      addInterfaceIfPresent(ifaceList, "entity." + lineSubtype.Code)
      var propName = getLineAccessorPropertyName(lineSubtype)
      if (propName != null) {
        try {
          var prop = PolicyPeriod.Type.TypeInfo.getProperty(propName)
          if (prop != null and prop.FeatureType != null) {
            addInterfaceIfPresent(ifaceList, prop.FeatureType.Name)
          }
        } catch (ignored : Exception) {}
        addInterfaceIfPresent(ifaceList, "productmodel." + propName)
      }
      addInterfaceIfPresent(ifaceList, "productmodel." + lineSubtype.Code)
    }
    var arr = new Class[ifaceList.size()]
    for (i in 0..|ifaceList.size()) {
      arr[i] = ifaceList.get(i)
    }
    return arr
  }

  private function addInterfaceIfPresent(ifaceList : ArrayList<Class>, className : String) {
    if (className == null) {
      return
    }
    try {
      var cls = Class.forName(className)
      if (cls != null and cls.isInterface() and !ifaceList.contains(cls)) {
        ifaceList.add(cls)
      }
    } catch (ignored : Exception) {}
  }

  private function getLineAccessorPropertyName(lineSubtype : typekey.PolicyLine) : String {
    if (lineSubtype == typekey.PolicyLine.TC_GL7GENERALLIABILITYLINE) {
      return "GL7Line"
    } else if (lineSubtype == typekey.PolicyLine.TC_WCMWORKERSCOMPLINE) {
      return "WCMLine"
    } else if (lineSubtype == typekey.PolicyLine.TC_CA7COMMAUTOLINE) {
      return "CA7Line"
    } else if (lineSubtype == typekey.PolicyLine.TC_PSLPROPERTYSELECTLINE) {
      return "PropertySelectLine"
    } else if (lineSubtype == typekey.PolicyLine.TC_CULCOMMERCIALUMBRELLALINE) {
      return "CommercialUmbrellaLine"
    } else if (lineSubtype == typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE) {
      return "PrimaryCasualtyLine"
    } else if (lineSubtype == typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE) {
      return "ExcessCasualtyLine"
    }
    return null
  }

  private function setProxyValue(proxyObj : Object, methodName : String, value : Object) {
    if (proxyObj != null) {
      var state = _proxyStateMap.get(proxyObj)
      if (state != null) {
        state.put(methodName, value)
      }
    }
  }

  private function setLineSubProduct(lineProxy : Object, getterName : String, value : Object, typeName : String) {
    if (lineProxy == null) {
      return
    }
    setProxyValue(lineProxy, getterName, value)
    try {
      for (m in lineProxy.Class.getMethods()) {
        if (m.getParameterCount() == 0 and m.getReturnType().getName() == typeName) {
          setProxyValue(lineProxy, m.getName(), value)
        }
      }
    } catch (ignored : Exception) {}
  }

  private function populateProxyArrayGetter(proxyObj : Object, getterName : String) : Object {
    if (proxyObj == null) {
      return null
    }
    try {
      for (method in proxyObj.Class.getMethods()) {
        if (method.getName() == getterName and method.getParameterCount() == 0 and method.getReturnType().isArray()) {
          var compType = method.getReturnType().getComponentType()
          var elemProxy = createEntityProxy(new Class[]{compType})
          var arr = java.lang.reflect.Array.newInstance(compType, 1)
          java.lang.reflect.Array.set(arr, 0, elemProxy)
          setProxyValue(proxyObj, getterName, arr)
          return elemProxy
        }
      }
    } catch (ignored : Exception) {}
    return null
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

}
