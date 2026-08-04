package gw.pc.uw

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.util.List
import java.util.ArrayList

class UWAuthorityMatrix {

  public static final var ROLE_JUNIOR_UW : String = "Junior Underwriter"
  public static final var ROLE_STANDARD_UW : String = "Underwriter"
  public static final var ROLE_SENIOR_UW : String = "Senior Underwriter"
  public static final var ROLE_UW_MANAGER : String = "Underwriting Manager"
  public static final var ROLE_CHIEF_UW : String = "Chief Underwriter"

  public static function getAuthorityLimitForRole(role : String) : BigDecimal {
    print("[GW-LOG] → UWAuthorityMatrix.getAuthorityLimitForRole")
    if (ROLE_JUNIOR_UW.equalsIgnoreCase(role)) return new BigDecimal("250000.00")
    if (ROLE_STANDARD_UW.equalsIgnoreCase(role)) return new BigDecimal("1000000.00")
    if (ROLE_SENIOR_UW.equalsIgnoreCase(role)) return new BigDecimal("5000000.00")
    if (ROLE_UW_MANAGER.equalsIgnoreCase(role)) return new BigDecimal("20000000.00")
    if (ROLE_CHIEF_UW.equalsIgnoreCase(role)) return new BigDecimal("100000000.00")
    return new BigDecimal("100000.00") // Default entry limit
  }

  public static function getRequiredApprovalRole(issueKey : String, totalPremium : BigDecimal) : String {
    print("[GW-LOG] → UWAuthorityMatrix.getRequiredApprovalRole")
    if ("UW_HIGH_LIMIT".equalsIgnoreCase(issueKey)) {
      return ROLE_SENIOR_UW
    }
    if ("UW_CATASTROPHE_ZONE_HIGH_RISK".equalsIgnoreCase(issueKey)) {
      return ROLE_UW_MANAGER
    }
    if ("UW_LARGE_PREMIUM_EXPOSURE".equalsIgnoreCase(issueKey)) {
      if (totalPremium != null and totalPremium.compareTo(new BigDecimal("50000.00")) > 0) {
        return ROLE_CHIEF_UW
      }
      return ROLE_SENIOR_UW
    }
    return ROLE_STANDARD_UW
  }

  public static function canUserApproveIssue(userRole : String, issueKey : String, totalPremium : BigDecimal) : boolean {
    print("[GW-LOG] → UWAuthorityMatrix.canUserApproveIssue")
    var requiredRole = getRequiredApprovalRole(issueKey, totalPremium)
    var userLimit = getAuthorityLimitForRole(userRole)
    var requiredLimit = getAuthorityLimitForRole(requiredRole)

    return userLimit.compareTo(requiredLimit) >= 0
  }

  public static function evaluateAuthorityIssues(period : PolicyPeriod) : List<UWIssue> {
    print("[GW-LOG] → UWAuthorityMatrix.evaluateAuthorityIssues")
    var issues = new ArrayList<UWIssue>()
    if (period == null) return issues

    // Check large premium exposure
    if (period.TotalPremium != null and period.TotalPremium.compareTo(new BigDecimal("15000.00")) > 0) {
      issues.add(new UWIssue(
        "UW_LARGE_PREMIUM_EXPOSURE",
        "Total policy premium (" + period.TotalPremium + ") exceeds standard bind threshold.",
        "BlocksBind"
      ))
    }

    return issues
  }
}
