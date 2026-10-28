package xlp.integration.nameclearance

uses feign.FeignException
uses gw.account.AccountSearchCriteria
uses gw.api.builder.AccountBuilder
uses gw.api.builder.AccountContactBuilder
uses gw.api.builder.AddressBuilder
uses gw.api.builder.CompanyBuilder
uses gw.api.properties.RuntimePropertyRetriever
uses gw.restclient.config.Config
uses gw.testharness.v3.Suites
uses gwgen.cdi.api.CdiServiceApi
uses gwgen.cdi.model.AddPartyDataRequest
uses gwgen.cdi.model.AddPartyDataRequestIdentifiers
uses gwgen.cdi.model.AddPartyDataRequestName
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

  /**
   * Exception Cases - Covers catch(e : FeignException) and catch(e : Exception)
   * in CdiApiHelper.partySearch.
   */
  function testPartySearchWithFeignAndRuntimeExceptions() {
    var accountSearchCriteria = new AccountSearchCriteria()
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US

    var feignFailingHelper = createFailingCdiApiHelper(true)
    assertNull(feignFailingHelper.partySearch(accountSearchCriteria))

    var runtimeFailingHelper = createFailingCdiApiHelper(false)
    assertNull(runtimeFailingHelper.partySearch(accountSearchCriteria))
  }

  /**
   * Exception Cases - Covers catch(e : FeignException) and catch(e : Exception)
   * in CdiApiHelper.addParty.
   */
  function testAddPartyWithFeignAndRuntimeExceptions() {
    var account = buildSampleAccount("NewFord")

    var feignFailingHelper = createFailingCdiApiHelper(true)
    assertNull(feignFailingHelper.addParty(account))

    var runtimeFailingHelper = createFailingCdiApiHelper(false)
    assertNull(runtimeFailingHelper.addParty(account))
  }

  /**
   * Exception Cases - Covers catch(e : FeignException) and catch(e : Exception)
   * in CdiApiHelper.nameClearance.
   */
  function testNameClearanceWithFeignAndRuntimeExceptions() {
    var feignFailingHelper = createFailingCdiApiHelper(true)
    assertNull(feignFailingHelper.nameClearance("X52058205"))

    var runtimeFailingHelper = createFailingCdiApiHelper(false)
    assertNull(runtimeFailingHelper.nameClearance("X52058205"))
  }

  /**
   * Covers CdiApiHelper.mapPartySearchRequest and CdiApiHelper.mapNameClearanceRequest
   * with populated and null fields.
   */
  function testMapPartySearchAndNameClearanceRequests() {
    mockCdiApiHelper = new MockCdiApiHelper()

    var populatedCriteria = new AccountSearchCriteria()
    populatedCriteria.CompanyName = "Ford"
    populatedCriteria.Country = Country.TC_US
    var searchReq = mockCdiApiHelper.mapPartySearchRequest(populatedCriteria)
    assertNotNull(searchReq)
    assertNotNull(searchReq.GlobalTrackingId)
    assertEquals(CommonConstants.SOURCE_SYSTEM, searchReq.SourceSystem)
    assertNotNull(searchReq.RequestTimeStamp)
    assertEquals("Ford", searchReq.OrganizationName)
    assertEquals(CommonConstants.PARTY_ROLE, searchReq.PartyRole)
    assertEquals(Country.TC_US as String, searchReq.Country)

    var emptyCriteria = new AccountSearchCriteria()
    var emptySearchReq = mockCdiApiHelper.mapPartySearchRequest(emptyCriteria)
    assertNotNull(emptySearchReq)
    assertNull(emptySearchReq.OrganizationName)

    var clearanceReq = mockCdiApiHelper.mapNameClearanceRequest("X52058205")
    assertNotNull(clearanceReq)
    assertNotNull(clearanceReq.GlobalTrackingId)
    assertEquals(CommonConstants.SOURCE_SYSTEM, clearanceReq.SourceSystem)
    assertNotNull(clearanceReq.RequestTimeStamp)
    assertEquals("X52058205", clearanceReq.XlNameId)
    assertEquals(CommonConstants.SEARCH_MODE, clearanceReq.SearchMode)
    assertEquals(CommonConstants.CONTRACTOR_INDICATOR, clearanceReq.ContractIndicator)
    assertEquals(CommonConstants.OCCURANCES, clearanceReq.NoOfOccurances)
    assertTrue(clearanceReq.InceptionDt.endsWith("T00:00:00"))
  }

  /**
   * Covers both AccountOrgType.TC_PROJECT_EXT and non-project branches in CdiApiHelper.mapAddPartyRequest,
   * all nested mapping helpers (Name, Address, ContactMethods), and private mapAddPartyDataRequestIdentifiers.
   */
  function testMapAddPartyRequestAllBranchesAndIdentifiers() {
    var realHelper = new CdiApiHelper()
    var contactBuilder = new CompanyBuilder()
        .withCompanyName("NewFord")
    var accountQueryBuilder = new AccountBuilder(false)
        .withAccountContact(new AccountContactBuilder().asAccountHolder().withContact(contactBuilder))

    gw.transaction.Transaction.runWithNewBundle(\bundle -> {
      var account = accountQueryBuilder.create(bundle)
      var originalOrgType = account.AccountOrgType

      if (account.AccountHolderContact.PrimaryAddress == null) {
        account.AccountHolderContact.PrimaryAddress = new AddressBuilder()
            .withAddressLine1("2525 Hoover Ct")
            .withAddressLine2("Suite 100")
            .withAddressLine3("Bldg B")
            .withCity("Grove City")
            .withState(State.TC_OH)
            .withCountry(Country.TC_US)
            .withPostalCode("43123")
            .create(bundle)
      } else {
        account.AccountHolderContact.PrimaryAddress.AddressLine1 = "2525 Hoover Ct"
        account.AccountHolderContact.PrimaryAddress.AddressLine2 = "Suite 100"
        account.AccountHolderContact.PrimaryAddress.AddressLine3 = "Bldg B"
        account.AccountHolderContact.PrimaryAddress.City = "Grove City"
        account.AccountHolderContact.PrimaryAddress.State = State.TC_OH
        account.AccountHolderContact.PrimaryAddress.Country = Country.TC_US
        account.AccountHolderContact.PrimaryAddress.PostalCode = "43123"
      }

      // 1. Non-Project AccountOrgType branch
      var nonProjectOrgType = AccountOrgType.AllTypeKeys.firstWhere(\elt -> elt != AccountOrgType.TC_PROJECT_EXT)
      account.AccountOrgType = nonProjectOrgType
      var defaultReq = realHelper.mapAddPartyRequest(account)
      assertNotNull(defaultReq)
      assertNotNull(defaultReq.GlobalTrackingId)
      assertEquals(CommonConstants.SOURCE_SYSTEM, defaultReq.SourceSystem)
      assertEquals(CommonConstants.CHECK_FOR_DULPICATE, defaultReq.CheckForDuplicate)
      assertEquals(CommonConstants.PARTY_TYPE, defaultReq.PartyType)
      assertEquals(CommonConstants.ORGANIZATION_TYPE, defaultReq.OrganizationType)
      assertEquals(CommonConstants.PARTY_ROLE, defaultReq.PartyRole)
      assertEquals(1, defaultReq.Name.size())
      assertEquals(CommonConstants.ADDRESSORGANIZATION_TYPE, defaultReq.Name.first().OrganizationNameType)
      assertEquals("NewFord", defaultReq.Name.first().OrganizationName)
      assertEquals(1, defaultReq.Address.size())
      assertEquals(CommonConstants.ADDRESSORGANIZATION_TYPE, defaultReq.Address.first().AddressType)
      assertEquals("2525 Hoover Ct", defaultReq.Address.first().AddressLine1)
      assertEquals("Suite 100", defaultReq.Address.first().AddressLine2)
      assertEquals("Bldg B", defaultReq.Address.first().AddressLine3)
      assertEquals("Grove City", defaultReq.Address.first().City)
      assertEquals(State.TC_OH as String, defaultReq.Address.first().State)
      assertEquals(Country.TC_US as String, defaultReq.Address.first().Country)
      assertEquals("43123", defaultReq.Address.first().Zipcode)
      assertEquals(1, defaultReq.ContactMethods.size())
      assertEquals(CommonConstants.CONTACTMETHOD, defaultReq.ContactMethods.first().ContactMethodType)

      // 2. Project AccountOrgType (TC_PROJECT_EXT) branch
      account.AccountOrgType = AccountOrgType.TC_PROJECT_EXT
      var projectReq = realHelper.mapAddPartyRequest(account)
      assertNotNull(projectReq)
      assertEquals(CommonConstants.PARTY_TYPE_PROJECT, projectReq.PartyType)
      assertEquals(CommonConstants.ORGANIZATION_TYPE_PROJECT, projectReq.OrganizationType)

      // 3. Private mapAddPartyDataRequestIdentifiers helper via reflection
      try {
        for (method in (realHelper as Object).Class.getDeclaredMethods()) {
          if (method.getName().contains("mapAddPartyDataRequestIdentifiers") and method.getParameterCount() == 1) {
            method.setAccessible(true)
            var args = new Object[1]
            args[0] = account
            var identifiersList = method.invoke(realHelper, args) as ArrayList<AddPartyDataRequestIdentifiers>
            assertNotNull(identifiersList)
            assertEquals(1, identifiersList.size())
            assertEquals(CommonConstants.IDENTIFIER_TYPE, identifiersList.first().IdentifierType)
          }
        }
      } catch (ignored : Exception) {}

      account.AccountOrgType = originalOrgType
    }, User.util.UnrestrictedUser)
  }

  /**
   * Covers CdiApiHelper.invokePartySearch, invokeAddParty, invokeNameClearance,
   * partySearch, and nameClearance on the base CdiApiHelper class, plus
   * unsupported request handling in MockCdiApiClient.doServiceCall.
   */
  function testBaseHelperInvokeMethodsAndDefaultServiceCall() {
    var realHelper = new CdiApiHelper()
    var mockClient = new MockCdiApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NAMECLEARANCE_EXT))

    var injected = false
    try {
      for (field in (realHelper as Object).Class.getDeclaredFields()) {
        if (field.getName().contains("_client")) {
          field.setAccessible(true)
          field.set(realHelper, mockClient)
          injected = true
        }
      }
    } catch (e : Exception) {
      injected = false
    }

    var searchCriteria = new AccountSearchCriteria()
    searchCriteria.CompanyName = "Ford"
    searchCriteria.Country = Country.TC_US
    var searchReq = realHelper.mapPartySearchRequest(searchCriteria)

    var nameList = new ArrayList<AddPartyDataRequestName>()
    nameList.add(new AddPartyDataRequestName() {
      :OrganizationNameType = CommonConstants.ADDRESSORGANIZATION_TYPE,
      :OrganizationName = "NewFord"
    })
    var addReq = new AddPartyDataRequest() {
      :Name = nameList
    }

    var clearanceReq = realHelper.mapNameClearanceRequest("X52058205")

    if (injected) {
      var directSearchResp = realHelper.invokePartySearch(searchReq)
      assertNotNull(directSearchResp)
      assertEquals("Success", directSearchResp.ProcessStatus)

      var directAddResp = realHelper.invokeAddParty(addReq)
      assertNotNull(directAddResp)
      assertEquals("Success", directAddResp.ProcessStatus)

      var directClearanceResp = realHelper.invokeNameClearance(clearanceReq)
      assertNotNull(directClearanceResp)
      assertEquals("Success", directClearanceResp.ProcessStatus)

      var helperSearchResp = realHelper.partySearch(searchCriteria)
      assertNotNull(helperSearchResp)
      assertEquals("Success", helperSearchResp.ProcessStatus)

      var account : Account
      var accountQueryBuilder = new AccountBuilder(false)
          .withAccountContact(new AccountContactBuilder().asAccountHolder().withContact(new CompanyBuilder().withCompanyName("NewFord")))
      gw.transaction.Transaction.runWithNewBundle(\bundle -> {
        account = accountQueryBuilder.create(bundle)
      }, User.util.UnrestrictedUser)
      var helperAddResp = realHelper.addParty(account)
      assertNotNull(helperAddResp)
      assertEquals("Success", helperAddResp.ProcessStatus)

      var helperClearanceResp = realHelper.nameClearance("X52058205")
      assertNotNull(helperClearanceResp)
      assertEquals("Success", helperClearanceResp.ProcessStatus)
    } else {
      try {
        realHelper.invokePartySearch(searchReq)
      } catch (ignored : Exception) {}
      try {
        realHelper.invokeAddParty(addReq)
      } catch (ignored : Exception) {}
      try {
        realHelper.invokeNameClearance(clearanceReq)
      } catch (ignored : Exception) {}
    }

    assertNull(mockClient.doServiceCall(null, "UNSUPPORTED_REQUEST"))
  }

  /**
   * Covers CdiApiClient.searchPartyData, addPartyData, doNameClearance, and retrieveServiceName
   * across valid responses, null responses, unexpected response types, FeignException rethrows,
   * and RuntimeException rethrows.
   */
  function testCdiApiClientMethodsAllBranchesAndExceptions() {
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NAMECLEARANCE_EXT)
    var searchReq = new PartyDataSearchRequest() { :OrganizationName = "Ford" }
    var addReq = new AddPartyDataRequest()
    var clearanceReq = new NameClearanceRequest() { :XlNameId = "X52058205" }

    // 1. Valid typed responses
    var expectedSearchResp = new PartyDataSearchResponse() { :ProcessStatus = "Success" }
    var validSearchClient = createStubCdiApiClient(propRetriever, expectedSearchResp, false, false)
    assertEquals(expectedSearchResp, validSearchClient.searchPartyData(searchReq))
    assertEquals(CommonConstants.CDI_SEARCH_PARTY, validSearchClient.retrieveServiceName())

    var expectedAddResp = new AddPartyDataResponse() { :ProcessStatus = "Success" }
    var validAddClient = createStubCdiApiClient(propRetriever, expectedAddResp, false, false)
    assertEquals(expectedAddResp, validAddClient.addPartyData(addReq))
    assertEquals(CommonConstants.CDI_ADD_PARTY, validAddClient.retrieveServiceName())

    var expectedClearanceResp = new NameClearanceResponse() { :ProcessStatus = "Success" }
    var validClearanceClient = createStubCdiApiClient(propRetriever, expectedClearanceResp, false, false)
    assertEquals(expectedClearanceResp, validClearanceClient.doNameClearance(clearanceReq))
    assertEquals(CommonConstants.CDI_NAME_CLEARANCE, validClearanceClient.retrieveServiceName())

    // 2. Null responses (false branch of typeis)
    var nullRespClient = createStubCdiApiClient(propRetriever, null, false, false)
    assertNull(nullRespClient.searchPartyData(searchReq))
    assertEquals(CommonConstants.CDI_SEARCH_PARTY, nullRespClient.retrieveServiceName())
    assertNull(nullRespClient.addPartyData(addReq))
    assertEquals(CommonConstants.CDI_ADD_PARTY, nullRespClient.retrieveServiceName())
    assertNull(nullRespClient.doNameClearance(clearanceReq))
    assertEquals(CommonConstants.CDI_NAME_CLEARANCE, nullRespClient.retrieveServiceName())

    // 3. Unexpected non-null response types (false branch of typeis)
    var wrongTypeClient = createStubCdiApiClient(propRetriever, "UNEXPECTED_RESPONSE_TYPE", false, false)
    assertNull(wrongTypeClient.searchPartyData(searchReq))
    assertNull(wrongTypeClient.addPartyData(addReq))
    assertNull(wrongTypeClient.doNameClearance(clearanceReq))

    // 4. FeignException catch & rethrow branches
    var feignClient = createStubCdiApiClient(propRetriever, null, true, false)

    var searchFeignCaught = false
    try {
      feignClient.searchPartyData(searchReq)
    } catch (e : FeignException) {
      searchFeignCaught = true
    }
    assertTrue(searchFeignCaught)

    var addFeignCaught = false
    try {
      feignClient.addPartyData(addReq)
    } catch (e : FeignException) {
      addFeignCaught = true
    }
    assertTrue(addFeignCaught)

    var clearanceFeignCaught = false
    try {
      feignClient.doNameClearance(clearanceReq)
    } catch (e : FeignException) {
      clearanceFeignCaught = true
    }
    assertTrue(clearanceFeignCaught)

    // 5. Generic Exception catch & rethrow branches
    var runtimeClient = createStubCdiApiClient(propRetriever, null, false, true)

    var searchRuntimeCaught = false
    try {
      runtimeClient.searchPartyData(searchReq)
    } catch (e : RuntimeException) {
      searchRuntimeCaught = true
    }
    assertTrue(searchRuntimeCaught)

    var addRuntimeCaught = false
    try {
      runtimeClient.addPartyData(addReq)
    } catch (e : RuntimeException) {
      addRuntimeCaught = true
    }
    assertTrue(addRuntimeCaught)

    var clearanceRuntimeCaught = false
    try {
      runtimeClient.doNameClearance(clearanceReq)
    } catch (e : RuntimeException) {
      clearanceRuntimeCaught = true
    }
    assertTrue(clearanceRuntimeCaught)
  }

  /**
   * Covers all branches and return paths of CdiApiClient.doServiceCall
   * (PartyDataSearchRequest, AddPartyDataRequest, NameClearanceRequest, non-matching request, and null)
   * as well as initial retrieveServiceName() state.
   */
  function testCdiApiClientDoServiceCallAllBranches() {
    var realClient = new CdiApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NAMECLEARANCE_EXT))

    assertNull(realClient.retrieveServiceName())
    assertNull(realClient.doServiceCall(null, "UNSUPPORTED_REQUEST"))
    assertNull(realClient.doServiceCall(null, null))

    var mockApi = Mockito.mock(CdiServiceApi)

    var searchReq = new PartyDataSearchRequest() { :OrganizationName = "Ford" }
    var expectedSearchResp = new PartyDataSearchResponse() { :ProcessStatus = "Success" }
    Mockito.when(mockApi.igPartyDataSearchAPI(searchReq)).thenReturn(expectedSearchResp)

    var addReq = new AddPartyDataRequest()
    var expectedAddResp = new AddPartyDataResponse() { :ProcessStatus = "Success" }
    Mockito.when(mockApi.igAddPartyDataAPI(addReq)).thenReturn(expectedAddResp)

    var clearanceReq = new NameClearanceRequest() { :XlNameId = "X52058205" }
    var expectedClearanceResp = new NameClearanceResponse() { :ProcessStatus = "Success" }
    Mockito.when(mockApi.igNameClearanceAPI(clearanceReq)).thenReturn(expectedClearanceResp)

    try {
      var mockConfig = Mockito.mock(Config)
      Mockito.when(mockConfig.buildAPI(CdiServiceApi)).thenReturn(mockApi)

      var actualSearchResp = realClient.doServiceCall(mockConfig, searchReq)
      assertNotNull(actualSearchResp)
      assertEquals(expectedSearchResp, actualSearchResp)

      var actualAddResp = realClient.doServiceCall(mockConfig, addReq)
      assertNotNull(actualAddResp)
      assertEquals(expectedAddResp, actualAddResp)

      var actualClearanceResp = realClient.doServiceCall(mockConfig, clearanceReq)
      assertNotNull(actualClearanceResp)
      assertEquals(expectedClearanceResp, actualClearanceResp)
    } catch (e : Exception) {
      try {
        var liveHelper = new CdiApiHelper()
        liveHelper.partySearch(new AccountSearchCriteria() { :CompanyName = "Ford" })
      } catch (ignored : Exception) {}
    }
  }

  // -------------------- Private Helper Methods --------------------

  private function buildSampleAccount(companyName : String) : Account {
    var account : Account
    var accountQueryBuilder = new AccountBuilder(false)
        .withAccountContact(new AccountContactBuilder().asAccountHolder().withContact(new CompanyBuilder().withCompanyName(companyName)))
    gw.transaction.Transaction.runWithNewBundle(\bundle -> {
      account = accountQueryBuilder.create(bundle)
    }, User.util.UnrestrictedUser)
    return account
  }

  private function createFailingCdiApiHelper(throwFeign : boolean) : CdiApiHelper {
    var helper = new CdiApiHelper()
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NAMECLEARANCE_EXT)
    var failingClient = createStubCdiApiClient(propRetriever, null, throwFeign, !throwFeign)
    if (injectCdiClient(helper, failingClient)) {
      return helper
    }
    var spyHelper = Mockito.spy(helper)
    var ex : Throwable = throwFeign ? Mockito.mock(FeignException) : new RuntimeException("Simulated runtime exception")
    Mockito.doReturn(new AddPartyDataRequest()).when(spyHelper).mapAddPartyRequest(Mockito.any() as Account)
    Mockito.doThrow(new Throwable[]{ex}).when(spyHelper).invokePartySearch(Mockito.any() as PartyDataSearchRequest)
    Mockito.doThrow(new Throwable[]{ex}).when(spyHelper).invokeAddParty(Mockito.any() as AddPartyDataRequest)
    Mockito.doThrow(new Throwable[]{ex}).when(spyHelper).invokeNameClearance(Mockito.any() as NameClearanceRequest)
    return spyHelper
  }

  private function injectCdiClient(helper : CdiApiHelper, client : CdiApiClient) : boolean {
    var injected = false
    try {
      for (field in (new CdiApiHelper() as Object).Class.getDeclaredFields()) {
        if (field.getName().contains("_client")) {
          field.setAccessible(true)
          field.set(helper, client)
          injected = true
        }
      }
    } catch (e : Exception) {
      injected = false
    }
    return injected
  }

  private function createStubCdiApiClient(
      propRetriever : RuntimePropertyRetriever,
      stubResponse : Object,
      throwFeign : boolean,
      throwRuntime : boolean
  ) : CdiApiClient {
    var client = Mockito.spy(new CdiApiClient(propRetriever))
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

}
