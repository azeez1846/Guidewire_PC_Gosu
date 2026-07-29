package gw.pc.model

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal
import java.text.SimpleDateFormat

enhancement PolicyPeriodEnhancement : PolicyPeriod {

  public property get FormattedTotalPremium() : String {
    if (this.TotalPremium != null) {
      return "$" + String.format("%.2f", {this.TotalPremium})
    }
    return "$0.00"
  }

  public property get IsTermActive() : boolean {
    if (this.Status != "Issued" and this.Status != "Bound") {
      return false
    }
    var today = new java.util.Date()
    if (this.PeriodStart != null and this.PeriodEnd != null) {
      return !today.before(this.PeriodStart) and !today.after(this.PeriodEnd)
    }
    return true
  }

  public function calculateUnearnedPremium(asOfDate : java.util.Date) : BigDecimal {
    if (this.TotalPremium == null or this.PeriodStart == null or this.PeriodEnd == null) {
      return BigDecimal.ZERO
    }
    if (asOfDate.before(this.PeriodStart)) {
      return this.TotalPremium
    }
    if (asOfDate.after(this.PeriodEnd)) {
      return BigDecimal.ZERO
    }
    var totalMillis = this.PeriodEnd.getTime() - this.PeriodStart.getTime()
    var remainingMillis = this.PeriodEnd.getTime() - asOfDate.getTime()
    if (totalMillis <= 0) return BigDecimal.ZERO

    var fractionRemaining = (remainingMillis as double) / (totalMillis as double)
    var unearned = this.TotalPremium.doubleValue() * fractionRemaining
    return new BigDecimal(String.format("%.2f", {unearned}))
  }

  public property get PrimaryInsuredFullAddress() : String {
    if (this.Account != null and this.Account.PrimaryAddress != null) {
      return this.Account.PrimaryAddress
    }
    return "No primary address specified"
  }
}
