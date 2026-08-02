package gw.pc.line.auto

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.math.RoundingMode

class CommercialAutoRatingEngine {

  public static function rateCommercialAuto(period : PolicyPeriod, vehicleCount : int, isFleet : boolean, radius : String) : BigDecimal {
    if (period == null or vehicleCount <= 0) return BigDecimal.ZERO

    var basePerVehicle = 1200.00
    if ("LongDistance".equalsIgnoreCase(radius)) {
      basePerVehicle = 2200.00
    } else if ("Intermediate".equalsIgnoreCase(radius)) {
      basePerVehicle = 1600.00
    }

    var totalBase = new BigDecimal(basePerVehicle * vehicleCount)

    // Fleet discount (-10% for fleets of 5+ vehicles)
    if (isFleet or vehicleCount >= 5) {
      totalBase = totalBase.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP)
    }

    // Taxes & State Surcharges (+6%)
    var tax = totalBase.multiply(new BigDecimal("0.06")).setScale(2, RoundingMode.HALF_UP)
    return totalBase.add(tax)
  }
}
