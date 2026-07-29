package gw.pc.service

import com.guidewire.pc.model.PolicyPeriod
import gw.pc.validation.ValidationLevel
import java.util.List
import java.util.ArrayList

class ValidationService {

  public static function validateSubmission(sub : PolicyPeriod) : List<String> {
    return validateAtLevel(sub, ValidationLevel.Default)
  }

  public static function validateAtLevel(sub : PolicyPeriod, level : ValidationLevel) : List<String> {
    var errors = new ArrayList<String>()

    if (sub == null) {
      errors.add("PolicyPeriod object cannot be null.")
      return errors
    }

    // Level: Default (Draft level validation)
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

    if (level == ValidationLevel.Default) {
      return errors
    }

    // Level: Quoteable
    if (sub.ProducerCode == null or sub.ProducerCode.trim().length() == 0) {
      errors.add("Producer Code is required prior to quoting.")
    }
    if (sub.TermMonths <= 0) {
      errors.add("Term Months must be greater than zero prior to quoting.")
    }

    if (level == ValidationLevel.Quoteable) {
      return errors
    }

    // Level: Bindable
    if (sub.BasePremium == null or sub.BasePremium.doubleValue() <= 0) {
      errors.add("Policy must be rated with a non-zero premium prior to binding.")
    }
    if (sub.BodilyInjuryLimit == null or sub.BodilyInjuryLimit.trim().length() == 0) {
      errors.add("Bodily Injury Limit must be selected prior to binding.")
    }

    if (level == ValidationLevel.Bindable) {
      return errors
    }

    // Level: Issuable
    if (sub.PolicyNumber == null or sub.PolicyNumber.trim().length() == 0) {
      // Automatic policy number assignment will occur if null, but if provided must match format
    }
    if (sub.Status != "Bound" and sub.Status != "Issued") {
      errors.add("Policy Period status must be 'Bound' before issuance.")
    }

    return errors
  }
}
