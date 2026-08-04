package gw.pc.plugin

import com.guidewire.pc.model.PolicyPeriod
import java.util.Map
import java.util.HashMap
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class PolicyDocumentPluginImpl implements IPolicyDocumentPlugin {

  override public function generatePolicyBinder(period : PolicyPeriod) : Map<String, Object> {
    print("[GW-LOG] → PolicyDocumentPluginImpl.generatePolicyBinder")
    var sb = new StringBuilder()
    sb.append("====================================================\n")
    sb.append("         GUIDEWIRE POLICYCENTER BINDER SUMMARY      \n")
    sb.append("====================================================\n")
    sb.append("Policy Number  : ").append(period?.PolicyNumber).append("\n")
    sb.append("Product Line   : ").append(period?.ProductCode).append("\n")
    sb.append("Status         : ").append(period?.Status).append("\n")
    sb.append("Effective Date : ").append(period?.EffectiveDate).append("\n")
    sb.append("Expiration Date: ").append(period?.ExpirationDate).append("\n")
    sb.append("Total Premium  : $").append(period?.TotalPremium).append("\n")
    sb.append("====================================================\n")

    return createDocumentResponse("Binder_" + period?.PolicyNumber + ".pdf", "application/pdf", sb.toString())
  }

  override public function generateDecSheet(period : PolicyPeriod) : Map<String, Object> {
    print("[GW-LOG] → PolicyDocumentPluginImpl.generateDecSheet")
    var sb = new StringBuilder()
    sb.append("====================================================\n")
    sb.append("        POLICY DECLARATION PAGE (DEC SHEET)        \n")
    sb.append("====================================================\n")
    sb.append("Named Insured  : ").append(period?.Account?.AccountHolderName != null ? period.Account.AccountHolderName : "Valued Customer").append("\n")
    sb.append("Policy Number  : ").append(period?.PolicyNumber).append("\n")
    sb.append("Bodily Injury  : ").append(period?.BodilyInjuryLimit).append("\n")
    sb.append("Prop Damage    : ").append(period?.PropertyDamageLimit).append("\n")
    sb.append("Comp Deduct    : ").append(period?.ComprehensiveDeductible).append("\n")
    sb.append("Coll Deduct    : ").append(period?.CollisionDeductible).append("\n")
    sb.append("Base Premium   : $").append(period?.BasePremium).append("\n")
    sb.append("Taxes & Fees   : $").append(period?.TaxesAndFees).append("\n")
    sb.append("Total Premium  : $").append(period?.TotalPremium).append("\n")
    sb.append("====================================================\n")

    return createDocumentResponse("DecSheet_" + period?.PolicyNumber + ".pdf", "application/pdf", sb.toString())
  }

  override public function generateCertificateOfInsurance(period : PolicyPeriod, certificateHolder : String) : Map<String, Object> {
    print("[GW-LOG] → PolicyDocumentPluginImpl.generateCertificateOfInsurance")
    var sb = new StringBuilder()
    sb.append("====================================================\n")
    sb.append("  ACORD 25 CERTIFICATE OF LIABILITY INSURANCE       \n")
    sb.append("====================================================\n")
    sb.append("Cert Holder    : ").append(certificateHolder != null ? certificateHolder : "Standard Holder").append("\n")
    sb.append("Insured        : ").append(period?.Account?.AccountHolderName != null ? period.Account.AccountHolderName : "Valued Customer").append("\n")
    sb.append("Policy Number  : ").append(period?.PolicyNumber).append("\n")
    sb.append("BI Limits      : ").append(period?.BodilyInjuryLimit).append("\n")
    sb.append("PD Limits      : ").append(period?.PropertyDamageLimit).append("\n")
    sb.append("Eff Date       : ").append(period?.EffectiveDate).append("\n")
    sb.append("Exp Date       : ").append(period?.ExpirationDate).append("\n")
    sb.append("====================================================\n")

    return createDocumentResponse("COI_" + period?.PolicyNumber + ".pdf", "application/pdf", sb.toString())
  }

  private function createDocumentResponse(filename : String, mimeType : String, content : String) : Map<String, Object> {
    print("[GW-LOG] → PolicyDocumentPluginImpl.createDocumentResponse")
    var map = new HashMap<String, Object>()
    var bytes = content.getBytes(StandardCharsets.UTF_8)
    
    var md = MessageDigest.getInstance("SHA-256")
    var digest = md.digest(bytes)
    var hexSb = new StringBuilder()
    for (b in digest) {
      hexSb.append(String.format("%02x", {b}))
    }

    map.put("FileName", filename)
    map.put("MimeType", mimeType)
    map.put("ContentBytes", bytes)
    map.put("ContentLength", bytes.length)
    map.put("SHA256Checksum", hexSb.toString())

    return map
  }
}
