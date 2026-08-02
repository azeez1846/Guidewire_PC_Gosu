package gw.pc.line.auto

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal

enhancement CommercialAutoEnhancement : PolicyPeriod {

  public property get IsCommercialAutoLine() : boolean {
    return "CommercialAuto".equalsIgnoreCase(this.ProductCode)
  }

  public function calculateCommercialAutoPremium(vehicles : int, isFleet : boolean, radius : String) : BigDecimal {
    return CommercialAutoRatingEngine.rateCommercialAuto(this, vehicles, isFleet, radius)
  }
}
