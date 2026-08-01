package gw.pc.rating

import com.guidewire.pc.model.AuditInformation
import com.guidewire.pc.model.PolicyPeriod
import com.guidewire.pc.model.Transaction
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class AuditRatingEngine {

  public static function calculateAuditPremiumAdjustment(auditInfo : AuditInformation, period : PolicyPeriod) : BigDecimal {
    if (auditInfo == null or period == null) return BigDecimal.ZERO

    var estimated = auditInfo.EstimatedExposure
    var audited = auditInfo.AuditedExposure
    var basePrem = period.BasePremium != null ? period.BasePremium : BigDecimal.ZERO

    if (estimated == null or estimated.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO
    }

    // Ratio = Audited Exposure / Estimated Exposure
    var ratio = audited.divide(estimated, 6, RoundingMode.HALF_UP)
    var newAuditedPremium = basePrem.multiply(ratio).setScale(2, RoundingMode.HALF_UP)
    var adjustment = newAuditedPremium.subtract(basePrem).setScale(2, RoundingMode.HALF_UP)

    return adjustment
  }

  public static function createAuditTransaction(auditInfo : AuditInformation, period : PolicyPeriod) : Transaction {
    if (auditInfo == null or period == null) return null

    var adjustment = calculateAuditPremiumAdjustment(auditInfo, period)
    var tx = new Transaction(null, period.PolicyNumber, adjustment, "AuditPremiumAdjustment")
    return tx
  }
}
