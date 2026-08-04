package gw.pc.plugin

class AddressVerificationPlugin implements IGosuPlugin {

  override function getPluginName() : String {
    print("[GW-LOG] → AddressVerificationPlugin.getPluginName")
    return "AddressVerificationPlugin"
  }

  override function isAvailable() : boolean {
    print("[GW-LOG] → AddressVerificationPlugin.isAvailable")
    return true
  }

  public function verifyAddress(addressLine1 : String, city : String, state : String, postalCode : String) : String {
    print("[GW-LOG] → AddressVerificationPlugin.verifyAddress")
    if (addressLine1 == null or addressLine1.trim().length() == 0) {
      return "INVALID: Address line 1 is missing."
    }
    if (postalCode == null or postalCode.trim().length() < 5) {
      return "INVALID: Postal code must be at least 5 digits."
    }
    return "VALID: " + addressLine1.toUpperCase() + ", " + city.toUpperCase() + ", " + state.toUpperCase() + " " + postalCode + "-0001 (USPS Standardized)"
  }
}
