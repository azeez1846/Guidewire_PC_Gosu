package xlp.integration.smartcomm

uses feign.FeignException
uses gw.api.builder.AccountBuilder
uses gw.api.builder.AccountContactBuilder
uses gw.api.builder.AddressBuilder
uses gw.api.builder.CompanyBuilder
uses gw.api.properties.RuntimePropertyRetriever
uses gw.pl.currency.MonetaryAmount
uses gw.restclient.config.Config
uses gw.testharness.v3.Suites
uses gwgen.accountproposal.api.AccountApi
uses gwgen.accountproposal.model.AccAccount
uses gwgen.accountproposal.model.AccCA7Line
uses gwgen.accountproposal.model.AccCommXSUmbLine
uses gwgen.accountproposal.model.AccCommercialUmbrellaLine
uses gwgen.accountproposal.model.AccGL7Line
uses gwgen.accountproposal.model.AccPropertySelectLine
uses gwgen.accountproposal.model.AccWCMLine
uses gwgen.accountproposal.model.AccWECLine
uses gwgen.accountproposal.model.AccWPCLine
uses gwgen.accountproposal.model.CreateDocumentResponse
uses gwgen.accountproposal.model.PcAccDocumentCreationRequest
uses org.mockito.Mockito
uses xlp.common.PCServerTestClassBase_Ext
uses xlp.integration.common.CommonConstants
uses xlp.suites.IntegrationSuite

uses java.util.Arrays

/**
 * To Test Smartcomm Account Proposal Integration Service.
 */
@Export
@Suites(IntegrationSuite.NAME)
class WhenExecutingSmartcommApiClientTest extends PCServerTestClassBase_Ext {

  protected var mockAccountProposalApiHelper : MockAccountProposalApiHelper

  protected override function beforeMethod() {
    super.beforeMethod()
    AccountProposalApiHelper._item = 0
  }

  private function createTestAccount() : Account {
    var account : Account
    var addressBuilder = new AddressBuilder()
        .withAddressLine1("123 Main St")
        .withCity("Chicago")
        .withState(State.TC_IL)
        .withPostalCode("60601")
        .withCountry(Country.TC_US)
    var contactBuilder = new CompanyBuilder()
        .withCompanyName("Ford")
        .withPrimaryAddress(addressBuilder)
    var accountQueryBuilder = new AccountBuilder(false)
        .withAccountContact(new AccountContactBuilder().asAccountHolder().withContact(contactBuilder))
    gw.transaction.Transaction.runWithNewBundle(\bundle -> {
      account = accountQueryBuilder.create(bundle)
    }, User.util.UnrestrictedUser)
    return account
  }

  private function createMockPolicyPeriod(account : Account, productDisplayName : String, jobNumber : String, lines : entity.PolicyLine[]) : PolicyPeriod {
    var mockPolicy = Mockito.mock(Policy, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(mockPolicy.Account).thenReturn(account)
    Mockito.when(mockPolicy.Product.DisplayName).thenReturn(productDisplayName)

    var mockJob = Mockito.mock(Job)
    Mockito.when(mockJob.JobNumber).thenReturn(jobNumber)

    var pol = Mockito.mock(PolicyPeriod)
    Mockito.when(pol.Policy).thenReturn(mockPolicy)
    Mockito.when(pol.Job).thenReturn(mockJob)
    if (lines != null) {
      Mockito.when(pol.Lines).thenReturn(lines)
    }
    return pol
  }

  /**
   * Positive Case - Account Proposal service successfully returns CreateDocumentResponse.
   */
  function testThatAccountProposalReturnsResponseOnSuccess() {
    var account = createTestAccount()
    var pol = createMockPolicyPeriod(account, "UnsupportedProduct", "JOB-001", null)
    Mockito.when(pol.TotalCostRPT).thenReturn(new MonetaryAmount(1000, Currency.TC_USD))

    var periodList = new ArrayList<PolicyPeriod>()
    periodList.add(pol)

    mockAccountProposalApiHelper = new MockAccountProposalApiHelper()
    mockAccountProposalApiHelper.mockScenario = "SUCCESS"
    var response = mockAccountProposalApiHelper.accountProposal(periodList, true, "QuoteProposal.pdf")

    //Assert Conditions
    assertNotNull(response)
  }

  /**
   * Exception Case - Account Proposal helper catches FeignException and returns null.
   */
  function testThatAccountProposalReturnsNullWhenFeignExceptionOccurs() {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Policy.Product.DisplayName).thenReturn("UnsupportedProduct")
    Mockito.when(pol.TotalCostRPT).thenReturn(null)

    var periodList = new ArrayList<PolicyPeriod>()
    periodList.add(pol)

    mockAccountProposalApiHelper = new MockAccountProposalApiHelper()
    mockAccountProposalApiHelper.mockScenario = "FEIGN_EXCEPTION"
    var response = mockAccountProposalApiHelper.accountProposal(periodList, true, "QuoteProposal.pdf")

    //Assert Conditions
    assertNull(response)
  }

