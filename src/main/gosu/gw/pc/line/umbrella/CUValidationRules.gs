package gw.pc.line.umbrella

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.util.ArrayList
import java.util.List

class CUValidationRules {

  public static function validateCommercialUmbrellaLine(period : PolicyPeriod, limitAmount : BigDecimal, underlyingPolicyCount : int) : List<String> {
    var errors = new ArrayList<String>()
    if (period == null) return errors

    if (limitAmount == null or limitAmount.compareTo(new BigDecimal("1000000.00")) < 0) {
      errors.add("UMBRELLA_VALIDATION_ERROR: Commercial Umbrella occurrence limit must be at least $1,000,000")
    }

    if (underlyingPolicyCount <= 0) {
      errors.add("UMBRELLA_VALIDATION_ERROR: Commercial Umbrella must have at least 1 schedule underlying policy (GL, Auto, or EL)")
    }

    return errors
  }
}
