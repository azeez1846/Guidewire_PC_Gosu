package gw.pc.line.gl

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.math.RoundingMode

class GLRatingEngine {

  public static function rateGeneralLiability(period : PolicyPeriod, exposureBasisAmount : BigDecimal, baseRatePer1000 : BigDecimal, isClaimsMade : boolean) : BigDecimal {
    if (period == null or exposureBasisAmount == null) return BigDecimal.ZERO

    var rate = baseRatePer1000 != null ? baseRatePer1000 : new BigDecimal("4.50") // $4.50 per $1,000 exposure
    var unitsOf1000 = exposureBasisAmount.divide(new BigDecimal("1000.00"), 4, RoundingMode.HALF_UP)
    var basePrem = unitsOf1000.multiply(rate).setScale(2, RoundingMode.HALF_UP)

    // Claims-Made discount (e.g. 0.85 for 1st year Claims-Made vs 1.00 for Occurrence)
    if (isClaimsMade) {
      basePrem = basePrem.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP)
    }

    // Taxes & Surcharges (+7%)
    var tax = basePrem.multiply(new BigDecimal("0.07")).setScale(2, RoundingMode.HALF_UP)
    return basePrem.add(tax)
  }
}
