package gw.pc.job

import com.guidewire.pc.model.PolicyPeriod
import gw.pc.config.PCConstants
import java.time.LocalDate

class OOSEndorsementEngine {

  public static function isOutOfSequence(oosEffDate : String, priorEndorsementEffDates : List<String>) : boolean {
    print("[GW-LOG] → OOSEndorsementEngine.isOutOfSequence")
    if (oosEffDate == null or priorEndorsementEffDates == null or priorEndorsementEffDates.isEmpty()) return false

    try {
      var current = LocalDate.parse(oosEffDate)
      for (d in priorEndorsementEffDates) {
        var prior = LocalDate.parse(d)
        if (current.isBefore(prior)) {
          return true
        }
      }
    } catch (e : Exception) {
      return false
    }

    return false
  }

  public static function createOOSBranch(period : PolicyPeriod, oosEffDate : String) : PolicyPeriod {
    print("[GW-LOG] → OOSEndorsementEngine.createOOSBranch")
    if (period == null) return null

    var oosBranch = new PolicyPeriod()
    oosBranch.setPolicyNumber(period.PolicyNumber)
    oosBranch.setProductCode(period.ProductCode)
    oosBranch.setAccount(period.Account)
    oosBranch.setJobType(PCConstants.JOB_TYPE_POLICY_CHANGE)
    oosBranch.setStatus(PCConstants.STATUS_DRAFT)
    oosBranch.setEffectiveDate(oosEffDate != null ? oosEffDate : period.EffectiveDate)
    oosBranch.setExpirationDate(period.ExpirationDate)
    oosBranch.setBaseState(period.BaseState)
    oosBranch.setBodilyInjuryLimit(period.BodilyInjuryLimit)
    oosBranch.setPropertyDamageLimit(period.PropertyDamageLimit)
    oosBranch.setCollisionDeductible(period.CollisionDeductible)
    oosBranch.setComprehensiveDeductible(period.ComprehensiveDeductible)
    oosBranch.setTotalPremium(period.TotalPremium)

    return oosBranch
  }

  public static function mergeOOSSlice(oosBranch : PolicyPeriod, latestPeriod : PolicyPeriod) : PolicyPeriod {
    print("[GW-LOG] → OOSEndorsementEngine.mergeOOSSlice")
    if (oosBranch == null or latestPeriod == null) return latestPeriod

    latestPeriod.setBodilyInjuryLimit(oosBranch.BodilyInjuryLimit)
    latestPeriod.setPropertyDamageLimit(oosBranch.PropertyDamageLimit)
    latestPeriod.setCollisionDeductible(oosBranch.CollisionDeductible)
    latestPeriod.setComprehensiveDeductible(oosBranch.ComprehensiveDeductible)

    return latestPeriod
  }
}
