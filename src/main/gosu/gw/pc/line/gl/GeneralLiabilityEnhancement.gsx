package gw.pc.line.gl

import com.guidewire.pc.model.PolicyPeriod
import java.math.BigDecimal

enhancement GeneralLiabilityEnhancement : PolicyPeriod {

  public property get IsGeneralLiabilityLine() : boolean {
    return "GeneralLiability".equalsIgnoreCase(this.ProductCode)
  }

  public function calculateGLPremium(exposure : BigDecimal, isClaimsMade : boolean) : BigDecimal {
    return GLRatingEngine.rateGeneralLiability(this, exposure, new BigDecimal("4.50"), isClaimsMade)
  }
}
