package gw.pc.line.wc

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.util.ArrayList
import java.util.List

class WCValidationRules {

  public static function validateWorkersCompLine(period : PolicyPeriod, estimatedPayroll : BigDecimal, expMod : BigDecimal) : List<String> {
    print("[GW-LOG] → WCValidationRules.validateWorkersCompLine")
    var errors = new ArrayList<String>()
    if (period == null) return errors

    if (estimatedPayroll == null or estimatedPayroll.compareTo(BigDecimal.ZERO) <= 0) {
      errors.add("WC_VALIDATION_ERROR: Estimated annual payroll must be greater than 0")
    }

    if (expMod != null and (expMod.compareTo(new BigDecimal("0.400")) < 0 or expMod.compareTo(new BigDecimal("2.500")) > 0)) {
      errors.add("WC_VALIDATION_ERROR: Experience Modifier " + expMod + " is outside acceptable NCCI range (0.400 - 2.500)")
    }

    if (period.BaseState == null or period.BaseState.trim().length() == 0) {
      errors.add("WC_VALIDATION_ERROR: Workers Compensation primary jurisdiction base state is required")
    }

    return errors
  }
}
