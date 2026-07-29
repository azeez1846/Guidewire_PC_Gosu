package gw.pc.job

import com.guidewire.pc.model.PolicyPeriod
import gw.pc.rating.GosuRatingEngine
import java.math.BigDecimal

class PolicyChangeJobService {

  public static function startPolicyChange(origPeriod : PolicyPeriod, editEffDateStr : String) : PolicyPeriod {
    var changePeriod = new PolicyPeriod()
    changePeriod.setJobType("PolicyChange")
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
    changePeriod.setStatus("Draft")

    if (editEffDateStr != null and editEffDateStr.trim().length() > 0) {
      changePeriod.setEffectiveDate(editEffDateStr)
    }

    GosuRatingEngine.ratePolicy(changePeriod)
    return changePeriod
  }

  public static function processEndorsement(changePeriod : PolicyPeriod, newLimit : String, newDeductible : String) : PolicyPeriod {
    if (newLimit != null and newLimit.trim().length() > 0) {
      changePeriod.setBodilyInjuryLimit(newLimit)
    }
    if (newDeductible != null and newDeductible.trim().length() > 0) {
      changePeriod.setCollisionDeductible(newDeductible)
    }

    GosuRatingEngine.ratePolicy(changePeriod)
    changePeriod.setStatus("Quoted")
    return changePeriod
  }

  public static function bindEndorsement(changePeriod : PolicyPeriod) : PolicyPeriod {
    changePeriod.setStatus("Bound")
    return changePeriod
  }
}
