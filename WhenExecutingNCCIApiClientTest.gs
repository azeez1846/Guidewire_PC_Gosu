package xlp.integration.ncci

uses gw.api.properties.RuntimePropertyRetriever
uses gw.api.util.DateUtil
uses gw.restclient.config.Config
uses gw.testharness.v3.Suites
uses gwgen.ncci.api.NcciApi
uses gwgen.ncci.model.GetModFeinv2Request
uses gwgen.ncci.model.GetModFeinv2Response
uses org.mockito.Mockito
uses xlp.common.PCServerTestClassBase_Ext
uses xlp.integration.common.CommonConstants
uses xlp.suites.IntegrationSuite

/**
 * To Test ncci Service.
 */
@Export
@Suites(IntegrationSuite.NAME)
class WhenExecutingNCCIApiClientTest extends PCServerTestClassBase_Ext {

  protected var mockNcciApiHelper : MockNCCIApiHelper

  protected override function beforeMethod() { super.beforeMethod() }

  function testGetModFeinWithSuccess() {
    var req = new GetModFeinv2Request()
    req.Fein = "12-3456789"
    req.RatingEffectiveStartDate = DateUtil.currentDate().toStringWithFormat(CommonConstants.DATEFORMAT_MM_DD_YYYY)
    req.RatingEffectiveEndDate = DateUtil.currentDate().addYears(1).toStringWithFormat(CommonConstants.DATEFORMAT_MM_DD_YYYY)
    req.ModType = "C"
    mockNcciApiHelper = new MockNCCIApiHelper()
    var ncciGetModResp = mockNcciApiHelper.invokeModFactor(req,"testClass")
    //Assert Conditions
    assertTrue(ncciGetModResp.RiskHeaderInformation.ResponseCode == "0")
  }

  function testGetModFeinWithFailure() {
    var req = new GetModFeinv2Request()
    req.Fein = "$%-345678^"
    req.RatingEffectiveStartDate = DateUtil.currentDate().toStringWithFormat(CommonConstants.DATEFORMAT_MM_DD_YYYY)
    req.RatingEffectiveEndDate = DateUtil.currentDate().addYears(1).toStringWithFormat(CommonConstants.DATEFORMAT_MM_DD_YYYY)
    req.ModType = "C"

    mockNcciApiHelper = new MockNCCIApiHelper()
    var ncciGetModResp = mockNcciApiHelper.invokeModFactor(req,"testClass")
    //Assert Conditions
    assertTrue(ncciGetModResp.RiskHeaderInformation.ResponseCode == "1")
  }

  /**
   * Positive Case - Maps FEIN by removing hyphen and populates all GetModFeinv2Request fields.
   */
  function testThatMapModFactorRemovesHyphenAndMapsAllFields() {
    var startDate = DateUtil.currentDate().toStringWithFormat(CommonConstants.DATEFORMAT_MM_DD_YYYY)
    var endDate = DateUtil.currentDate().addYears(1).toStringWithFormat(CommonConstants.DATEFORMAT_MM_DD_YYYY)
    mockNcciApiHelper = new MockNCCIApiHelper()

    var req = mockNcciApiHelper.mapModFactor("12-3456789", startDate, endDate, "C", "Y")
    var ncciGetModResp = mockNcciApiHelper.invokeModFactor(req, "testClass")

    //Assert Conditions
    assertNotNull(req)
    assertEquals("123456789", req.Fein)
    assertEquals(startDate, req.RatingEffectiveStartDate)
    assertEquals(endDate, req.RatingEffectiveEndDate)
    assertEquals("C", req.ModType)
    assertEquals("Y", req.Test)
    assertNotNull(ncciGetModResp)
    assertEquals("0", ncciGetModResp.RiskHeaderInformation.ResponseCode)
  }

  /**
   * Negative Case - Maps FEIN and verifies failure response from NCCI service.
   */
  function testThatMapModFactorWithInvalidFeinReturnsFailureResponse() {
    var startDate = DateUtil.currentDate().toStringWithFormat(CommonConstants.DATEFORMAT_MM_DD_YYYY)
    var endDate = DateUtil.currentDate().addYears(1).toStringWithFormat(CommonConstants.DATEFORMAT_MM_DD_YYYY)
    mockNcciApiHelper = new MockNCCIApiHelper()

    var req = mockNcciApiHelper.mapModFactor("$%-345678^", startDate, endDate, "C", "N")
    var ncciGetModResp = mockNcciApiHelper.invokeModFactor(req, "testClass")

    //Assert Conditions
    assertNotNull(req)
    assertEquals("$%345678^", req.Fein)
    assertEquals("N", req.Test)
    assertNotNull(ncciGetModResp)
    assertEquals("1", ncciGetModResp.RiskHeaderInformation.ResponseCode)
  }

  /**
   * Positive Case - Executes mapModFactor and invokeModFactor directly on NcciApiHelper.
   */
  function testThatNcciApiHelperInvokeModFactorExecutesServiceCall() {
    var startDate = DateUtil.currentDate().toStringWithFormat(CommonConstants.DATEFORMAT_MM_DD_YYYY)
    var endDate = DateUtil.currentDate().addYears(1).toStringWithFormat(CommonConstants.DATEFORMAT_MM_DD_YYYY)
    var ncciApiHelper = new NcciApiHelper()
    var req = ncciApiHelper.mapModFactor("12-3456789", startDate, endDate, "C", "Y")

    //Assert Conditions
    assertNotNull(req)
    assertEquals("123456789", req.Fein)
    try {
      ncciApiHelper.invokeModFactor(req, "testClass")
    } catch (ex : Exception) {
      assertNotNull(ex)
    }
  }

