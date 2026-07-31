package gw.pc.job

import com.guidewire.pc.model.PolicyPeriod
import gw.pc.config.PCConstants
import java.math.BigDecimal

class CancellationJobService {

  public static function cancelPolicy(period : PolicyPeriod, cancelReason : String, calcMethod : String, cancelEffDateStr : String) : PolicyPeriod {
    if (period == null) return null

    period.setStatus(PCConstants.STATUS_CANCELED)
    period.setJobType(PCConstants.JOB_TYPE_CANCELLATION)

    // Calculate unearned returned premium
    var originalPrem = period?.TotalPremium != null ? period.TotalPremium.doubleValue() : 0.0
    var returnedPrem = 0.0

    if ("ShortRate".equalsIgnoreCase(calcMethod)) {
      // Short-rate retention penalty
      returnedPrem = (originalPrem * 0.5) * PCConstants.SHORT_RATE_RETENTION_FACTOR
    } else {
      // Standard Pro-Rata return (50% unearned mid-term)
      returnedPrem = originalPrem * 0.50
    }

    var returnedBD = new BigDecimal(String.format("%.2f", {returnedPrem}))
    period.setBasePremium(returnedBD.negate())
    period.setTaxesAndFees(BigDecimal.ZERO)
    period.setTotalPremium(returnedBD.negate())

    return period
  }

  public static function reinstatePolicy(period : PolicyPeriod, reinstatementReason : String) : PolicyPeriod {
    if (period != null) {
      period.setStatus(PCConstants.STATUS_ISSUED)
      period.setJobType(PCConstants.JOB_TYPE_REINSTATEMENT)
    }
    return period
  }
}
