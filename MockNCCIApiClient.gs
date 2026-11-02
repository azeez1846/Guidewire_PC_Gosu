package xlp.integration.ncci

uses feign.FeignException
uses gw.api.properties.RuntimePropertyRetriever
uses gw.restclient.config.Config
uses gwgen.ncci.model.GetModFeinv2Request
uses gwgen.ncci.model.GetModFeinv2Response
uses org.mockito.Mockito
uses xlp.integration.common.CommonConstants

class MockNCCIApiClient extends NcciApiClient {

  public var SERVICE_PROP_RETRIEVER : RuntimePropertyRetriever

  construct(servicePropertyRetreiver : RuntimePropertyRetriever) {
    super(servicePropertyRetreiver)
    SERVICE_PROP_RETRIEVER = servicePropertyRetreiver
  }

  override function getConfig(baseUrl : String, maxRetryAttempts : Integer, connectTimeoutMillis : Integer, readTimeoutMillis : Integer, correlationId : String, oauthToken : String) : Config {
    return super.getConfig(baseUrl, maxRetryAttempts, connectTimeoutMillis, readTimeoutMillis, correlationId, oauthToken)
  }

  override function doGet(request : Object) : Object { return super.doGet(request) }

  override function getModFactor(request : GetModFeinv2Request, serviceCallFrom : String) : GetModFeinv2Response {
    return super.getModFactor(request, serviceCallFrom)
  }

  override function retrieveServiceName() : String { return super.retrieveServiceName() }

  override function doServiceCall(config : Config, request : Object) : Object {
    var mockJsonClient = new MockNCCIApiClientFromJson(SERVICE_PROP_RETRIEVER)
    if (request typeis GetModFeinv2Request) {
      if (request.Fein == "12-3456789" or request.Fein == "123456789") {
        mockJsonClient.initializeMockResponseFromJson(MockNCCIApiClientFromJson.MockType.NCCI_GETMOD_SUCCESS)
      } else if (request.Fein == "$%-345678^" or request.Fein == "$%345678^") {
        mockJsonClient.initializeMockResponseFromJson(MockNCCIApiClientFromJson.MockType.NCCI_GETMOD_FAILURE)
      } else if (request.Fein == "FEIGN_EXCEPTION") {
        var feignEx = Mockito.mock(FeignException)
        Mockito.when(feignEx.status()).thenReturn(-1)
        Mockito.when(feignEx.getStackTrace()).thenReturn(new StackTraceElement[0])
        throw feignEx
      } else if (request.Fein == "FEIGN_WCM_ERROR") {
        var feignEx = Mockito.mock(FeignException)
        var errorCodes = CommonConstants.WCM_Error_Code.get(CommonConstants.Key_Code)
        var statusCode = (errorCodes != null and errorCodes.HasElements) ? Integer.parseInt(errorCodes.first().toString().trim()) : 404
        Mockito.when(feignEx.status()).thenReturn(statusCode)
        Mockito.when(feignEx.getStackTrace()).thenReturn(new StackTraceElement[0])
        throw feignEx
      } else if (request.Fein == "RUNTIME_EXCEPTION") {
        throw new RuntimeException("Simulated NCCI Service Exception")
      } else if (request.Fein == "NULL_RESPONSE") {
        return null
      } else if (request.Fein == "WRONG_TYPE_RESPONSE") {
        return "UNEXPECTED_RESPONSE_TYPE"
      } else {
        mockJsonClient.initializeMockResponseFromJson(MockNCCIApiClientFromJson.MockType.NCCI_GETMOD_FAILURE)
      }
      return mockJsonClient.generateGetModRespObject()
    }
    return null
  }

}

