package gw.pc.service

import com.guidewire.pc.model.PolicyPeriod
import java.util.List
import java.util.ArrayList

class ValidationService {

  public static function validateSubmission(sub : PolicyPeriod) : List<String> {
    var errors = new ArrayList<String>()

    if (sub.Account == null) {
      errors.add("Submission must be linked to a valid Account.")
    }
    if (sub.ProductCode == null or sub.ProductCode.trim().length() == 0) {
      errors.add("Product / Policy Line must be selected.")
    }
    if (sub.EffectiveDate == null or sub.EffectiveDate.trim().length() == 0) {
      errors.add("Effective Date is required.")
    }
    if (sub.BaseState == null or sub.BaseState.trim().length() == 0) {
      errors.add("Base State is required.")
    }
    if (sub.ProducerCode == null or sub.ProducerCode.trim().length() == 0) {
      errors.add("Producer Code is required.")
    }

    return errors
  }
}
