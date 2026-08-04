package gw.pc.uw

import java.math.BigDecimal

class ApprovalAuthority {
  private var _username : String
  private var _maxPolicyLimit : BigDecimal
  private var _canApproveHighRisk : boolean

  public construct(user : String, maxLimit : BigDecimal, highRisk : boolean) {
    _username = user
    _maxPolicyLimit = maxLimit
    _canApproveHighRisk = highRisk
  }

  public property get Username() : String { return _username }
  public property get MaxPolicyLimit() : BigDecimal { return _maxPolicyLimit }
  public property get CanApproveHighRisk() : boolean { return _canApproveHighRisk }

  public function canApproveIssue(issue : UWIssue, requestedLimit : BigDecimal) : boolean {
    print("[GW-LOG] → ApprovalAuthority.canApproveIssue")
    if (requestedLimit != null and requestedLimit.compareTo(_maxPolicyLimit) > 0) {
      return false
    }
    if (issue.IssueKey.contains("HIGH_RISK") and !_canApproveHighRisk) {
      return false
    }
    return true
  }

  public static function getUnderwriterProfile(username : String) : ApprovalAuthority {
    print("[GW-LOG] → ApprovalAuthority.getUnderwriterProfile")
    if ("su".equalsIgnoreCase(username) or "uw_senior".equalsIgnoreCase(username)) {
      return new ApprovalAuthority(username, new BigDecimal("10000000.00"), true)
    }
    // Standard Underwriter
    return new ApprovalAuthority(username, new BigDecimal("1000000.00"), false)
  }
}