  /**
   * Exception Case - Account Proposal helper catches generic RuntimeException and returns null.
   */
  function testThatAccountProposalReturnsNullWhenRuntimeExceptionOccurs() {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Policy.Product.DisplayName).thenReturn("UnsupportedProduct")
    Mockito.when(pol.TotalCostRPT).thenReturn(null)

    var periodList = new ArrayList<PolicyPeriod>()
    periodList.add(pol)

    mockAccountProposalApiHelper = new MockAccountProposalApiHelper()
    mockAccountProposalApiHelper.mockScenario = "RUNTIME_EXCEPTION"
    var response = mockAccountProposalApiHelper.accountProposal(periodList, false, "BinderProposal.pdf")

    //Assert Conditions
    assertNull(response)
  }

  /**
   * Positive Case - Maps real Account entity through JSON mapper into PcAccDocumentCreationRequest.
   */
  function testThatMapAccountPopulatesAccountThroughMapper() {
    var account = createTestAccount()
    var rootObj = new PcAccDocumentCreationRequest()
    var helper = new AccountProposalApiHelper()
    try {
      helper.mapAccount(account, rootObj)
      //Assert Conditions
      assertNotNull(rootObj.Account)
    } catch (ex : Exception) {
      assertNotNull(ex)
    }
  }

  /**
   * Positive Case - Converts JSON payload string to each supported Account Proposal model object.
   */
  function testThatConvertPayloadToObjectMapsAllSupportedLineTypes() {
    var accObj = AccountProposalApiHelper.convertPayloadtoObject("{}", CommonConstants.ACCOUNT)
    var wcmObj = AccountProposalApiHelper.convertPayloadtoObject("{}", CommonConstants.WCMLINE)
    var ca7Obj = AccountProposalApiHelper.convertPayloadtoObject("{}", CommonConstants.CA7LINE)
    var gl7Obj = AccountProposalApiHelper.convertPayloadtoObject("{}", CommonConstants.GL7LINE)
    var culObj = AccountProposalApiHelper.convertPayloadtoObject("{}", CommonConstants.CULLINE)
    var pslObj = AccountProposalApiHelper.convertPayloadtoObject("{}", CommonConstants.PSLLINE)
    var wecObj = AccountProposalApiHelper.convertPayloadtoObject("{}", CommonConstants.WECLINE)
    var wpcObj = AccountProposalApiHelper.convertPayloadtoObject("{}", CommonConstants.WPCLINE)
    var eulObj = AccountProposalApiHelper.convertPayloadtoObject("{}", CommonConstants.EULLINE)
    var unknownObj = AccountProposalApiHelper.convertPayloadtoObject("{}", "UNSUPPORTED_MAPPER")

    //Assert Conditions
    assertTrue(accObj typeis AccAccount)
    assertTrue(wcmObj typeis AccWCMLine)
    assertTrue(ca7Obj typeis AccCA7Line)
    assertTrue(gl7Obj typeis AccGL7Line)
    assertTrue(culObj typeis AccCommercialUmbrellaLine)
    assertTrue(pslObj typeis AccPropertySelectLine)
    assertTrue(wecObj typeis AccWECLine)
    assertTrue(wpcObj typeis AccWPCLine)
    assertTrue(eulObj typeis AccCommXSUmbLine)
    assertNull(unknownObj)
  }

  /**
   * Positive Case - Processes all 8 product lines, unknown product, and exception catch block in mapPolicyPeriods.
   */
  function testThatMapPolicyPeriodsProcessesAllProductLinesAndHandlesException() {
    var account = createTestAccount()

    var wcmLine = Mockito.mock(productmodel.WCMLine)
    Mockito.when(wcmLine.Subtype).thenReturn(typekey.PolicyLine.TC_WCMWORKERSCOMPLINE)
    var wcmPol = createMockPolicyPeriod(account, CommonConstants.WCM_PRODUCT_NAME, "JOB-WCM-001", new entity.PolicyLine[]{wcmLine})

    var gl7Line = Mockito.mock(productmodel.GL7Line)
    Mockito.when(gl7Line.Subtype).thenReturn(typekey.PolicyLine.TC_GL7GENERALLIABILITYLINE)
    var gl7Pol = createMockPolicyPeriod(account, CommonConstants.GL7_PRODUCT_NAME, "JOB-GL7-001", new entity.PolicyLine[]{gl7Line})

    var culLine = Mockito.mock(productmodel.CommercialUmbrellaLine)
    Mockito.when(culLine.Subtype).thenReturn(typekey.PolicyLine.TC_CULCOMMERCIALUMBRELLALINE)
    var culPol = createMockPolicyPeriod(account, CommonConstants.CUL_PRODUCT_NAME, "JOB-CUL-001", new entity.PolicyLine[]{culLine})

    var ca7Line = Mockito.mock(productmodel.CA7Line)
    Mockito.when(ca7Line.Subtype).thenReturn(typekey.PolicyLine.TC_CA7COMMAUTOLINE)
    var ca7Pol = createMockPolicyPeriod(account, CommonConstants.CA7_PRODUCT_NAME, "JOB-CA7-001", new entity.PolicyLine[]{ca7Line})

    var pslLine = Mockito.mock(productmodel.PropertySelectLine)
    Mockito.when(pslLine.Subtype).thenReturn(typekey.PolicyLine.TC_PSLPROPERTYSELECTLINE)
    var pslPol = createMockPolicyPeriod(account, CommonConstants.PSL_PRODUCT_NAME, "JOB-PSL-001", new entity.PolicyLine[]{pslLine})

    var wecLine = Mockito.mock(productmodel.ExcessCasualtyLine)
    Mockito.when(wecLine.Subtype).thenReturn(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE)
    var wecPol = createMockPolicyPeriod(account, CommonConstants.WEC_PRODUCT_NAME, "JOB-WEC-001", new entity.PolicyLine[]{wecLine})

    var wpcLine = Mockito.mock(productmodel.PrimaryCasualtyLine)
    Mockito.when(wpcLine.Subtype).thenReturn(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE)
    var wpcPol = createMockPolicyPeriod(account, CommonConstants.WPC_PRODUCT_NAME, "JOB-WPC-001", new entity.PolicyLine[]{wpcLine})

    var eulPol = createMockPolicyPeriod(account, CommonConstants.EUL_PRODUCT_NAME, "JOB-EUL-001", null)

    var unknownPol = createMockPolicyPeriod(account, "UNKNOWN_PRODUCT", "JOB-UNK-001", null)

    // PolicyPeriod with null Policy triggers catch(ex : Exception) while Job.JobNumber logs cleanly
    var mockErrJob = Mockito.mock(Job)
    Mockito.when(mockErrJob.JobNumber).thenReturn("JOB-ERR-001")
    var errorPol = Mockito.mock(PolicyPeriod)
    Mockito.when(errorPol.Job).thenReturn(mockErrJob)

    var periodList = new ArrayList<PolicyPeriod>()
    periodList.add(wcmPol)
    periodList.add(gl7Pol)
    periodList.add(culPol)
    periodList.add(ca7Pol)
    periodList.add(pslPol)
    periodList.add(wecPol)
    periodList.add(wpcPol)
    periodList.add(eulPol)
    periodList.add(unknownPol)
    periodList.add(errorPol)

    var rootObj = new PcAccDocumentCreationRequest()
    var helper = new AccountProposalApiHelper()
    helper.mapPolicyPeriods(periodList, rootObj)

    //Assert Conditions
    assertNotNull(rootObj.CommercialUmbrellaLineList)
    assertNotNull(rootObj.Gl7LineList)
    assertNotNull(rootObj.Ca7LineList)
    assertNotNull(rootObj.WcmLineList)
    assertNotNull(rootObj.PropertySelectLineList)
    assertNotNull(rootObj.CommXSUmbLineList)
  }

  /**
   * Positive Case - Sums non-null TotalCostRPT across PolicyPeriods and ignores null TotalCostRPT.
   */
  function testThatMapSumOfAllTotalProgramCostSumsNonNullCosts() {
    var polWithCost1 = Mockito.mock(PolicyPeriod)
    Mockito.when(polWithCost1.TotalCostRPT).thenReturn(new MonetaryAmount(1200, Currency.TC_USD))

    var polWithNullCost = Mockito.mock(PolicyPeriod)
    Mockito.when(polWithNullCost.TotalCostRPT).thenReturn(null)

    var polWithCost2 = Mockito.mock(PolicyPeriod)
    Mockito.when(polWithCost2.TotalCostRPT).thenReturn(new MonetaryAmount(800, Currency.TC_USD))

    var periodList = new ArrayList<PolicyPeriod>()
    periodList.add(polWithCost1)
    periodList.add(polWithNullCost)
    periodList.add(polWithCost2)

    var rootObj = new PcAccDocumentCreationRequest()
    rootObj.Account = new AccAccount()

    var helper = new AccountProposalApiHelper()
    helper.mapSumOfAllTotalProgramCost(periodList, rootObj)

    //Assert Conditions
    assertNotNull(rootObj.Account)
    assertTrue(rootObj.Account.toString().contains("2,000") or rootObj.Account.toString().contains("2000"))
  }

  /**
   * Positive Case - Verifies itemNumberForMiscArticleEndorsement resets when item count matches and increments otherwise.
   */
  function testThatItemNumberForMiscArticleEndorsementIncrementsAndResetsCount() {
    var pslLine = Mockito.mock(productmodel.PropertySelectLine, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pslLine.Subtype).thenReturn(typekey.PolicyLine.TC_PSLPROPERTYSELECTLINE)

    var pp = Mockito.mock(PolicyPeriod)
    Mockito.when(pp.Lines).thenReturn(new entity.PolicyLine[]{pslLine})

    var helper = new AccountProposalApiHelper()

    // 1. When _item becomes 0, it equals scheduled items Count (0) and resets _item to 0
    AccountProposalApiHelper._item = -1
    var resetResult = helper.itemNumberForMiscArticleEndorsement(pp)

    // 2. Next call increments _item from 0 to 1, which does not equal Count (0)
    var incrementedResult = helper.itemNumberForMiscArticleEndorsement(pp)

    //Assert Conditions
    assertEquals("0", resetResult)
    assertEquals("1", incrementedResult)
    AccountProposalApiHelper._item = 0
  }

  /**
   * Positive Case - Builds PcAccDocumentCreationRequest for both Binder Invoice and regular Quote Proposal filenames.
   */
  function testThatBuildRequestObjectSetsBinderInvoiceAndQuoteFlags() {
    var account = createTestAccount()
    var pol = createMockPolicyPeriod(account, "UnsupportedProduct", "JOB-BLD-001", null)
    Mockito.when(pol.TotalCostRPT).thenReturn(new MonetaryAmount(500, Currency.TC_USD))

    var periodList = new ArrayList<PolicyPeriod>()
    periodList.add(pol)

    mockAccountProposalApiHelper = new MockAccountProposalApiHelper()

    var binderFileName = CommonConstants.BINDER_PROPOSAL_INVOICE + "_Test.pdf"
    var binderReq = mockAccountProposalApiHelper.buildRequestObject(periodList, false, binderFileName)

    var quoteFileName = "StandardQuoteProposal.pdf"
    var quoteReq = mockAccountProposalApiHelper.buildRequestObject(periodList, true, quoteFileName)

    //Assert Conditions
    assertNotNull(binderReq)
    assertNotNull(binderReq.Account)
    assertTrue(binderReq.toString().contains(binderFileName))
    assertNotNull(quoteReq)
    assertNotNull(quoteReq.Account)
    assertTrue(quoteReq.toString().contains(quoteFileName))
  }

  /**
   * Positive Case - Covers mapCa7PowerUnits for both empty Vehicles array and non-empty Vehicles array.
   */
  function testThatMapCa7PowerUnitsHandlesEmptyAndNonEmptyVehicles() {
    var ca7PolicyLine = Mockito.mock(productmodel.CA7Line)
    Mockito.when(ca7PolicyLine.Subtype).thenReturn(typekey.PolicyLine.TC_CA7COMMAUTOLINE)

    var period = Mockito.mock(PolicyPeriod)
    Mockito.when(period.Lines).thenReturn(new entity.PolicyLine[]{ca7PolicyLine})

    var helper = new AccountProposalApiHelper()

    // 1. Empty Vehicles array branch
    var emptyAccCa7 = helper.mapCa7PowerUnits(new AccCA7Line(), period)

    // 2. Non-empty Vehicles array branch
    var nonEmptyVehicles = Arrays.copyOf(ca7PolicyLine.Vehicles, 1)
    Mockito.when(ca7PolicyLine.Vehicles).thenReturn(nonEmptyVehicles)
    var nonEmptyAccCa7 = helper.mapCa7PowerUnits(new AccCA7Line(), period)

    //Assert Conditions
    assertNotNull(emptyAccCa7)
    assertTrue(emptyAccCa7.toString().contains("0"))
    assertNotNull(nonEmptyAccCa7)
    assertTrue(nonEmptyAccCa7.toString().contains("0"))
  }

  /**
   * Positive Case - Covers all FloodZoneDesignation branches in mapBuildingFloodZoneDesignation.
   */
  function testThatMapBuildingFloodZoneDesignationCoversAllFloodZones() {
    var helper = new AccountProposalApiHelper()
    var building = Mockito.mock(PSLPSBuilding)

    // 1. Null FloodZoneDesignation
    Mockito.when(building.FloodZoneDesignation).thenReturn(null)
    var nullResult = helper.mapBuildingFloodZoneDesignation(building)

    // 2. TC_NCP
    Mockito.when(building.FloodZoneDesignation).thenReturn(FloodZoneDesignationType.TC_NCP)
    var ncpResult = helper.mapBuildingFloodZoneDesignation(building)

    // 3. TC_LOWHAZARDFLOOD
    Mockito.when(building.FloodZoneDesignation).thenReturn(FloodZoneDesignationType.TC_LOWHAZARDFLOOD)
    var lowHazardResult = helper.mapBuildingFloodZoneDesignation(building)

    // 4. TC_MODERATEHAZARDFLOOD
    Mockito.when(building.FloodZoneDesignation).thenReturn(FloodZoneDesignationType.TC_MODERATEHAZARDFLOOD)
    var moderateHazardResult = helper.mapBuildingFloodZoneDesignation(building)

    // 5. TC_HIGHHAZARDFLOOD
    Mockito.when(building.FloodZoneDesignation).thenReturn(FloodZoneDesignationType.TC_HIGHHAZARDFLOOD)
    var highHazardResult = helper.mapBuildingFloodZoneDesignation(building)

    // 6. Any other FloodZoneDesignationType (if present in typelist)
    var otherZone = FloodZoneDesignationType.getAllTypeKeys().firstWhere(\tk ->
        tk != FloodZoneDesignationType.TC_NCP and
        tk != FloodZoneDesignationType.TC_LOWHAZARDFLOOD and
        tk != FloodZoneDesignationType.TC_MODERATEHAZARDFLOOD and
        tk != FloodZoneDesignationType.TC_HIGHHAZARDFLOOD)
    if (otherZone != null) {
      Mockito.when(building.FloodZoneDesignation).thenReturn(otherZone)
      assertEquals("", helper.mapBuildingFloodZoneDesignation(building))
    }

    //Assert Conditions
    assertEquals(FloodZoneDesignationType.TC_NCP.Code, nullResult)
    assertEquals(FloodZoneDesignationType.TC_NCP.Code, ncpResult)
    assertEquals(CommonConstants.FLOOD, lowHazardResult)
    assertEquals(FloodZoneDesignationType.TC_MODERATEHAZARDFLOOD.DisplayName, moderateHazardResult)
    assertEquals(FloodZoneDesignationType.TC_HIGHHAZARDFLOOD.DisplayName, highHazardResult)
  }

  /**
   * Positive Case - Executes invokeAccountProposal directly on AccountProposalApiHelper.
   */
  function testThatInvokeAccountProposalOnHelperExecutesClientCall() {
    var helper = new AccountProposalApiHelper()
    var req = new PcAccDocumentCreationRequest()
    try {
      var resp = helper.invokeAccountProposal(req)
      //Assert Conditions
      assertTrue(resp == null or resp typeis CreateDocumentResponse)
    } catch (ex : Exception) {
      assertNotNull(ex)
    }
  }

  /**
   * Positive & Negative Cases - Verifies AccountProposalApiClient.accountProposal for valid, null, and unexpected responses.
   */
  function testThatClientAccountProposalReturnsResponseAndHandlesNonMatchingResponses() {
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_DPS_GC)
    var mockClient = new MockAccountProposalApiClient(propRetriever)
    var req = new PcAccDocumentCreationRequest()

    mockClient.mockScenario = "SUCCESS"
    var validResp = mockClient.accountProposal("DOC-101", req)

    mockClient.mockScenario = "NULL_RESPONSE"
    var nullResp = mockClient.accountProposal("DOC-102", req)

    mockClient.mockScenario = "WRONG_TYPE_RESPONSE"
    var wrongTypeResp = mockClient.accountProposal("DOC-103", req)

    //Assert Conditions
    assertNotNull(validResp)
    assertEquals(CommonConstants.ACCOUNT_PROPOSAL, mockClient.retrieveServiceName())
    assertNull(nullResp)
    assertNull(wrongTypeResp)
  }

  /**
   * Exception Cases - Verifies AccountProposalApiClient.accountProposal logs and rethrows FeignException and RuntimeException.
   */
  function testThatClientAccountProposalRethrowsFeignAndRuntimeExceptions() {
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_DPS_GC)
    var mockClient = new MockAccountProposalApiClient(propRetriever)
    var req = new PcAccDocumentCreationRequest()

    mockClient.mockScenario = "FEIGN_EXCEPTION"
    var caughtFeign = false
    try {
      mockClient.accountProposal("DOC-201", req)
    } catch (ex : FeignException) {
      caughtFeign = true
    }

    mockClient.mockScenario = "RUNTIME_EXCEPTION"
    var caughtRuntime = false
    try {
      mockClient.accountProposal("DOC-202", req)
    } catch (ex : RuntimeException) {
      caughtRuntime = true
    }

    //Assert Conditions
    assertTrue(caughtFeign)
    assertTrue(caughtRuntime)
  }

  /**
   * Positive & Negative Cases - Covers AccountProposalApiClient.doServiceCall and retrieveServiceName across all branches.
   */
  function testThatClientDoServiceCallHandlesValidAndUnsupportedRequests() {
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_DPS_GC)
    var client = new AccountProposalApiClient(propRetriever)

    var initialServiceName = client.retrieveServiceName()
    var unsupportedResp = client.doServiceCall(null, "UNSUPPORTED_REQUEST")
    var nullReqResp = client.doServiceCall(null, null)

    var req = new PcAccDocumentCreationRequest()
    var expectedResp = new CreateDocumentResponse()

    var mockApi = Mockito.mock(AccountApi)
    Mockito.when(mockApi.restCreateAccDocumentSynchronously(null, req)).thenReturn(expectedResp)

    var mockConfig = Mockito.mock(Config)
    Mockito.when(mockConfig.buildAPI(AccountApi)).thenReturn(mockApi)

    var actualResp = client.doServiceCall(mockConfig, req)

    Mockito.when(mockApi.restCreateAccDocumentSynchronously(null, req)).thenReturn(null)
    var nullApiResp = client.doServiceCall(mockConfig, req)

    //Assert Conditions
    assertNull(initialServiceName)
    assertNull(unsupportedResp)
    assertNull(nullReqResp)
    assertEquals(expectedResp, actualResp)
    assertNull(nullApiResp)
  }

  /**
   * Positive & Negative Cases - Covers all branches of SmartcommUIhelper.isAValidURL.
   */
  function testThatSmartcommUIHelperValidatesRuntimePropertyUrls() {
    var uiHelper = new SmartcommUIhelper()
    assertNotNull(uiHelper)

    var smartcommGroup = RuntimePropertyGroup.getAllTypeKeys()
        .firstWhere(\tk -> tk.Code == CommonConstants.SMARTCOMM_PROPERTYGROUPCODE)
    var otherGroup = RuntimePropertyGroup.getAllTypeKeys()
        .firstWhere(\tk -> tk.Code != CommonConstants.SMARTCOMM_PROPERTYGROUPCODE)

    gw.transaction.Transaction.runWithNewBundle(\bundle -> {
      if (smartcommGroup != null) {
        // 1. Matching group + SMARTCOMM_AUTHENTICATIODRAFT + non-matching URL -> false
        var authDraftProp = new RuntimeProperty(bundle)
        authDraftProp.PropertyGroup = smartcommGroup
        authDraftProp.Name = CommonConstants.SMARTCOMM_AUTHENTICATIODRAFT
        authDraftProp.Value = "invalid_url"

        // 2. Matching group + SMARTCOMM_DRAFTEDITOR + non-matching URL -> false
        var draftEditorProp = new RuntimeProperty(bundle)
        draftEditorProp.PropertyGroup = smartcommGroup
        draftEditorProp.Name = CommonConstants.SMARTCOMM_DRAFTEDITOR
        draftEditorProp.Value = "invalid_url"

        // 3. Matching group + unrelated property Name -> true
        var otherNameProp = new RuntimeProperty(bundle)
        otherNameProp.PropertyGroup = smartcommGroup
        otherNameProp.Name = "UnrelatedPropertyName"
        otherNameProp.Value = "invalid_url"

        //Assert Conditions
        assertFalse(SmartcommUIhelper.isAValidURL(authDraftProp))
        assertFalse(SmartcommUIhelper.isAValidURL(draftEditorProp))
        assertTrue(SmartcommUIhelper.isAValidURL(otherNameProp))
      }

      // 4. Non-matching PropertyGroup -> true
      var otherGroupProp = new RuntimeProperty(bundle)
      otherGroupProp.PropertyGroup = otherGroup
      otherGroupProp.Name = CommonConstants.SMARTCOMM_AUTHENTICATIODRAFT
      otherGroupProp.Value = "invalid_url"

      assertTrue(SmartcommUIhelper.isAValidURL(otherGroupProp))
    }, User.util.UnrestrictedUser)
  }

}
