package gw.pc.line.cp

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.math.RoundingMode

class CPRatingEngine {

  public static function rateCommercialProperty(period : PolicyPeriod, buildingLimit : BigDecimal, bppLimit : BigDecimal, protectionClass : String) : BigDecimal {
    print("[GW-LOG] → CPRatingEngine.rateCommercialProperty")
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

  public static function rateTenantsImprovements(period : PolicyPeriod, limit : BigDecimal, valuationBasis : String) : BigDecimal {
    print("[GW-LOG] → CPRatingEngine.rateTenantsImprovements")
    if (limit == null or limit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO
    
    // Base rate $0.42 per $100 limit. 15% surcharge if ReplacementCost vs ACV.
    var baseRate = new BigDecimal("0.42")
    var unitsOf100 = limit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP)
    var premium = unitsOf100.multiply(baseRate).setScale(2, RoundingMode.HALF_UP)
    
    if ("ReplacementCost".equalsIgnoreCase(valuationBasis)) {
      premium = premium.multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP)
    }
    return premium
  }

  public static function rateBusinessIncome(period : PolicyPeriod, biLimit : BigDecimal, indemnityFraction : String, includePayroll : boolean) : BigDecimal {
    print("[GW-LOG] → CPRatingEngine.rateBusinessIncome")
    if (biLimit == null or biLimit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO

    // Monthly limitation fraction multiplier: 1/3 (0.90x), 1/4 (0.80x), 1/6 (0.65x), NoLimit (1.0x)
    var fractionMultiplier = new BigDecimal("1.00")
    if ("1/3".equals(indemnityFraction)) {
      fractionMultiplier = new BigDecimal("0.90")
    } else if ("1/4".equals(indemnityFraction)) {
      fractionMultiplier = new BigDecimal("0.80")
    } else if ("1/6".equals(indemnityFraction)) {
      fractionMultiplier = new BigDecimal("0.65")
    }

    var baseRate = new BigDecimal("0.50") // $0.50 per $100
    var unitsOf100 = biLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP)
    var premium = unitsOf100.multiply(baseRate).multiply(fractionMultiplier).setScale(2, RoundingMode.HALF_UP)

    if (includePayroll) {
      premium = premium.multiply(new BigDecimal("1.10")).setScale(2, RoundingMode.HALF_UP) // 10% ordinary payroll extension
    }
    return premium
  }

  public static function rateBoilerAndMachinery(period : PolicyPeriod, equipmentLimit : BigDecimal, hasProductionMachinery : boolean) : BigDecimal {
    print("[GW-LOG] → CPRatingEngine.rateBoilerAndMachinery")
    if (equipmentLimit == null or equipmentLimit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO

    var rate = new BigDecimal("0.18") // $0.18 per $100
    var unitsOf100 = equipmentLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP)
    var premium = unitsOf100.multiply(rate).setScale(2, RoundingMode.HALF_UP)

    if (hasProductionMachinery) {
      premium = premium.multiply(new BigDecimal("1.25")).setScale(2, RoundingMode.HALF_UP)
    }
    return premium
  }

  public static function rateBlanketCoverage(period : PolicyPeriod, blanketLimit : BigDecimal, weightedRatePer100 : BigDecimal, coinsurancePct : int) : BigDecimal {
    print("[GW-LOG] → CPRatingEngine.rateBlanketCoverage")
    if (blanketLimit == null or blanketLimit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO

    var rate = weightedRatePer100 != null ? weightedRatePer100 : new BigDecimal("0.45")
    var unitsOf100 = blanketLimit.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP)
    var premium = unitsOf100.multiply(rate).setScale(2, RoundingMode.HALF_UP)

    // Blanket coinsurance credit: 90% -> 5% discount, 100% -> 10% discount
    if (coinsurancePct >= 100) {
      premium = premium.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP)
    } else if (coinsurancePct >= 90) {
      premium = premium.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP)
    }
    return premium
  }

  public static function calculateCoinsurancePayout(reportedCoverageLimit : BigDecimal, coinsuranceReqPct : int, fullPropertyActualValue : BigDecimal, lossAmount : BigDecimal, deductible : BigDecimal) : BigDecimal {
    print("[GW-LOG] → CPRatingEngine.calculateCoinsurancePayout")
    if (lossAmount == null or lossAmount.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO
    if (fullPropertyActualValue == null or fullPropertyActualValue.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO

    var reqPctDecimal = new BigDecimal(coinsuranceReqPct).divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP)
    var requiredInsurance = fullPropertyActualValue.multiply(reqPctDecimal).setScale(2, RoundingMode.HALF_UP)
    var actualLimit = reportedCoverageLimit != null ? reportedCoverageLimit : BigDecimal.ZERO

    var coinsuranceFactor = BigDecimal.ONE
    if (actualLimit.compareTo(requiredInsurance) < 0) {
      coinsuranceFactor = actualLimit.divide(requiredInsurance, 4, RoundingMode.HALF_UP)
    }

    var grossClaimPayout = lossAmount.multiply(coinsuranceFactor).setScale(2, RoundingMode.HALF_UP)
    var ded = deductible != null ? deductible : BigDecimal.ZERO
    var netPayout = grossClaimPayout.subtract(ded)
    if (netPayout.compareTo(BigDecimal.ZERO) < 0) {
      return BigDecimal.ZERO
    }
    return netPayout.min(actualLimit)
  }
}
