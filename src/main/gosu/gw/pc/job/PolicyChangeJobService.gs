package gw.pc.job

import com.guidewire.pc.model.PolicyPeriod
import gw.pc.config.PCConstants
import gw.pc.rating.GosuRatingEngine
import java.math.BigDecimal

class PolicyChangeJobService {

  public static function startPolicyChange(origPeriod : PolicyPeriod, editEffDateStr : String) : PolicyPeriod {
    print("[GW-LOG] → PolicyChangeJobService.startPolicyChange")
    if (origPeriod == null) return null

    var changePeriod = new PolicyPeriod()
    changePeriod.setJobType(PCConstants.JOB_TYPE_POLICY_CHANGE)
    changePeriod.setJobNumber("C000" + (System.currentTimeMillis() % 10000))
    changePeriod.setPolicyNumber(origPeriod.PolicyNumber)
    changePeriod.setProductCode(origPeriod.ProductCode)
    changePeriod.setAccount(origPeriod.Account)
    changePeriod.setBaseState(origPeriod.BaseState)
    changePeriod.setProducerCode(origPeriod.ProducerCode)
    changePeriod.setEffectiveDate(origPeriod.EffectiveDate)
    changePeriod.setExpirationDate(origPeriod.ExpirationDate)
    changePeriod.setTermMonths(origPeriod.TermMonths)
    changePeriod.setBodilyInjuryLimit(origPeriod.BodilyInjuryLimit)
    changePeriod.setPropertyDamageLimit(origPeriod.PropertyDamageLimit)
    changePeriod.setComprehensiveDeductible(origPeriod.ComprehensiveDeductible)
    changePeriod.setCollisionDeductible(origPeriod.CollisionDeductible)
    changePeriod.setStatus(PCConstants.STATUS_DRAFT)

    if (editEffDateStr != null and editEffDateStr.trim().length() > 0) {
      changePeriod.setEffectiveDate(editEffDateStr)
    }

    GosuRatingEngine.ratePolicy(changePeriod)
    return changePeriod
  }

  public static function processEndorsement(changePeriod : PolicyPeriod, newLimit : String, newDeductible : String) : PolicyPeriod {
    print("[GW-LOG] → PolicyChangeJobService.processEndorsement")
    if (changePeriod == null) return null

    if (newLimit != null and newLimit.trim().length() > 0) {
      changePeriod.setBodilyInjuryLimit(newLimit)
    }
    if (newDeductible != null and newDeductible.trim().length() > 0) {
      changePeriod.setCollisionDeductible(newDeductible)
    }

    GosuRatingEngine.ratePolicy(changePeriod)
    changePeriod.setStatus(PCConstants.STATUS_QUOTED)
    return changePeriod
  }

  public static function bindEndorsement(changePeriod : PolicyPeriod) : PolicyPeriod {
    print("[GW-LOG] → PolicyChangeJobService.bindEndorsement")
    if (changePeriod != null) {
      changePeriod.setStatus(PCConstants.STATUS_BOUND)
    }
    return changePeriod
  }
}
