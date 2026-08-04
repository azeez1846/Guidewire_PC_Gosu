package gw.pc.job

import com.guidewire.pc.model.PolicyPeriod
import gw.pc.config.PCConstants
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class RenewalJobService {

  public static function startRenewal(period : PolicyPeriod) : PolicyPeriod {
    print("[GW-LOG] → RenewalJobService.startRenewal")
    if (period == null) return null

    var renewal = new PolicyPeriod()
    renewal.setPolicyNumber(period.PolicyNumber)
    renewal.setProductCode(period.ProductCode)
    renewal.setAccount(period.Account)
    renewal.setJobType(PCConstants.JOB_TYPE_RENEWAL)
    renewal.setStatus(PCConstants.STATUS_DRAFT)
    renewal.setProducerCode(period.ProducerCode)
    renewal.setBaseState(period.BaseState)
    renewal.setBodilyInjuryLimit(period.BodilyInjuryLimit)
    renewal.setPropertyDamageLimit(period.PropertyDamageLimit)
    renewal.setCollisionDeductible(period.CollisionDeductible)
    renewal.setComprehensiveDeductible(period.ComprehensiveDeductible)
    renewal.setTermMonths(period.TermMonths > 0 ? period.TermMonths : 12)

    // Calculate dates: Renewal starts when current policy expires
    if (period.ExpirationDate != null and period.ExpirationDate.trim().length() > 0) {
      renewal.setEffectiveDate(period.ExpirationDate)
      try {
        var start = LocalDate.parse(period.ExpirationDate)
        var end = start.plusMonths(renewal.TermMonths)
        renewal.setExpirationDate(end.toString())
      } catch (e : Exception) {
        renewal.setExpirationDate("2028-01-01")
      }
    } else {
      renewal.setEffectiveDate("2027-01-01")
      renewal.setExpirationDate("2028-01-01")
    }

    return renewal
  }

  public static function calculateRenewalQuote(renewal : PolicyPeriod, priorPeriod : PolicyPeriod) : PolicyPeriod {
    print("[GW-LOG] → RenewalJobService.calculateRenewalQuote")
    if (renewal == null) return null

    // Base rate calculation for renewal (5% inflation / rate adjustment)
    var baseRate = 600.0
    if (PCConstants.PRODUCT_PERSONAL_AUTO.equalsIgnoreCase(renewal.ProductCode)) {
      baseRate = 700.0
    } else if (PCConstants.PRODUCT_COMMERCIAL_AUTO.equalsIgnoreCase(renewal.ProductCode)) {
      baseRate = 1350.0
    } else if (PCConstants.PRODUCT_COMMERCIAL_PROPERTY.equalsIgnoreCase(renewal.ProductCode)) {
      baseRate = 2250.0
    }

    if (renewal.TermMonths == 12) {
      baseRate = baseRate * 1.9
    }

    var taxes = baseRate * 0.08
    var baseBD = new BigDecimal(baseRate).setScale(2, RoundingMode.HALF_UP)
    var taxBD = new BigDecimal(taxes).setScale(2, RoundingMode.HALF_UP)

    renewal.setBasePremium(baseBD)
    renewal.setTaxesAndFees(taxBD)
    renewal.setTotalPremium(baseBD.add(taxBD))
    renewal.setStatus(PCConstants.STATUS_QUOTED)

    return renewal
  }

  public static function bindRenewal(renewal : PolicyPeriod) : PolicyPeriod {
    print("[GW-LOG] → RenewalJobService.bindRenewal")
    if (renewal == null) return null
    renewal.setStatus(PCConstants.STATUS_BOUND)
    return renewal
  }
}
