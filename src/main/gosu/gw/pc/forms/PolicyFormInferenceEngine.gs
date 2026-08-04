package gw.pc.forms

import com.guidewire.pc.model.PolicyForm
import com.guidewire.pc.model.PolicyPeriod
import java.util.ArrayList
import java.util.List

class PolicyFormInferenceEngine {

  public static function inferPolicyForms(period : PolicyPeriod) : List<PolicyForm> {
    print("[GW-LOG] → PolicyFormInferenceEngine.inferPolicyForms")
    var forms = new ArrayList<PolicyForm>()
    if (period == null) return forms

    var state = period.BaseState != null ? period.BaseState : "US"
    var prod = period.ProductCode != null ? period.ProductCode : "Generic"

    // 1. Mandatory Common Forms
    forms.add(new PolicyForm("IL 00 17", "Common Policy Conditions", "01 20", true, state, "Mandatory ISO Common Conditions"))
    forms.add(new PolicyForm("IL 00 21", "Nuclear Energy Liability Exclusion", "01 15", true, state, "Mandatory Nuclear Liability Exclusion"))

    // 2. Line of Business Coverage Forms
    if ("CommercialAuto".equalsIgnoreCase(prod)) {
      forms.add(new PolicyForm("CA 00 01", "Business Auto Coverage Form", "10 20", true, state, "Standard Commercial Auto Coverage Form"))
    } else if ("CommercialProperty".equalsIgnoreCase(prod)) {
      forms.add(new PolicyForm("CP 00 10", "Building and Personal Property Coverage Form", "06 18", true, state, "Standard Commercial Property Form"))
    } else if ("PersonalAuto".equalsIgnoreCase(prod)) {
      forms.add(new PolicyForm("PP 00 01", "Personal Auto Policy Form", "09 18", true, state, "Standard Personal Auto Form"))
    } else if ("GeneralLiability".equalsIgnoreCase(prod)) {
      forms.add(new PolicyForm("CG 00 01", "Commercial General Liability Coverage Form", "04 13", true, state, "Standard General Liability Form"))
    }

    // 3. State Mandatory Cancellation Notice Endorsements
    if ("FL".equalsIgnoreCase(state)) {
      forms.add(new PolicyForm("IL 01 02", "Florida Changes - Cancellation and Nonrenewal", "02 21", true, "FL", "Florida Statutory Notice"))
    } else if ("CA".equalsIgnoreCase(state)) {
      forms.add(new PolicyForm("IL 01 04", "California Changes - Cancellation and Nonrenewal", "07 20", true, "CA", "California Statutory Notice"))
    } else if ("TX".equalsIgnoreCase(state)) {
      forms.add(new PolicyForm("IL 01 88", "Texas Changes - Cancellation and Nonrenewal", "01 22", true, "TX", "Texas Statutory Notice"))
    }

    // 4. TRIA & High Exposure Optional Disclosure Forms
    if (period.TotalPremium != null and period.TotalPremium.doubleValue() >= 10000.0) {
      forms.add(new PolicyForm("IL 09 85", "Disclosure Pursuant to Terrorism Risk Insurance Act", "01 21", false, state, "TRIA Disclosure Endorsement"))
    }

    return forms
  }
}
