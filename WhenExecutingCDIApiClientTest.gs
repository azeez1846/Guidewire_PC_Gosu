package xlp.integration.nameclearance

uses feign.FeignException
uses gw.account.AccountSearchCriteria
uses gw.api.builder.AccountBuilder
uses gw.api.builder.AccountContactBuilder
uses gw.api.builder.CompanyBuilder
uses gw.api.properties.RuntimePropertyRetriever
uses gw.restclient.config.Config
uses gw.testharness.v3.Suites
uses gwgen.cdi.api.CdiServiceApi
uses gwgen.cdi.model.AddPartyDataRequest
uses gwgen.cdi.model.AddPartyDataResponse
uses gwgen.cdi.model.NameClearanceRequest
uses gwgen.cdi.model.NameClearanceResponse
uses gwgen.cdi.model.PartyDataSearchRequest
uses gwgen.cdi.model.PartyDataSearchResponse
uses org.mockito.Mockito
uses xlp.common.PCServerTestClassBase_Ext
uses xlp.integration.common.CommonConstants
uses xlp.suites.IntegrationSuite

/**
 * To Test CDI Integration Service.
 */
@Export
@Suites(IntegrationSuite.NAME)
class WhenExecutingCDIApiClientTest extends PCServerTestClassBase_Ext {
  protected var mockCdiApiHelper : MockCdiApiHelper

  protected override function beforeMethod() { super.beforeMethod() }

  // ===================================================================
  // 1. End-to-end tests using MockCdiApiHelper (JSON mock responses)
  // ===================================================================

  /**
   * Positive Case - CDI Search Party Service successfully returns party data.
   */
  function testSearchPartyWithSuccess() {
    var accountSearchCriteria = new AccountSearchCriteria()
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    mockCdiApiHelper = new MockCdiApiHelper()
    var partySearchResponse = mockCdiApiHelper.partySearch(accountSearchCriteria)
    //Assert Conditions
    assertTrue(partySearchResponse.ProcessStatus == "Success")
  }

  /**
   * Negative Case - CDI Search Party Service did not find searched party data.
   */
  function testSearchPartyWithFailure() {
    var accountSearchCriteria = new AccountSearchCriteria()
    accountSearchCriteria.CompanyName = "TestUser123"
    accountSearchCriteria.Country = Country.TC_US
    mockCdiApiHelper = new MockCdiApiHelper()
    var partySearchResponse = mockCdiApiHelper.partySearch(accountSearchCriteria)
    //Assert Conditions
    assertTrue(partySearchResponse.ErrorMessage == "No results found")
  }

  /**
   * Positive Case - CDI Add Party Service successfully added new party data.
   */
  function testAddPartyWithSuccess() {
    var account : Account
    var contactBuilder = new CompanyBuilder()
        .withCompanyName("NewFord")

    var accountQueryBuilder = new AccountBuilder(false)
        .withAccountContact(new AccountContactBuilder().asAccountHolder().withContact(contactBuilder))
    gw.transaction.Transaction.runWithNewBundle(\bundle -> {
      account = accountQueryBuilder.create(bundle)
    }, User.util.UnrestrictedUser)
    mockCdiApiHelper = new MockCdiApiHelper()
    var addPartyResponse = mockCdiApiHelper.addParty(account)
    //Assert Conditions
    assertTrue(addPartyResponse.ProcessStatus == "Success")
  }

  /**
   * Negative Case - CDI Add Party Service could not add new party data.
   */
  function testAddPartyWithFailure() {
    var account : Account
    var contactBuilder = new CompanyBuilder()
        .withCompanyName("Ford")

    var accountQueryBuilder = new AccountBuilder(false)
        .withAccountContact(new AccountContactBuilder().asAccountHolder().withContact(contactBuilder))

    gw.transaction.Transaction.runWithNewBundle(\bundle -> {
      account = accountQueryBuilder.create(bundle)
    }, User.util.UnrestrictedUser)
    mockCdiApiHelper = new MockCdiApiHelper()
    var addPartyResponse = mockCdiApiHelper.addParty(account)
    //Assert Conditions
    assertTrue(addPartyResponse.ProcessStatus == "Failed")
  }

