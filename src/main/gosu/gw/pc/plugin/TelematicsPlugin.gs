package gw.pc.plugin

import java.math.BigDecimal
import java.util.HashMap
import java.util.Map

class TelematicsPlugin implements IGosuPlugin {

  override function getPluginName() : String {
    print("[GW-LOG] → TelematicsPlugin.getPluginName")
    return "TelematicsPlugin"
  }

  override function isAvailable() : boolean {
    print("[GW-LOG] → TelematicsPlugin.isAvailable")
    return true
  }

  public function calculateDriverSafetyScore(milesDriven : int, hardBrakingEvents : int, nightDrivingRatio : double) : Map<String, Object> {
    print("[GW-LOG] → TelematicsPlugin.calculateDriverSafetyScore")
    var result = new HashMap<String, Object>()

    var score = 100
    // Deduct points for hard braking
    score -= (hardBrakingEvents * 3)
    // Deduct points for night driving ratio (> 20%)
    if (nightDrivingRatio > 0.20) {
      score -= ((nightDrivingRatio - 0.20) * 100).intValue()
    }
    // Ensure score bounded between 0 and 100
    if (score < 0) score = 0
    if (score > 100) score = 100

    var discountPercent = 0.0
    var ratingTier = "Standard"

    if (score >= 85) {
      discountPercent = -0.15 // 15% discount
      ratingTier = "Preferred Safe Driver (15% Discount)"
    } else if (score >= 70) {
      discountPercent = -0.05 // 5% discount
      ratingTier = "Good Driver (5% Discount)"
    } else if (score < 60) {
      discountPercent = 0.10 // 10% surcharge
      ratingTier = "High Risk Driver (10% Surcharge)"
    }

    result.put("safetyScore", score)
    result.put("ratingTier", ratingTier)
    result.put("modifierFactor", 1.0 + discountPercent)
    return result
  }
}
