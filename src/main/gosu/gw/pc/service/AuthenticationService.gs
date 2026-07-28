package gw.pc.service

class AuthenticationService {

  public static function authenticate(username : String, password : String) : boolean {
    if (username != null and password != null) {
      return username.trim().equalsIgnoreCase("su") and password.trim().equals("gw")
    }
    return false
  }
}
