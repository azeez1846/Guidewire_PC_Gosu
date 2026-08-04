package gw.pc.line.auto

import com.guidewire.pc.model.PolicyPeriod
import java.util.ArrayList
import java.util.List

class CommercialAutoValidationRules {

  public static function validateCommercialAutoLine(period : PolicyPeriod, vehicleCount : int, radius : String) : List<String> {
    print("[GW-LOG] → CommercialAutoValidationRules.validateCommercialAutoLine")
    var errors = new ArrayList<String>()
    if (period == null) return errors

    if (vehicleCount <= 0) {
      errors.add("AUTO_VALIDATION_ERROR: Commercial Auto policy must have at least 1 registered vehicle")
    }

    if (radius == null or radius.trim().length() == 0) {
      errors.add("AUTO_VALIDATION_ERROR: Radius of operation is required (Local, Intermediate, LongDistance)")
    }

    return errors
  }
}
