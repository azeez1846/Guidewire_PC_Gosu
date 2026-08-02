package gw.pc.line.wc

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal

enhancement WorkersCompEnhancement : PolicyPeriod {

  public property get IsWorkersCompLine() : boolean {
    return "WorkersComp".equalsIgnoreCase(this.ProductCode)
  }

  public function calculateEstimatedWcPremium(payroll : BigDecimal, emod : BigDecimal) : BigDecimal {
    return WCRatingEngine.rateWorkersComp(this, payroll, emod, new BigDecimal("2.50"))
  }
}
