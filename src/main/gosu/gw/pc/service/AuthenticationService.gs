package gw.pc.service

class AuthenticationService {

  public static function authenticate(username : String, password : String) : boolean {
    print("[GW-LOG] → AuthenticationService.authenticate")
    if (username != null and password != null) {
      var isUserValid = com.guidewire.pc.security.SecurityUtils.constantTimeEquals(username.trim().toLowerCase(), "su")
      var isPassValid = com.guidewire.pc.security.SecurityUtils.constantTimeEquals(password.trim(), "gw")
      return isUserValid and isPassValid
    }
    return false
  }
}
