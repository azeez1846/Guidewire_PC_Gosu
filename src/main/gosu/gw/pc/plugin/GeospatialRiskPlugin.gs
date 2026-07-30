package gw.pc.plugin

import java.util.HashMap
import java.util.Map

class GeospatialRiskPlugin implements IGosuPlugin {

  override function getPluginName() : String {
    return "GeospatialRiskPlugin"
  }

  override function isAvailable() : boolean {
    return true
  }

  public function evaluateLocationRisk(city : String, state : String, postalCode : String) : Map<String, Object> {
    var result = new HashMap<String, Object>()
    result.put("city", city)
    result.put("state", state)
    result.put("postalCode", postalCode)

    if (state == null or state.trim().isEmpty()) {
      result.put("valid", false)
      result.put("error", "State parameter is required")
      return result
    }

    var cleanState = state.trim().toUpperCase()

    if (cleanState.equals("CA")) {
      result.put("wildfireScore", 85) // High Wildfire risk
      result.put("floodZone", "Zone X")
      result.put("distanceToCoastMiles", 12.5)
      result.put("hazardHoldRequired", true)
      result.put("hazardReason", "High Wildfire Risk Zone (Score 85/100)")
    } else if (cleanState.equals("FL")) {
      result.put("wildfireScore", 30)
      result.put("floodZone", "Zone A") // High Flood risk
      result.put("distanceToCoastMiles", 2.1)
      result.put("hazardHoldRequired", true)
      result.put("hazardReason", "Coastal Special Flood Hazard Area (Zone A)")
    } else {
      result.put("wildfireScore", 15)
      result.put("floodZone", "Zone X")
      result.put("distanceToCoastMiles", 120.0)
      result.put("hazardHoldRequired", false)
      result.put("hazardReason", "Low Hazard Location")
    }

    result.put("valid", true)
    return result
  }
}
