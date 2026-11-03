package xlp.integration.smartcomm

uses gw.api.properties.RuntimePropertyRetriever
uses gwgen.accountproposal.model.AccAccount
uses gwgen.accountproposal.model.CreateDocumentResponse
uses gwgen.accountproposal.model.PcAccDocumentCreationRequest

/**
 * Mock Helper Class for Account Proposal Integration tests.
 */
class MockAccountProposalApiHelper extends AccountProposalApiHelper {

  public var mockScenario : String = "SUCCESS"
  private var _client = new MockAccountProposalApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_DPS_GC))

  override function mapAccount(account : Account, rootObj : PcAccDocumentCreationRequest) {
    try {
      super.mapAccount(account, rootObj)
    } catch (ex : Exception) {
      // Fallback to empty AccAccount when mocked PolicyPeriod has no persisted Account
    }
    if (rootObj.Account == null) {
      rootObj.Account = new AccAccount()
    }
  }

  override function invokeAccountProposal(request : PcAccDocumentCreationRequest) : CreateDocumentResponse {
    _client.mockScenario = mockScenario
    var docIdentifier = UUID.randomUUID().toString()
    return _client.accountProposal(docIdentifier, request)
  }

}

