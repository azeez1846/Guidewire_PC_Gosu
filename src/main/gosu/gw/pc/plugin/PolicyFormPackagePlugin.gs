package gw.pc.plugin

import com.guidewire.pc.model.PolicyForm
import com.guidewire.pc.model.PolicyPeriod
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map

class PolicyFormPackagePlugin {

  public static function buildPolicyPacket(period : PolicyPeriod, forms : List<PolicyForm>) : Map<String, Object> {
    print("[GW-LOG] → PolicyFormPackagePlugin.buildPolicyPacket")
    var packet = new HashMap<String, Object>()
    if (period == null) return packet

    var formList = forms != null ? forms : new ArrayList<PolicyForm>()
    var mandatoryCount = 0
    var optionalCount = 0
    var toc = new ArrayList<String>()
    var rawText = "POLICY_PACKET:" + period.PolicyNumber + ":"

    for (f in formList) {
      if (f.isMandatory()) {
        mandatoryCount++
      } else {
        optionalCount++
      }
      var entry = f.FormNumber + " (" + f.EditionDate + ") - " + f.FormName
      toc.add(entry)
      rawText = rawText + f.FormNumber + ";"
    }

    var hash = computeSHA256(rawText)

    packet.put("PolicyNumber", period.PolicyNumber)
    packet.put("TotalFormsCount", formList.size())
    packet.put("MandatoryFormsCount", mandatoryCount)
    packet.put("OptionalFormsCount", optionalCount)
    packet.put("TableOfContents", toc)
    packet.put("PacketChecksum", hash)
    packet.put("Status", "Generated")

    return packet
  }

  private static function computeSHA256(input : String) : String {
    print("[GW-LOG] → PolicyFormPackagePlugin.computeSHA256")
    try {
      var digest = MessageDigest.getInstance("SHA-256")
      var hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8))
      var sb = new StringBuilder()
      for (b in hashBytes) {
        sb.append(String.format("%02x", {b & 0xff}))
      }
      return sb.toString()
    } catch (e : Exception) {
      return "HASH-ERR-001"
    }
  }
}
