package gw.pc.line.cp

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.util.ArrayList
import java.util.List

class CPValidationRules {

  public static function validateCommercialPropertyLine(period : PolicyPeriod, buildingLimit : BigDecimal, coinsurance : int) : List<String> {
    print("[GW-LOG] → CPValidationRules.validateCommercialPropertyLine")
    var errors = new ArrayList<String>()
    if (period == null) return errors

    if (buildingLimit == null or buildingLimit.compareTo(BigDecimal.ZERO) <= 0) {
      errors.add("CP_VALIDATION_ERROR: Building coverage limit must be greater than 0")
    }

    if (coinsurance < 80 or coinsurance > 100) {
      errors.add("CP_VALIDATION_ERROR: Coinsurance percentage must be between 80% and 100%")
    }

    return errors
  }
}
