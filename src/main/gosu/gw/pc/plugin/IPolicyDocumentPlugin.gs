package gw.pc.plugin

import com.guidewire.pc.model.PolicyPeriod
import java.util.Map

interface IPolicyDocumentPlugin extends IGosuPlugin {
  public function generatePolicyBinder(period : PolicyPeriod) : Map<String, Object>
  public function generateDecSheet(period : PolicyPeriod) : Map<String, Object>
  public function generateCertificateOfInsurance(period : PolicyPeriod, certificateHolder : String) : Map<String, Object>
}
