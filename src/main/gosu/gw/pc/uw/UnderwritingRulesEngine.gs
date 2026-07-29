package gw.pc.uw

import com.guidewire.pc.model.PolicyPeriod
import java.util.List
import java.util.ArrayList

class UnderwritingRulesEngine {

  public static function evaluateRules(period : PolicyPeriod) : List<UWIssue> {
    var issues = new ArrayList<UWIssue>()

    if (period == null) return issues

    // Rule 1: High Bodily Injury Limits require Underwriting Approval
    if ("$1M/$1M".equalsIgnoreCase(period.BodilyInjuryLimit)) {
      issues.add(new UWIssue(
        "UW_HIGH_LIMIT",
        "Bodily Injury limit $1M/$1M exceeds standard authority threshold.",
        "BlocksBind"
      ))
    }

    // Rule 2: Commercial Property Base State FL or CA catastrophe zone inspection
    if ("CommercialProperty".equalsIgnoreCase(period.ProductCode) and ("FL".equalsIgnoreCase(period.BaseState) or "CA".equalsIgnoreCase(period.BaseState))) {
      issues.add(new UWIssue(
        "UW_CATASTROPHE_ZONE_HIGH_RISK",
        "Property located in high-risk coastal/wildfire state (" + period.BaseState + ") requires catastrophe survey.",
        "BlocksQuote"
      ))
    }

    // Rule 3: Short term policy (under 6 months)
    if (period.TermMonths < 6) {
      issues.add(new UWIssue(
        "UW_SHORT_TERM_POLICY",
        "Policy term length (" + period.TermMonths + " months) is non-standard.",
        "BlocksIssuance"
      ))
    }

    return issues
  }
}
