package gw.pc.rating

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal

class GosuRatingEngine {

  public static function ratePolicy(period : PolicyPeriod) {
    if (period == null) return

    var baseRate = 500.0

    // 1. Line-level base rates
    if ("PersonalAuto".equalsIgnoreCase(period.ProductCode)) {
      baseRate = 650.0
    } else if ("CommercialAuto".equalsIgnoreCase(period.ProductCode)) {
      baseRate = 1250.0
    } else if ("CommercialProperty".equalsIgnoreCase(period.ProductCode)) {
      baseRate = 2100.0
    } else if ("GeneralLiability".equalsIgnoreCase(period.ProductCode)) {
      baseRate = 1800.0
    }

    // 2. Term factor
    if (period.TermMonths == 12) {
      baseRate = baseRate * 1.9
    } else if (period.TermMonths == 6) {
      baseRate = baseRate * 1.0
    }

    // 3. Limits & Deductibles
    if ("$500k/$500k".equals(period.BodilyInjuryLimit)) {
      baseRate = baseRate + 250.0
    } else if ("$1M/$1M".equals(period.BodilyInjuryLimit)) {
      baseRate = baseRate + 500.0
    }

    if ("$250k".equals(period.PropertyDamageLimit)) {
      baseRate = baseRate + 150.0
    } else if ("$500k".equals(period.PropertyDamageLimit)) {
      baseRate = baseRate + 300.0
    }

    if ("$250".equals(period.ComprehensiveDeductible)) {
      baseRate = baseRate + 100.0
    } else if ("$1000".equals(period.ComprehensiveDeductible)) {
      baseRate = baseRate - 75.0
    }

    // 4. Policy Job Proration (Mid-Term Endorsement / MTA adjustment)
    if ("PolicyChange".equalsIgnoreCase(period.JobType)) {
      baseRate = baseRate * 0.5
    }

    // 5. State Tax Rate Matrix
    var taxPercent = 0.08 // CA default
    if ("NY".equalsIgnoreCase(period.BaseState)) {
      taxPercent = 0.075
    } else if ("TX".equalsIgnoreCase(period.BaseState)) {
      taxPercent = 0.06
    } else if ("FL".equalsIgnoreCase(period.BaseState)) {
      taxPercent = 0.09
    } else if ("IL".equalsIgnoreCase(period.BaseState)) {
      taxPercent = 0.07
    }

    var basePremBD = new BigDecimal(String.format("%.2f", {baseRate}))
    var taxesBD = new BigDecimal(String.format("%.2f", {baseRate * taxPercent}))
    var totalPremBD = basePremBD.add(taxesBD)

    period.BasePremium = basePremBD
    period.TaxesAndFees = taxesBD
    period.TotalPremium = totalPremBD
  }
}
