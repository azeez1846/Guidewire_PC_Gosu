package gw.pc.plugin

import java.util.HashMap
import java.util.Map
import java.util.UUID

class ESignaturePlugin implements IGosuPlugin {

  override function getPluginName() : String {
    print("[GW-LOG] → ESignaturePlugin.getPluginName")
    return "ESignaturePlugin"
  }

  override function isAvailable() : boolean {
    print("[GW-LOG] → ESignaturePlugin.isAvailable")
    return true
  }

  public function createSignatureEnvelope(jobNumber : String, signerEmail : String, documentName : String) : Map<String, Object> {
    print("[GW-LOG] → ESignaturePlugin.createSignatureEnvelope")
    var result = new HashMap<String, Object>()

    if (jobNumber == null or signerEmail == null) {
      result.put("status", "Failed")
      result.put("error", "Job number and signer email are required.")
      return result
    }

    var envelopeId = "ENV-DOCUSIGN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
    result.put("envelopeId", envelopeId)
    result.put("jobNumber", jobNumber)
    result.put("signerEmail", signerEmail)
    result.put("documentName", documentName)
    result.put("status", "Sent")
    result.put("createdTime", System.currentTimeMillis())
    result.put("signingUrl", "https://demo.docusign.net/signing/?env=" + envelopeId)

    return result
  }

  public function processSignatureCallback(envelopeId : String, event : String) : Map<String, Object> {
    print("[GW-LOG] → ESignaturePlugin.processSignatureCallback")
    var result = new HashMap<String, Object>()
    result.put("envelopeId", envelopeId)

    if ("completed".equalsIgnoreCase(event) or "signed".equalsIgnoreCase(event)) {
      result.put("status", "Signed")
      result.put("policyStatus", "Bound & Signed")
      result.put("message", "E-Signature completed successfully by insured.")
    } else {
      result.put("status", "Pending")
      result.put("message", "E-Signature envelope event logged: " + event)
    }

    return result
  }
}
