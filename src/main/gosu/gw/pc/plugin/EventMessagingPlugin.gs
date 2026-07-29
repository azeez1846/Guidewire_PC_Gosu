package gw.pc.plugin

import com.guidewire.pc.model.PolicyPeriod

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
    var payload = "[Guidewire Event Message] Event=" + eventName + " | JobNumber=" + jobNum + " | PolicyNumber=" + polNum + " | Status=" + (period != null ? period.Status : "N/A")
    System.out.println(payload)
    return payload
  }
}
