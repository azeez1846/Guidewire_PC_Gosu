package gw.pc.service

import com.guidewire.pc.model.Account
import java.util.List
import java.util.ArrayList

class AccountService {

  private static var _instance : AccountService
  private var _accounts : List<Account> = new ArrayList<Account>()
  private var _accountCounter : int = 1001

  private construct() {
    initSampleAccounts()
  }

  public static property get Instance() : AccountService {
    if (_instance == null) {
      _instance = new AccountService()
    }
    return _instance
  }

  private function initSampleAccounts() {
    var acc1 = new Account()
    acc1.AccountNumber = "A0001001"
    acc1.AccountHolderName = "Acme Logistics Inc."
    acc1.AccountHolderType = "Company"
    acc1.FEIN = "12-3456789"
    acc1.AddressLine1 = "100 Innovation Way"
    acc1.City = "San Jose"
    acc1.State = "CA"
    acc1.PostalCode = "95113"
    acc1.Phone = "(408) 555-0199"
    acc1.Email = "contact@acmelogistics.com"
    acc1.AccountStatus = "Active"
    acc1.ProducerCode = "PR-10928"
    acc1.IndustryCode = "484110 - General Freight"
    acc1.OrgType = "Corporation"
    acc1.CreateTime = "2026-01-15 09:30:00"

    var acc2 = new Account()
    acc2.AccountNumber = "A0001002"
    acc2.AccountHolderName = "Johnathan Mercer"
    acc2.AccountHolderType = "Individual"
    acc2.FEIN = "XXX-XX-4891"
    acc2.AddressLine1 = "742 Evergreen Terrace"
    acc2.City = "Springfield"
    acc2.State = "OR"
    acc2.PostalCode = "97477"
    acc2.Phone = "(541) 555-0142"
    acc2.Email = "john.mercer@example.com"
    acc2.AccountStatus = "Active"
    acc2.ProducerCode = "PR-20451"
    acc2.IndustryCode = "811111 - Automotive Repair"
    acc2.OrgType = "Individual"
    acc2.CreateTime = "2026-02-01 14:15:00"

    _accounts.add(acc1)
    _accounts.add(acc2)
    _accountCounter = 1003
  }

  public function getAllAccounts() : List<Account> {
    return _accounts
  }

  public function findByNumber(accountNum : String) : Account {
    for (acc in _accounts) {
      if (acc.AccountNumber.equalsIgnoreCase(accountNum)) {
        return acc
      }
    }
    return null
  }

  public function generateAccountNumber() : String {
    var num = "A000" + _accountCounter
    _accountCounter = _accountCounter + 1
    return num
  }

  public function createAccount(newAcc : Account) : Account {
    if (newAcc.AccountNumber == null or newAcc.AccountNumber.length() == 0) {
      newAcc.AccountNumber = generateAccountNumber()
    }
    if (newAcc.AccountStatus == null) {
      newAcc.AccountStatus = "Active"
    }
    if (newAcc.CreateTime == null) {
      newAcc.CreateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())
    }
    _accounts.add(0, newAcc)
    return newAcc
  }
}
