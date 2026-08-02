package gw.pc.line.cp

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.math.RoundingMode

class CPRatingEngine {

  public static function rateCommercialProperty(period : PolicyPeriod, buildingLimit : BigDecimal, bppLimit : BigDecimal, protectionClass : String) : BigDecimal {
    if (period == null) return BigDecimal.ZERO

    var bldg = buildingLimit != null ? buildingLimit : new BigDecimal("1000000.00")
    var bpp = bppLimit != null ? bppLimit : new BigDecimal("250000.00")
    var totalLimit = bldg.add(bpp)

    // Base Property rate per $100 value (e.g. $0.35 per $100 for Prot Class 1-4, $0.55 for 5-8, $0.85 for 9-10)
    var baseRate = 0.35
    if ("9".equals(protectionClass) or "10".equals(protectionClass)) {
      baseRate = 0.85
    } else if ("5".equals(protectionClass) or "6".equals(protectionClass) or "7".equals(protectionClass) or "8".equals(protectionClass)) {
      baseRate = 0.55
    }

    var unitsOf100 = totalLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP)
    var basePrem = unitsOf100.multiply(new BigDecimal(baseRate)).setScale(2, RoundingMode.HALF_UP)

    // Fire Tax & State Assessment (+5%)
    var tax = basePrem.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP)
    return basePrem.add(tax)
  }
}
