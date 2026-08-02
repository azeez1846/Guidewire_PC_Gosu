package gw.pc.line.umbrella

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.math.RoundingMode

class CURatingEngine {

  public static function rateCommercialUmbrella(period : PolicyPeriod, limitAmount : BigDecimal, sirAmount : BigDecimal, underlyingPolicyCount : int) : BigDecimal {
    if (period == null or limitAmount == null) return BigDecimal.ZERO

    // Base umbrella rate per $1,000,000 limit e.g. $1,500 for 1st million, $800 for subsequent millions
    var millions = limitAmount.divide(new BigDecimal("1000000.00"), 2, RoundingMode.HALF_UP)
    var basePrem = new BigDecimal("1500.00")
    if (millions.compareTo(BigDecimal.ONE) > 0) {
      var extraMillions = millions.subtract(BigDecimal.ONE)
      basePrem = basePrem.add(extraMillions.multiply(new BigDecimal("800.00")))
    }

    // Underlying policy count charge (+10% per underlying policy after 2)
    if (underlyingPolicyCount > 2) {
      var surchargeFactor = new BigDecimal(1.00 + ((underlyingPolicyCount - 2) * 0.10))
      basePrem = basePrem.multiply(surchargeFactor).setScale(2, RoundingMode.HALF_UP)
    }

    // Taxes (+5%)
    var tax = basePrem.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP)
    return basePrem.add(tax)
  }
}
