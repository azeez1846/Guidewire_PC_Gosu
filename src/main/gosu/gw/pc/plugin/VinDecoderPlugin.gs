package gw.pc.plugin

import java.util.HashMap
import java.util.Map

class VinDecoderPlugin implements IGosuPlugin {

  override function getPluginName() : String {
    print("[GW-LOG] → VinDecoderPlugin.getPluginName")
    return "VinDecoderPlugin"
  }

  override function isAvailable() : boolean {
    print("[GW-LOG] → VinDecoderPlugin.isAvailable")
    return true
  }

  public function decodeVin(vin : String) : Map<String, Object> {
    print("[GW-LOG] → VinDecoderPlugin.decodeVin")
    var result = new HashMap<String, Object>()

    if (vin == null or vin.trim().length() != 17) {
      result.put("valid", false)
      result.put("error", "Invalid VIN length: Must be exactly 17 alphanumeric characters.")
      return result
    }

    var cleanVin = vin.trim().toUpperCase()
    result.put("vin", cleanVin)
    result.put("valid", true)

    var wmi = cleanVin.substring(0, 3)
    var vds = cleanVin.substring(3, 8)
    var yearCode = cleanVin.substring(9, 10)

    // Decode Manufacturer (WMI)
    if (wmi.startsWith("1") or wmi.startsWith("4") or wmi.startsWith("5")) {
      result.put("country", "USA")
    } else if (wmi.startsWith("2")) {
      result.put("country", "Canada")
    } else if (wmi.startsWith("J")) {
      result.put("country", "Japan")
    } else {
      result.put("country", "Global")
    }

    // Decode Year
    if (yearCode.equals("R")) result.put("modelYear", 2024)
    else if (yearCode.equals("S")) result.put("modelYear", 2025)
    else if (yearCode.equals("T")) result.put("modelYear", 2026)
    else result.put("modelYear", 2023)

    // Mock vehicle details based on VIN pattern
    if (cleanVin.contains("FORD") or wmi.equals("1FA")) {
      result.put("make", "Ford")
      result.put("model", "F-150 Commercial SuperDuty")
      result.put("bodyStyle", "Pickup Truck")
      result.put("msrp", 48500.00)
      result.put("antiTheftDevice", "Factory Alarm & GPS Tracker")
      result.put("safetyScore", 92)
    } else if (cleanVin.contains("TESLA") or wmi.equals("5YJ")) {
      result.put("make", "Tesla")
      result.put("model", "Model Y Fleet Edition")
      result.put("bodyStyle", "Electric SUV")
      result.put("msrp", 52900.00)
      result.put("antiTheftDevice", "Sentry Mode & Immobilizer")
      result.put("safetyScore", 98)
    } else {
      result.put("make", "Freightliner")
      result.put("model", "M2 106 Medium Duty")
      result.put("bodyStyle", "Box Truck")
      result.put("msrp", 75000.00)
      result.put("antiTheftDevice", "Ignition Cutoff")
      result.put("safetyScore", 88)
    }

    return result
  }
}
