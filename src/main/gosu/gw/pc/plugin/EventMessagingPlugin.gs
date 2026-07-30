package gw.pc.plugin

import com.guidewire.pc.model.PolicyPeriod
import java.lang.Thread

class EventMessagingPlugin implements IGosuPlugin {

  override function getPluginName() : String {
    return "EventMessagingPlugin"
  }

  override function isAvailable() : boolean {
    return true
  }

  public function sendPolicyEvent(eventName : String, period : PolicyPeriod) : String {
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
