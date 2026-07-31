package gw.pc.submission

import com.guidewire.pc.model.PolicyPeriod
import gw.pc.config.PCConstants
import java.math.BigDecimal

enhancement SubmissionEnhancement : PolicyPeriod {

  public property get FormattedStatus() : String {
    if (this.Status == PCConstants.STATUS_DRAFT) {
      return PCConstants.STATUS_DRAFT
    } else if (this.Status == PCConstants.STATUS_QUOTED) {
      return PCConstants.STATUS_QUOTED
    } else if (this.Status == PCConstants.STATUS_BOUND) {
      return PCConstants.STATUS_BOUND
    } else if (this.Status == PCConstants.STATUS_ISSUED) {
      return "In Force (Issued)"
    }
    return this.Status
  }

  public function canQuote() : boolean {
    return this.Status == PCConstants.STATUS_DRAFT
  }

  public function canBind() : boolean {
    return this.Status == PCConstants.STATUS_QUOTED
  }

  public function canIssue() : boolean {
    return this.Status == PCConstants.STATUS_BOUND
  }

  public function calculatePremium() : BigDecimal {
    var baseRate = 500.0
    
    if (this.ProductCode == PCConstants.PRODUCT_PERSONAL_AUTO) {
      baseRate = 650.0
    } else if (this.ProductCode == PCConstants.PRODUCT_COMMERCIAL_AUTO) {
      baseRate = 1250.0
    } else if (this.ProductCode == PCConstants.PRODUCT_COMMERCIAL_PROPERTY) {
      baseRate = 2100.0
    } else if (this.ProductCode == PCConstants.PRODUCT_GENERAL_LIABILITY) {
      baseRate = 1800.0
    }

    if (this.TermMonths == 12) {
      baseRate = baseRate * 1.9
    }

    if (this.BodilyInjuryLimit == "$500k/$500k") {
      baseRate = baseRate + 250.0
    } else if (this.BodilyInjuryLimit == "$1M/$1M") {
      baseRate = baseRate + 500.0
    }

    if (this.PropertyDamageLimit == "$250k") {
      baseRate = baseRate + 150.0
    } else if (this.PropertyDamageLimit == "$500k") {
      baseRate = baseRate + 300.0
    }

    var taxes = baseRate * 0.08
    this.BasePremium = new BigDecimal(baseRate).setScale(2, java.math.RoundingMode.HALF_UP)
    this.TaxesAndFees = new BigDecimal(taxes).setScale(2, java.math.RoundingMode.HALF_UP)
    this.TotalPremium = this.BasePremium.add(this.TaxesAndFees)
    
    return this.TotalPremium
  }

  public function quote() {
    this.calculatePremium()
    this.Status = PCConstants.STATUS_QUOTED
  }

  public function bindAndIssue() {
    if (this.PolicyNumber == null or this.PolicyNumber.trim().length() == 0) {
      var randomId = (int)(java.lang.Math.random() * 900000) + 100000
      this.PolicyNumber = "POL-" + randomId
    }
    this.Status = PCConstants.STATUS_ISSUED
  }

  public function createPolicyChange(editEffectiveDateStr : String, newJobNum : String) : PolicyPeriod {
    var sdf = new java.text.SimpleDateFormat("yyyy-MM-dd")
    var editEffDate = sdf.parse(editEffectiveDateStr)
    return this.createPolicyChangeBranch(editEffDate, newJobNum)
  }

  public function cancelPolicy(cancelEffectiveDateStr : String, newJobNum : String) : PolicyPeriod {
    var sdf = new java.text.SimpleDateFormat("yyyy-MM-dd")
    var cancelEffDate = sdf.parse(cancelEffectiveDateStr)
    return this.createCancellationBranch(cancelEffDate, newJobNum)
  }
}
