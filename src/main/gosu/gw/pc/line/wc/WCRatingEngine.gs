package gw.pc.line.wc

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.math.RoundingMode

class WCRatingEngine {

  public static function rateWorkersComp(period : PolicyPeriod, estimatedPayroll : BigDecimal, expMod : BigDecimal, classRate : BigDecimal) : BigDecimal {
    if (period == null) return BigDecimal.ZERO

    var payroll = estimatedPayroll != null ? estimatedPayroll : new BigDecimal("100000.00")
    var rate = classRate != null ? classRate : new BigDecimal("2.50") // $2.50 per $100 payroll
    var emod = expMod != null ? expMod : new BigDecimal("0.950")

    // Manual Premium = (Payroll / 100) * ClassRate
    var unitsOf100 = payroll.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP)
    var manualPrem = unitsOf100.multiply(rate).setScale(2, RoundingMode.HALF_UP)

    // Standard Earned Premium = Manual Premium * ExpMod
    var standardPrem = manualPrem.multiply(emod).setScale(2, RoundingMode.HALF_UP)

    // State Tax & Expense Constant (+8%)
    var taxAndExpense = standardPrem.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP)
    var totalWcPrem = standardPrem.add(taxAndExpense)

    return totalWcPrem
  }
}
