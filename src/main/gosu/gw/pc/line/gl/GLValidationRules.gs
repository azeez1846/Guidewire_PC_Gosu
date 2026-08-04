package gw.pc.line.gl

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.util.ArrayList
import java.util.List

class GLValidationRules {

  public static function validateGeneralLiabilityLine(period : PolicyPeriod, exposureAmount : BigDecimal, coverageForm : String) : List<String> {
    print("[GW-LOG] → GLValidationRules.validateGeneralLiabilityLine")
    var errors = new ArrayList<String>()
    if (period == null) return errors

    if (exposureAmount == null or exposureAmount.compareTo(BigDecimal.ZERO) <= 0) {
      errors.add("GL_VALIDATION_ERROR: Exposure amount (gross sales/payroll) must be greater than 0")
    }

    if (coverageForm == null or (!"Occurrence".equalsIgnoreCase(coverageForm) and !"ClaimsMade".equalsIgnoreCase(coverageForm))) {
      errors.add("GL_VALIDATION_ERROR: Coverage form must be either Occurrence or ClaimsMade")
    }

    return errors
  }
}
