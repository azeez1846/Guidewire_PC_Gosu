package gw.pc.job

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal

class CancellationJobService {

  public static function cancelPolicy(period : PolicyPeriod, cancelReason : String, calcMethod : String, cancelEffDateStr : String) : PolicyPeriod {
    period.setStatus("Canceled")
    period.setJobType("Cancellation")

    // Calculate unearned returned premium
    var originalPrem = period.TotalPremium != null ? period.TotalPremium.doubleValue() : 0.0
    var returnedPrem = 0.0

    if ("ShortRate".equalsIgnoreCase(calcMethod)) {
      // 90% Pro-rata return (10% short rate penalty)
      returnedPrem = (originalPrem * 0.5) * 0.90
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
    period.setStatus("Issued")
    period.setJobType("Reinstatement")
    return period
  }
}
