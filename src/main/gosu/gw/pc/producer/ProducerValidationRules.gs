package gw.pc.producer

import com.guidewire.pc.model.PolicyPeriod
import com.guidewire.pc.model.ProducerCode
import java.util.ArrayList
import java.util.List

class ProducerValidationRules {

  public static function validateProducerForPolicy(period : PolicyPeriod, producer : ProducerCode) : List<String> {
    var errors = new ArrayList<String>()
    if (period == null or producer == null) return errors

    if (!"Active".equalsIgnoreCase(producer.ProducerStatus)) {
      errors.add("PRODUCER_INACTIVE: Producer code " + producer.Code + " status is " + producer.ProducerStatus + " (Policy quotation/binding blocked)")
    }

    var state = period.BaseState != null ? period.BaseState : ""
    var licensed = producer.LicensedStates != null ? producer.LicensedStates : ""

    if (state.length() > 0 and !licensed.contains(state)) {
      errors.add("PRODUCER_NOT_LICENSED_IN_STATE: Producer " + producer.Code + " is not licensed in policy state " + state)
    }

    return errors
  }
}
