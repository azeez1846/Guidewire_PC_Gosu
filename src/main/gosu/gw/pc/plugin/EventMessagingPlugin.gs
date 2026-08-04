package gw.pc.plugin

import com.guidewire.pc.model.PolicyPeriod
import java.lang.Thread

class EventMessagingPlugin implements IGosuPlugin {

  override function getPluginName() : String {
    print("[GW-LOG] → EventMessagingPlugin.getPluginName")
    return "EventMessagingPlugin"
  }

  override function isAvailable() : boolean {
    print("[GW-LOG] → EventMessagingPlugin.isAvailable")
    return true
  }

  public function sendPolicyEvent(eventName : String, period : PolicyPeriod) : String {
    print("[GW-LOG] → EventMessagingPlugin.sendPolicyEvent")
    var jobNum = period != null ? period.JobNumber : "UNKNOWN"
    var polNum = period != null ? period.PolicyNumber : "UNASSIGNED"
    var payload = "[Guidewire Virtual Thread Event Message] Event=" + eventName + " | JobNumber=" + jobNum + " | PolicyNumber=" + polNum + " | Status=" + (period != null ? period.Status : "N/A")
    
    // Dispatch on Java 23 Virtual Thread asynchronously
    Thread.ofVirtual().name("gw-virtual-event-sender").start(\ -> {
      System.out.println(payload)
    })

    return payload
  }
}