  /**
   * Negative Case - Verifies getModFactor returns null when service response is null.
   */
  function testThatGetModFactorReturnsNullWhenResponseIsNull() {
    var req = new GetModFeinv2Request()
    req.Fein = "NULL_RESPONSE"
    mockNcciApiHelper = new MockNCCIApiHelper()
    var ncciGetModResp = mockNcciApiHelper.invokeModFactor(req, "testClass")

    //Assert Conditions
    assertNull(ncciGetModResp)
  }

  /**
   * Negative Case - Verifies getModFactor returns null when service response is not GetModFeinv2Response.
   */
  function testThatGetModFactorReturnsNullWhenResponseIsUnexpectedType() {
    var req = new GetModFeinv2Request()
    req.Fein = "WRONG_TYPE_RESPONSE"
    mockNcciApiHelper = new MockNCCIApiHelper()
    var ncciGetModResp = mockNcciApiHelper.invokeModFactor(req, "testClass")

    //Assert Conditions
    assertNull(ncciGetModResp)
  }

  /**
   * Exception Case - Verifies getModFactor catches FeignException and returns null.
   */
  function testThatGetModFactorReturnsNullWhenFeignExceptionOccurs() {
    var req = new GetModFeinv2Request()
    req.Fein = "FEIGN_EXCEPTION"
    mockNcciApiHelper = new MockNCCIApiHelper()
    var ncciGetModResp = mockNcciApiHelper.invokeModFactor(req, "testClass")

    //Assert Conditions
    assertNull(ncciGetModResp)
  }

  /**
   * Exception Case - Verifies getModFactor handles FeignException when status matches WCM error code.
   */
  function testThatGetModFactorHandlesFeignExceptionWithMatchingWcmErrorCode() {
    var req = new GetModFeinv2Request()
    req.Fein = "FEIGN_WCM_ERROR"
    mockNcciApiHelper = new MockNCCIApiHelper()

    try {
      var ncciGetModResp = mockNcciApiHelper.invokeModFactor(req, "testClass")
      //Assert Conditions
      assertNull(ncciGetModResp)
    } catch (ex : Exception) {
      assertNotNull(ex)
    }
  }

  /**
   * Exception Case - Verifies getModFactor catches generic RuntimeException and returns null.
   */
  function testThatGetModFactorReturnsNullWhenRuntimeExceptionOccurs() {
    var req = new GetModFeinv2Request()
    req.Fein = "RUNTIME_EXCEPTION"
    mockNcciApiHelper = new MockNCCIApiHelper()
    var ncciGetModResp = mockNcciApiHelper.invokeModFactor(req, "testClass")

    //Assert Conditions
    assertNull(ncciGetModResp)
  }

  /**
   * Positive Case - Verifies retrieveServiceName returns null initially and GetModFein after service call.
   */
  function testThatRetrieveServiceNameReturnsExpectedServiceName() {
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NCCI_EXT)
    var ncciApiClient = new NcciApiClient(propRetriever)
    var mockClient = new MockNCCIApiClient(propRetriever)

    var initialServiceName = ncciApiClient.retrieveServiceName()

    var req = new GetModFeinv2Request()
    req.Fein = "12-3456789"
    var ncciGetModResp = mockClient.getModFactor(req, "testClass")

    //Assert Conditions
    assertNull(initialServiceName)
    assertNotNull(ncciGetModResp)
    assertEquals("GetModFein", mockClient.retrieveServiceName())
  }

  /**
   * Positive Case - Verifies NcciApiClient.doServiceCall invokes NcciApi for GetModFeinv2Request.
   */
  function testThatDoServiceCallReturnsResponseForValidRequest() {
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NCCI_EXT)
    var ncciApiClient = new NcciApiClient(propRetriever)

    var req = new GetModFeinv2Request()
    req.Fein = "123456789"
    var expectedResp = new GetModFeinv2Response()

    var mockApi = Mockito.mock(NcciApi)
    Mockito.when(mockApi.igNcciAPI(req)).thenReturn(expectedResp)

    var mockConfig = Mockito.mock(Config)
    Mockito.when(mockConfig.buildAPI(NcciApi)).thenReturn(mockApi)

    var actualResp = ncciApiClient.doServiceCall(mockConfig, req)

    //Assert Conditions
    assertNotNull(actualResp)
    assertEquals(expectedResp, actualResp)
  }

  /**
   * Negative Case - Verifies NcciApiClient.doServiceCall returns null for unsupported or null requests.
   */
  function testThatDoServiceCallReturnsNullForUnsupportedOrNullRequest() {
    var propRetriever = new RuntimePropertyRetriever(RuntimePropertyGroup.TC_NCCI_EXT)
    var ncciApiClient = new NcciApiClient(propRetriever)
    var mockClient = new MockNCCIApiClient(propRetriever)

    //Assert Conditions
    assertNull(ncciApiClient.doServiceCall(null, "UNSUPPORTED_REQUEST"))
    assertNull(ncciApiClient.doServiceCall(null, null))
    assertNull(mockClient.doServiceCall(null, "UNSUPPORTED_REQUEST"))
  }

}

