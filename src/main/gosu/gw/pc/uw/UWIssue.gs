package gw.pc.uw

class UWIssue {
  private var _issueKey : String
  private var _shortDescription : String
  private var _blockingPoint : String // BlocksQuote, BlocksBind, BlocksIssuance
  private var _approved : boolean = false
  private var _approvedBy : String

  public construct(key : String, desc : String, blockingPoint : String) {
    _issueKey = key
    _shortDescription = desc
    _blockingPoint = blockingPoint
    _approved = false
  }

  public property get IssueKey() : String { return _issueKey }
  public property get ShortDescription() : String { return _shortDescription }
  public property get BlockingPoint() : String { return _blockingPoint }
  public property get Approved() : boolean { return _approved }
  public property set Approved(val : boolean) { _approved = val }
  public property get ApprovedBy() : String { return _approvedBy }
  public property set ApprovedBy(user : String) { _approvedBy = user }

  public function approve(username : String) {
    _approved = true
    _approvedBy = username
  }
}
