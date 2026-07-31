package gw.pc.job

import com.guidewire.pc.model.PolicyPeriod
import gw.pc.config.PCConstants
import java.math.BigDecimal
import java.math.RoundingMode

enhancement RenewalEnhancement : PolicyPeriod {

  public property get PriorTermPolicyNumber() : String {
    return this.PolicyNumber
  }

  public function isEligibleForAutoRenewal() : boolean {
    if (this.Status != PCConstants.STATUS_ISSUED) {
      return false
    }
    // High premium policies or high-risk states require manual UW triage
    if (this.TotalPremium != null and this.TotalPremium.compareTo(new BigDecimal("10000.00")) > 0) {
      return false
    }
    if ("FL".equalsIgnoreCase(this.BaseState) or "CA".equalsIgnoreCase(this.BaseState)) {
      return false
    }
    return true
  }

  public function calculateRenewalRateDiff(newPremium : BigDecimal) : BigDecimal {
    if (this.TotalPremium == null or this.TotalPremium.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO
    }
    var diff = newPremium.subtract(this.TotalPremium)
    var ratio = diff.divide(this.TotalPremium, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100.00"))
    return ratio.setScale(2, RoundingMode.HALF_UP)
  }
}
