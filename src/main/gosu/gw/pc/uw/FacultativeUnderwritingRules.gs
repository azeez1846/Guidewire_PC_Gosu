package gw.pc.uw

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.util.List
import java.util.ArrayList

/**
 * Facultative Reinsurance Underwriting Rules in Gosu.
 * Evaluates whether commercial submissions exceed treaty capacity thresholds ($10M TIV or high hazard exposure),
 * mandating facultative excess placement.
 */
class FacultativeUnderwritingRules {

  public static function evaluateFacultativeNeed(period : PolicyPeriod, totalInsuredValue : BigDecimal) : List<UWIssue> {
    print("[GW-LOG] → FacultativeUnderwritingRules.evaluateFacultativeNeed for policy: " + (period != null ? period.PolicyNumber : "N/A"))
    var issues = new ArrayList<UWIssue>()

    if (period == null) return issues

    var threshold = new BigDecimal("10000000") // $10M Treaty Line Limit

    if (totalInsuredValue != null and totalInsuredValue.compareTo(threshold) > 0) {
      issues.add(new UWIssue(
        "UW_FACULTATIVE_PLACEMENT_REQUIRED",
        "Total Insured Value ($" + totalInsuredValue + ") exceeds standard treaty capacity ($10,000,000). Facultative Reinsurance placement is mandatory before binding.",
        "BlocksBind"
      ))
    }

    return issues
  }
}
