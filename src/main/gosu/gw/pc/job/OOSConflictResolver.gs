package gw.pc.job

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.ArrayList
import java.util.List

class OOSConflictResolver {

  public static function detectConflicts(oosBranch : PolicyPeriod, latestPeriod : PolicyPeriod) : List<String> {
    print("[GW-LOG] → OOSConflictResolver.detectConflicts")
    var conflicts = new ArrayList<String>()
    if (oosBranch == null or latestPeriod == null) return conflicts

    if (oosBranch.BodilyInjuryLimit != null and !oosBranch.BodilyInjuryLimit.equalsIgnoreCase(latestPeriod.BodilyInjuryLimit)) {
      conflicts.add("OOS_CONFLICT: Bodily Injury Limit conflict between OOS Branch (" + oosBranch.BodilyInjuryLimit + ") and Latest Bound Period (" + latestPeriod.BodilyInjuryLimit + ")")
    }

    if (oosBranch.CollisionDeductible != null and !oosBranch.CollisionDeductible.equalsIgnoreCase(latestPeriod.CollisionDeductible)) {
      conflicts.add("OOS_CONFLICT: Collision Deductible conflict between OOS Branch (" + oosBranch.CollisionDeductible + ") and Latest Bound Period (" + latestPeriod.CollisionDeductible + ")")
    }

    return conflicts
  }

  public static function calculateOOSPremiumDelta(oosBranch : PolicyPeriod, latestPeriod : PolicyPeriod, newAnnualPremium : BigDecimal) : BigDecimal {
    print("[GW-LOG] → OOSConflictResolver.calculateOOSPremiumDelta")
    if (oosBranch == null or latestPeriod == null or newAnnualPremium == null) return BigDecimal.ZERO

    try {
      var eff = LocalDate.parse(oosBranch.EffectiveDate)
      var exp = LocalDate.parse(oosBranch.ExpirationDate)
      var totalTermDays = ChronoUnit.DAYS.between(eff, exp)
      var remainingDays = ChronoUnit.DAYS.between(eff, exp)

      if (totalTermDays <= 0) return BigDecimal.ZERO

      var origPrem = latestPeriod.TotalPremium != null ? latestPeriod.TotalPremium : BigDecimal.ZERO
      var annualDelta = newAnnualPremium.subtract(origPrem)
      var dayRatio = new BigDecimal(remainingDays).divide(new BigDecimal(totalTermDays), 6, RoundingMode.HALF_UP)
      var proratedDelta = annualDelta.multiply(dayRatio).setScale(2, RoundingMode.HALF_UP)

      return proratedDelta
    } catch (e : Exception) {
      return BigDecimal.ZERO
    }
  }
}
