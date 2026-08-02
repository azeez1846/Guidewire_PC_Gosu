package gw.pc.line.umbrella

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal

enhancement CommercialUmbrellaEnhancement : PolicyPeriod {

  public property get IsCommercialUmbrellaLine() : boolean {
    return "CommercialUmbrella".equalsIgnoreCase(this.ProductCode)
  }

  public function calculateUmbrellaPremium(limit : BigDecimal, sir : BigDecimal, underlyingCount : int) : BigDecimal {
    return CURatingEngine.rateCommercialUmbrella(this, limit, sir, underlyingCount)
  }
}
