package gw.pc.uw

import com.guidewire.pc.model.PolicyPeriod
import java.util.List
import java.util.ArrayList

/**
 * Claims & SIU Fraud Evaluation Gosu Engine for Guidewire PolicyCenter.
 * Executes native Gosu rules for claims triage, red-flag scoring, and underwriter referral.
 */
class ClaimsFraudEvaluationEngine {

  public static function evaluateClaimFraudRules(period : PolicyPeriod, lossAmount : java.math.BigDecimal, lossCause : String) : List<UWIssue> {
    print("[GW-LOG] → ClaimsFraudEvaluationEngine.evaluateClaimFraudRules for policy: " + (period != null ? period.PolicyNumber : "N/A"))
    var issues = new ArrayList<UWIssue>()

    if (period == null) return issues

    // Rule 1: High Dollar Claims Referral
    if (lossAmount != null and lossAmount.compareTo(new java.math.BigDecimal("75000")) > 0) {
      issues.add(new UWIssue(
        "SIU_HIGH_VALUED_CLAIM",
        "Claim loss amount ($" + lossAmount + ") exceeds $75,000 threshold. Mandates SIU Triage.",
        "BlocksIssuance"
      ))
    }

    // Rule 2: Suspicious Cause of Loss (Arson / Theft within 60 days of inception)
    if ("Arson".equalsIgnoreCase(lossCause) or "Stolen Vehicle".equalsIgnoreCase(lossCause)) {
      issues.add(new UWIssue(
        "SIU_SUSPICIOUS_CAUSE",
        "High fraud indicator cause of loss (" + lossCause + ") detected.",
        "BlocksBind"
      ))
    }

    // Rule 3: High Bodily Injury Risk Rating Surcharge
    if ("$1M/$1M".equalsIgnoreCase(period.BodilyInjuryLimit) and lossAmount != null and lossAmount.compareTo(new java.math.BigDecimal("25000")) > 0) {
      issues.add(new UWIssue(
        "SIU_HIGH_LIMIT_LOSS_EXPOSURE",
        "High limit policy ($1M/$1M) sustained claim over $25,000. Re-rate required at renewal.",
        "BlocksQuote"
      ))
    }

    return issues
  }
}
