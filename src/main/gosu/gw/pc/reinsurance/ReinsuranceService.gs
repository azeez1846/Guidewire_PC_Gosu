package gw.pc.reinsurance

import com.guidewire.pc.model.PolicyPeriod
import com.guidewire.pc.model.RIAgreement
import com.guidewire.pc.model.RICession
import java.math.BigDecimal
import java.math.RoundingMode

class ReinsuranceService {

  public static function calculateCession(period : PolicyPeriod, agreement : RIAgreement, grossLimit : BigDecimal) : RICession {
    print("[GW-LOG] → ReinsuranceService.calculateCession")
    if (period == null or agreement == null) return null

    var cession = new RICession()
    var grossExposure = grossLimit != null ? grossLimit : new BigDecimal("1000000.00")
    var grossPremium = period.TotalPremium != null ? period.TotalPremium : BigDecimal.ZERO

    cession.setGrossRiskExposure(grossExposure)
    cession.setGrossWrittenPremium(grossPremium)

    if ("QuotaShare".equalsIgnoreCase(agreement.AgreementType)) {
      var pct = agreement.CedingPercentage.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP)
      var cededExp = grossExposure.multiply(pct).setScale(2, RoundingMode.HALF_UP)
      var cededPrem = grossPremium.multiply(pct).setScale(2, RoundingMode.HALF_UP)

      cession.setCededExposure(cededExp)
      cession.setRetainedExposure(grossExposure.subtract(cededExp))
      cession.setCededPremium(cededPrem)
    } else if ("ExcessOfLoss".equalsIgnoreCase(agreement.AgreementType)) {
      var attach = agreement.AttachmentPoint != null ? agreement.AttachmentPoint : BigDecimal.ZERO
      if (grossExposure.compareTo(attach) > 0) {
        var excess = grossExposure.subtract(attach)
        var limit = agreement.GrossRetentionLimit
        var cededExp = excess.compareTo(limit) > 0 ? limit : excess
        var retainedExp = grossExposure.subtract(cededExp)

        var ratio = grossExposure.compareTo(BigDecimal.ZERO) > 0 ?
          cededExp.divide(grossExposure, 6, RoundingMode.HALF_UP) : BigDecimal.ZERO
        var cededPrem = grossPremium.multiply(ratio).setScale(2, RoundingMode.HALF_UP)

        cession.setCededExposure(cededExp)
        cession.setRetainedExposure(retainedExp)
        cession.setCededPremium(cededPrem)
      } else {
        cession.setCededExposure(BigDecimal.ZERO)
        cession.setRetainedExposure(grossExposure)
        cession.setCededPremium(BigDecimal.ZERO)
      }
    }

    if (grossExposure.compareTo(agreement.GrossRetentionLimit) > 0) {
      cession.setRequiresFacultative(true)
    }

    return cession
  }
}
