package gw.pc.rating

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Parametric Catastrophe Rating Engine in Gosu.
 * Calculates actuarial pricing for index-based triggers (hurricane windspeed, seismic magnitude, wildfire acreage).
 */
class ParametricRatingEngine {

  public static function calculateParametricPremium(payoutLimit : BigDecimal, hazardType : String, triggerThreshold : double) : BigDecimal {
    print("[GW-LOG] → ParametricRatingEngine.calculateParametricPremium for " + hazardType + " at threshold " + triggerThreshold)

    if (payoutLimit == null or payoutLimit.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO
    }

    var baseRatePercentage = 0.035 // 3.5% base rate on limit

    if ("HURRICANE_WINDSPEED".equalsIgnoreCase(hazardType)) {
      // Windspeed threshold (knots): lower threshold means higher probability -> higher rate
      if (triggerThreshold <= 100.0) {
        baseRatePercentage = 0.065
      } else if (triggerThreshold <= 125.0) {
        baseRatePercentage = 0.045
      } else {
        baseRatePercentage = 0.030
      }
    } else if ("EARTHQUAKE_SEISMIC".equalsIgnoreCase(hazardType)) {
      // Richter scale: 6.0 = higher risk, 7.5 = catastrophic lower frequency
      if (triggerThreshold <= 6.5) {
        baseRatePercentage = 0.070
      } else if (triggerThreshold <= 7.0) {
        baseRatePercentage = 0.050
      } else {
        baseRatePercentage = 0.032
      }
    } else if ("WILDFIRE_PERIMETER".equalsIgnoreCase(hazardType)) {
      // Proximity in miles
      if (triggerThreshold <= 5.0) {
        baseRatePercentage = 0.080
      } else {
        baseRatePercentage = 0.040
      }
    }

    var calculated = payoutLimit.multiply(new BigDecimal(String.valueOf(baseRatePercentage)))
    return calculated.setScale(2, RoundingMode.HALF_UP)
  }
}
