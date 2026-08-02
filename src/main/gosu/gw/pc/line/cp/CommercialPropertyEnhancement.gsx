package gw.pc.line.cp

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal

enhancement CommercialPropertyEnhancement : PolicyPeriod {

  public property get IsCommercialPropertyLine() : boolean {
    return "CommercialProperty".equalsIgnoreCase(this.ProductCode)
  }

  public function calculateCPPremium(buildingLimit : BigDecimal, bppLimit : BigDecimal, protectionClass : String) : BigDecimal {
    return CPRatingEngine.rateCommercialProperty(this, buildingLimit, bppLimit, protectionClass)
  }
}