  /**
   * Positive Case - CDI Name Clearance Service found existing contracts for searched party.
   */
  function testNameClearanceWithSuccess() {
    var xlNameId = "X52058205"
    mockCdiApiHelper = new MockCdiApiHelper()
    var nameClearanceResponse = mockCdiApiHelper.nameClearance(xlNameId)
    //Assert Conditions
    assertTrue(nameClearanceResponse.ProcessStatus == "Success")
  }

  /**
   * Negative Case - CDI Name Clearance Service did not find existing contracts for searched party.
   */
  function testNameClearanceWithFailure() {
    var xlNameId = "X1234567"
    mockCdiApiHelper = new MockCdiApiHelper()
    var nameClearanceResponse = mockCdiApiHelper.nameClearance(xlNameId)
    //Assert Conditions
    assertTrue(nameClearanceResponse.ProcessStatus == "Failure")
  }

  // ===================================================================
  // 2. CdiApiHelper - request mapping
  // ===================================================================

  function testMapPartySearchRequest() {
    var criteria = new AccountSearchCriteria()
    criteria.CompanyName = "Ford"
    criteria.Country = Country.TC_US

    var request = new CdiApiHelper().mapPartySearchRequest(criteria)

    assertNotNull(request.GlobalTrackingId)
    assertNotNull(request.RequestTimeStamp)
    assertEquals(CommonConstants.SOURCE_SYSTEM, request.SourceSystem)
    assertEquals("Ford", request.OrganizationName)
    assertEquals(CommonConstants.PARTY_ROLE, request.PartyRole)
    assertEquals(Country.TC_US as String, request.Country)
  }

  function testMapPartySearchRequestWithEmptyCriteria() {
    var request = new CdiApiHelper().mapPartySearchRequest(new AccountSearchCriteria())

    assertNotNull(request)
    assertNull(request.OrganizationName)
  }

  function testMapNameClearanceRequest() {
    var request = new CdiApiHelper().mapNameClearanceRequest("X52058205")

    assertNotNull(request.GlobalTrackingId)
    assertNotNull(request.RequestTimeStamp)
    assertEquals(CommonConstants.SOURCE_SYSTEM, request.SourceSystem)
    assertEquals("X52058205", request.XlNameId)
    assertEquals(CommonConstants.SEARCH_MODE, request.SearchMode)
    assertEquals(CommonConstants.CONTRACTOR_INDICATOR, request.ContractIndicator)
    assertEquals(CommonConstants.OCCURANCES, request.NoOfOccurances)
    assertTrue(request.InceptionDt.endsWith("T00:00:00"))
  }

  function testMapAddPartyRequestForNonProjectAccount() {
    var account : Account
    gw.transaction.Transaction.runWithNewBundle(\bundle -> {
      account = new AccountBuilder(false)
          .withAccountContact(new AccountContactBuilder().asAccountHolder().withContact(new CompanyBuilder().withCompanyName("NewFord")))
          .create(bundle)
    }, User.util.UnrestrictedUser)

    var request = new CdiApiHelper().mapAddPartyRequest(account)

    assertNotNull(request.GlobalTrackingId)
    assertEquals(CommonConstants.SOURCE_SYSTEM, request.SourceSystem)
    assertEquals(CommonConstants.CHECK_FOR_DULPICATE, request.CheckForDuplicate)
    assertEquals(CommonConstants.PARTY_ROLE, request.PartyRole)
    assertEquals(CommonConstants.PARTY_TYPE, request.PartyType)
    assertEquals(CommonConstants.ORGANIZATION_TYPE, request.OrganizationType)

    assertEquals(1, request.Name.size())
    assertEquals(CommonConstants.ADDRESSORGANIZATION_TYPE, request.Name.first().OrganizationNameType)
    assertEquals("NewFord", request.Name.first().OrganizationName)

    assertEquals(1, request.Address.size())
    assertEquals(CommonConstants.ADDRESSORGANIZATION_TYPE, request.Address.first().AddressType)
    assertEquals(account.AccountHolderContact.PrimaryAddress.AddressLine1, request.Address.first().AddressLine1)
    assertEquals(account.AccountHolderContact.PrimaryAddress.City, request.Address.first().City)
    assertEquals(account.AccountHolderContact.PrimaryAddress.PostalCode, request.Address.first().Zipcode)

    assertEquals(1, request.ContactMethods.size())
    assertEquals(CommonConstants.CONTACTMETHOD, request.ContactMethods.first().ContactMethodType)
  }

