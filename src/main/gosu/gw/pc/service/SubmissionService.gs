package gw.pc.service

import com.guidewire.pc.model.PolicyPeriod
import com.guidewire.pc.model.Account
import gw.pc.config.PCConstants
import java.util.List
import java.util.ArrayList

class SubmissionService {

  private static var _instance : SubmissionService
  private var _submissions : List<PolicyPeriod> = new ArrayList<PolicyPeriod>()
  private var _jobCounter : int = 5001

  private construct() {
    initSampleSubmissions()
  }

  public static property get Instance() : SubmissionService {
    if (_instance == null) {
      _instance = new SubmissionService()
    }
    return _instance
  }

  private function initSampleSubmissions() {
    print("[GW-LOG] → SubmissionService.initSampleSubmissions")
    var acc = AccountService.Instance.findByNumber("A0001001")
    if (acc != null) {
      var sub1 = new PolicyPeriod()
      sub1.JobNumber = "S0005001"
      sub1.PolicyNumber = "POL-849102"
      sub1.ProductCode = PCConstants.PRODUCT_COMMERCIAL_AUTO
      sub1.Status = PCConstants.STATUS_ISSUED
      sub1.EffectiveDate = "2026-03-01"
      sub1.ExpirationDate = "2027-03-01"
      sub1.TermMonths = 12
      sub1.BaseState = "CA"
      sub1.ProducerCode = "PR-10928"
      sub1.Account = acc
      sub1.BodilyInjuryLimit = "$500k/$500k"
      sub1.PropertyDamageLimit = "$250k"
      sub1.ComprehensiveDeductible = "$500"
      sub1.CollisionDeductible = "$1000"
      sub1.BasePremium = new java.math.BigDecimal("2675.00")
      sub1.TaxesAndFees = new java.math.BigDecimal("214.00")
      sub1.TotalPremium = new java.math.BigDecimal("2889.00")
      sub1.CreateTime = "2026-02-10 11:20:00"
      _submissions.add(sub1)

      var sub2 = new PolicyPeriod()
      sub2.JobNumber = "S0005002"
      sub2.PolicyNumber = null
      sub2.ProductCode = PCConstants.PRODUCT_GENERAL_LIABILITY
      sub2.Status = PCConstants.STATUS_QUOTED
      sub2.EffectiveDate = "2026-04-01"
      sub2.ExpirationDate = "2027-04-01"
      sub2.TermMonths = 12
      sub2.BaseState = "CA"
      sub2.ProducerCode = "PR-10928"
      sub2.Account = acc
      sub2.BodilyInjuryLimit = "$1M/$1M"
      sub2.PropertyDamageLimit = "$500k"
      sub2.ComprehensiveDeductible = "$1000"
      sub2.CollisionDeductible = "$1000"
      sub2.BasePremium = new java.math.BigDecimal("3920.00")
      sub2.TaxesAndFees = new java.math.BigDecimal("313.60")
      sub2.TotalPremium = new java.math.BigDecimal("4233.60")
      sub2.CreateTime = "2026-02-20 16:45:00"
      _submissions.add(sub2)
    }
    _jobCounter = 5003
  }

  public function getAllSubmissions() : List<PolicyPeriod> {
    print("[GW-LOG] → SubmissionService.getAllSubmissions")
    return _submissions
  }

  public function findByJobNumber(jobNum : String) : PolicyPeriod {
    print("[GW-LOG] → SubmissionService.findByJobNumber")
    if (jobNum == null) return null
    for (sub in _submissions) {
      if (sub?.JobNumber != null and sub.JobNumber.equalsIgnoreCase(jobNum)) {
        return sub
      }
    }
    return null
  }

  public function findSubmissionsForAccount(accountNum : String) : List<PolicyPeriod> {
    print("[GW-LOG] → SubmissionService.findSubmissionsForAccount")
    var result = new ArrayList<PolicyPeriod>()
    if (accountNum == null) return result
    for (sub in _submissions) {
      if (sub?.Account?.AccountNumber != null and sub.Account.AccountNumber.equalsIgnoreCase(accountNum)) {
        result.add(sub)
      }
    }
    return result
  }

  public function generateJobNumber() : String {
    print("[GW-LOG] → SubmissionService.generateJobNumber")
    var num = "S000" + _jobCounter
    _jobCounter = _jobCounter + 1
    return num
  }

  public function createSubmission(sub : PolicyPeriod) : PolicyPeriod {
    print("[GW-LOG] → SubmissionService.createSubmission")
    if (sub?.JobNumber == null or sub.JobNumber.length() == 0) {
      sub.JobNumber = generateJobNumber()
    }
    if (sub?.Status == null) {
      sub.Status = PCConstants.STATUS_DRAFT
    }
    if (sub?.CreateTime == null) {
      sub.CreateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())
    }
    _submissions.add(0, sub)
    return sub
  }

  public function copySubmission(origJobNum : String) : PolicyPeriod {
    print("[GW-LOG] → SubmissionService.copySubmission")
    var orig = findByJobNumber(origJobNum)
    if (orig == null) return null
    var newJobNum = generateJobNumber()
    var copy = orig.copySubmissionBranch(newJobNum)
    createSubmission(copy)
    return copy
  }
}
