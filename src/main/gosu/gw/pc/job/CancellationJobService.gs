package gw.pc.job

import com.guidewire.pc.model.PolicyPeriod
import gw.pc.config.PCConstants
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CancellationJobService {

  public static function cancelPolicy(period : PolicyPeriod, cancelReason : String, calcMethod : String, cancelEffDateStr : String) : PolicyPeriod {
    print("[GW-LOG] → CancellationJobService.cancelPolicy")
    if (period == null) return null

    period.setStatus(PCConstants.STATUS_CANCELED)
    period.setJobType(PCConstants.JOB_TYPE_CANCELLATION)

    var originalPrem = period?.TotalPremium != null ? period.TotalPremium.doubleValue() : 0.0
    var returnedPrem = 0.0

    // Exact calendar day count calculation if dates present
    var unearnedFactor = 0.50
    if (period.EffectiveDate != null and period.ExpirationDate != null and cancelEffDateStr != null) {
      try {
        var startDate = LocalDate.parse(period.EffectiveDate)
        var endDate = LocalDate.parse(period.ExpirationDate)
        var cancelDate = LocalDate.parse(cancelEffDateStr)

        var totalDays = ChronoUnit.DAYS.between(startDate, endDate)
        var elapsedDays = ChronoUnit.DAYS.between(startDate, cancelDate)

        if (totalDays > 0 and elapsedDays >= 0 and elapsedDays <= totalDays) {
          var earnedRatio = (double)elapsedDays / (double)totalDays
          unearnedFactor = 1.0 - earnedRatio
        }
      } catch (e : Exception) {
        unearnedFactor = 0.50
      }
    }

    if ("ShortRate".equalsIgnoreCase(calcMethod)) {
      // Short-rate applies 90% factor to unearned premium (10% penalty retained by carrier)
      returnedPrem = (originalPrem * unearnedFactor) * PCConstants.SHORT_RATE_RETENTION_FACTOR
    } else {
      // Standard Pro-Rata return
      returnedPrem = originalPrem * unearnedFactor
    }

    var returnedBD = new BigDecimal(String.format("%.2f", {returnedPrem})).setScale(2, RoundingMode.HALF_UP)
    period.setBasePremium(returnedBD.negate())
    period.setTaxesAndFees(BigDecimal.ZERO)
    period.setTotalPremium(returnedBD.negate())

    return period
  }

  public static function reinstatePolicy(period : PolicyPeriod, reinstatementReason : String, hasLapse : boolean, feeAmount : BigDecimal) : PolicyPeriod {
    print("[GW-LOG] → CancellationJobService.reinstatePolicy")
    if (period != null) {
      period.setStatus(PCConstants.STATUS_ISSUED)
      period.setJobType(PCConstants.JOB_TYPE_REINSTATEMENT)
      if (feeAmount != null and feeAmount.compareTo(BigDecimal.ZERO) > 0) {
        period.setTaxesAndFees(feeAmount)
        period.setTotalPremium(feeAmount)
      } else {
        period.setTaxesAndFees(BigDecimal.ZERO)
        period.setTotalPremium(BigDecimal.ZERO)
      }
    }
    return period
  }
}