  function testMapAddPartyRequestForProjectAccount() {
    var account : Account
    gw.transaction.Transaction.runWithNewBundle(\bundle -> {
      account = new AccountBuilder(false)
          .withAccountContact(new AccountContactBuilder().asAccountHolder().withContact(new CompanyBuilder().withCompanyName("NewFord")))
          .create(bundle)
      account.AccountOrgType = AccountOrgType.TC_PROJECT_EXT
    }, User.util.UnrestrictedUser)

    var request = new CdiApiHelper().mapAddPartyRequest(account)

    assertEquals(CommonConstants.PARTY_TYPE_PROJECT, request.PartyType)
    assertEquals(CommonConstants.ORGANIZATION_TYPE_PROJECT, request.OrganizationType)
  }

  // ===================================================================
  // 3. CdiApiHelper - error handling (catch blocks)
  // ===================================================================

  function testPartySearchReturnsNullOnFeignException() {
    var feignException = Mockito.mock(FeignException)
    Mockito.when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0])
    var criteria = new AccountSearchCriteria()
    criteria.CompanyName = "Ford"

    var response = new FailingCdiApiHelper(feignException).partySearch(criteria)

    assertNull(response)
  }

  function testPartySearchReturnsNullOnRuntimeException() {
    var criteria = new AccountSearchCriteria()
    criteria.CompanyName = "Ford"

    var response = new FailingCdiApiHelper(new RuntimeException("Service down")).partySearch(criteria)

    assertNull(response)
  }

  function testAddPartyReturnsNullOnFeignException() {
    var feignException = Mockito.mock(FeignException)
    Mockito.when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0])
    var account : Account
    gw.transaction.Transaction.runWithNewBundle(\bundle -> {
      account = new AccountBuilder(false)
          .withAccountContact(new AccountContactBuilder().asAccountHolder().withContact(new CompanyBuilder().withCompanyName("NewFord")))
          .create(bundle)
    }, User.util.UnrestrictedUser)

    var response = new FailingCdiApiHelper(feignException).addParty(account)

    assertNull(response)
  }

  function testAddPartyReturnsNullOnRuntimeException() {
    var account : Account
    gw.transaction.Transaction.runWithNewBundle(\bundle -> {
      account = new AccountBuilder(false)
          .withAccountContact(new AccountContactBuilder().asAccountHolder().withContact(new CompanyBuilder().withCompanyName("NewFord")))
          .create(bundle)
    }, User.util.UnrestrictedUser)

    var response = new FailingCdiApiHelper(new RuntimeException("Service down")).addParty(account)

    assertNull(response)
  }

  function testNameClearanceReturnsNullOnFeignException() {
    var feignException = Mockito.mock(FeignException)
    Mockito.when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0])

    var response = new FailingCdiApiHelper(feignException).nameClearance("X52058205")

    assertNull(response)
  }

  function testNameClearanceReturnsNullOnRuntimeException() {
    var response = new FailingCdiApiHelper(new RuntimeException("Service down")).nameClearance("X52058205")

    assertNull(response)
  }

  // ===================================================================
  // 4. CdiApiClient - searchPartyData
  // ===================================================================

  function testSearchPartyDataReturnsResponse() {
    var expected = new PartyDataSearchResponse() { :ProcessStatus = "Success" }
    var client = new StubCdiApiClient(expected, null)

    var actual = client.searchPartyData(new PartyDataSearchRequest())

    assertEquals(expected, actual)
    assertEquals(CommonConstants.CDI_SEARCH_PARTY, client.retrieveServiceName())
  }

  function testSearchPartyDataReturnsNullForUnexpectedResponse() {
    assertNull(new StubCdiApiClient(null, null).searchPartyData(new PartyDataSearchRequest()))
    assertNull(new StubCdiApiClient("WRONG_TYPE", null).searchPartyData(new PartyDataSearchRequest()))
  }

  function testSearchPartyDataRethrowsFeignException() {
    var feignException = Mockito.mock(FeignException)
    Mockito.when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0])
    var client = new StubCdiApiClient(null, feignException)

    var exceptionThrown = false
    try {
      client.searchPartyData(new PartyDataSearchRequest())
    } catch (e : FeignException) {
      exceptionThrown = true
    }
    assertTrue(exceptionThrown)
  }

  function testSearchPartyDataRethrowsRuntimeException() {
    var client = new StubCdiApiClient(null, new RuntimeException("Service down"))

    var exceptionThrown = false
    try {
      client.searchPartyData(new PartyDataSearchRequest())
    } catch (e : RuntimeException) {
      exceptionThrown = true
    }
    assertTrue(exceptionThrown)
  }

  // ===================================================================
  // 5. CdiApiClient - addPartyData
  // ===================================================================

  function testAddPartyDataReturnsResponse() {
    var expected = new AddPartyDataResponse() { :ProcessStatus = "Success" }
    var client = new StubCdiApiClient(expected, null)

    var actual = client.addPartyData(new AddPartyDataRequest())

    assertEquals(expected, actual)
    assertEquals(CommonConstants.CDI_ADD_PARTY, client.retrieveServiceName())
  }

  function testAddPartyDataReturnsNullForUnexpectedResponse() {
    assertNull(new StubCdiApiClient(null, null).addPartyData(new AddPartyDataRequest()))
    assertNull(new StubCdiApiClient("WRONG_TYPE", null).addPartyData(new AddPartyDataRequest()))
  }

  function testAddPartyDataRethrowsFeignException() {
    var feignException = Mockito.mock(FeignException)
    Mockito.when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0])
    var client = new StubCdiApiClient(null, feignException)

    var exceptionThrown = false
    try {
      client.addPartyData(new AddPartyDataRequest())
    } catch (e : FeignException) {
      exceptionThrown = true
    }
    assertTrue(exceptionThrown)
  }

  function testAddPartyDataRethrowsRuntimeException() {
    var client = new StubCdiApiClient(null, new RuntimeException("Service down"))

    var exceptionThrown = false
    try {
      client.addPartyData(new AddPartyDataRequest())
    } catch (e : RuntimeException) {
      exceptionThrown = true
    }
    assertTrue(exceptionThrown)
  }

  // ===================================================================
  // 6. CdiApiClient - doNameClearance
  // ===================================================================

  function testDoNameClearanceReturnsResponse() {
    var expected = new NameClearanceResponse() { :ProcessStatus = "Success" }
    var client = new StubCdiApiClient(expected, null)

    var actual = client.doNameClearance(new NameClearanceRequest())

    assertEquals(expected, actual)
    assertEquals(CommonConstants.CDI_NAME_CLEARANCE, client.retrieveServiceName())
  }

  function testDoNameClearanceReturnsNullForUnexpectedResponse() {
    assertNull(new StubCdiApiClient(null, null).doNameClearance(new NameClearanceRequest()))
    assertNull(new StubCdiApiClient("WRONG_TYPE", null).doNameClearance(new NameClearanceRequest()))
  }

  function testDoNameClearanceRethrowsFeignException() {
    var feignException = Mockito.mock(FeignException)
    Mockito.when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0])
    var client = new StubCdiApiClient(null, feignException)

    var exceptionThrown = false
    try {
      client.doNameClearance(new NameClearanceRequest())
    } catch (e : FeignException) {
      exceptionThrown = true
    }
    assertTrue(exceptionThrown)
  }

  function testDoNameClearanceRethrowsRuntimeException() {
    var client = new StubCdiApiClient(null, new RuntimeException("Service down"))

    var exceptionThrown = false
    try {
      client.doNameClearance(new NameClearanceRequest())
    } catch (e : RuntimeException) {
      exceptionThrown = true
    }
    assertTrue(exceptionThrown)
  }

  // ===================================================================
  // 7. CdiApiClient - doServiceCall and retrieveServiceName
  // ===================================================================

  function testRetrieveServiceNameIsNullBeforeAnyCall() {
    var client = new CdiApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NAMECLEARANCE_EXT))

    assertNull(client.retrieveServiceName())
  }

  function testDoServiceCallReturnsNullForUnsupportedRequest() {
    var client = new CdiApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NAMECLEARANCE_EXT))

    assertNull(client.doServiceCall(null, "UNSUPPORTED_REQUEST"))
    assertNull(client.doServiceCall(null, null))
  }

  function testDoServiceCallForAllRequestTypes() {
    var client = new CdiApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NAMECLEARANCE_EXT))

    var searchRequest = new PartyDataSearchRequest()
    var searchResponse = new PartyDataSearchResponse()
    var addRequest = new AddPartyDataRequest()
    var addResponse = new AddPartyDataResponse()
    var clearanceRequest = new NameClearanceRequest()
    var clearanceResponse = new NameClearanceResponse()

    var mockApi = Mockito.mock(CdiServiceApi)
    Mockito.when(mockApi.igPartyDataSearchAPI(searchRequest)).thenReturn(searchResponse)
    Mockito.when(mockApi.igAddPartyDataAPI(addRequest)).thenReturn(addResponse)
    Mockito.when(mockApi.igNameClearanceAPI(clearanceRequest)).thenReturn(clearanceResponse)

    var mockConfig = Mockito.mock(Config)
    Mockito.when(mockConfig.buildAPI(CdiServiceApi)).thenReturn(mockApi)

    assertEquals(searchResponse, client.doServiceCall(mockConfig, searchRequest))
    assertEquals(addResponse, client.doServiceCall(mockConfig, addRequest))
    assertEquals(clearanceResponse, client.doServiceCall(mockConfig, clearanceRequest))
  }

  // ===================================================================
  // Test doubles (small nested classes, no logic other than return/throw)
  // ===================================================================

  /**
   * CdiApiClient whose HTTP calls (doGet / doPost) return a fixed response or throw a fixed exception.
   */
  static class StubCdiApiClient extends CdiApiClient {
    var _response : Object
    var _exception : Exception

    construct(response : Object, exception : Exception) {
      super(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NAMECLEARANCE_EXT))
      _response = response
      _exception = exception
    }

    override function doGet(request : Object) : Object {
      if (_exception != null) {
        throw _exception
      }
      return _response
    }

    override function doPost(request : Object) : Object {
      if (_exception != null) {
        throw _exception
      }
      return _response
    }
  }

  /**
   * CdiApiHelper whose service calls always throw the given exception.
   */
  static class FailingCdiApiHelper extends CdiApiHelper {
    var _exception : Exception

    construct(exception : Exception) {
      _exception = exception
    }

    override function invokePartySearch(request : PartyDataSearchRequest) : PartyDataSearchResponse {
      throw _exception
    }

    override function invokeAddParty(request : AddPartyDataRequest) : AddPartyDataResponse {
      throw _exception
    }

    override function invokeNameClearance(request : NameClearanceRequest) : NameClearanceResponse {
      throw _exception
    }
  }

}
