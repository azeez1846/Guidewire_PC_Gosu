package gw.pc.reinsurance

import com.guidewire.pc.model.PolicyPeriod
import com.guidewire.pc.model.RIAgreement
import com.guidewire.pc.model.RICession
import java.math.BigDecimal
import java.util.ArrayList
import java.util.List

class ReinsuranceRIRulesEngine {

  public static function evaluateReinsuranceRules(period : PolicyPeriod, agreement : RIAgreement, grossExposure : BigDecimal) : List<String> {
    var issues = new ArrayList<String>()
    if (period == null or agreement == null) return issues

    var cession = ReinsuranceService.calculateCession(period, agreement, grossExposure)
    if (cession != null and cession.isRequiresFacultative()) {
      issues.add("UW_FACULTATIVE_REINSURANCE_REQUIRED: Policy gross limit $" + grossExposure + " exceeds treaty retention limit $" + agreement.GrossRetentionLimit)
    }

    if (cession != null and cession.CededExposure.compareTo(BigDecimal.ZERO) > 0) {
      issues.add("UW_REINSURANCE_TREATY_ATTACHED: " + agreement.AgreementType + " treaty " + agreement.AgreementNumber + " attached with ceded exposure $" + cession.CededExposure)
    }

    return issues
  }
}
