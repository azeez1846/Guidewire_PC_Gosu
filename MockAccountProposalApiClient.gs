package xlp.integration.smartcomm

uses feign.FeignException
uses gw.api.properties.RuntimePropertyRetriever
uses gwgen.accountproposal.model.CreateDocumentResponse
uses org.mockito.Mockito

/**
 * Mock Client Class for Account Proposal Integration tests.
 */
class MockAccountProposalApiClient extends AccountProposalApiClient {

  public var mockScenario : String = "SUCCESS"

  construct(servicePropertyRetreiver : RuntimePropertyRetriever) {
    super(servicePropertyRetreiver)
  }

  override function doPost(request : Object) : Object {
    if (mockScenario == "FEIGN_EXCEPTION") {
      var feignEx = Mockito.mock(FeignException)
      Mockito.when(feignEx.status()).thenReturn(500)
      Mockito.when(feignEx.getStackTrace()).thenReturn(new StackTraceElement[0])
      throw feignEx
    } else if (mockScenario == "RUNTIME_EXCEPTION") {
      throw new RuntimeException("Simulated Account Proposal Service Exception")
    } else if (mockScenario == "NULL_RESPONSE") {
      return null
    } else if (mockScenario == "WRONG_TYPE_RESPONSE") {
      return "UNEXPECTED_RESPONSE_TYPE"
    }
    return new CreateDocumentResponse()
  }

}

