package gw.pc.model

import com.guidewire.pc.model.Coverage
import java.math.BigDecimal

enhancement CoverageEnhancement : Coverage {

  public property get FormattedLimit() : String {
    if (this.DirectLimit != null) {
      return "$" + String.format("%.2f", {this.DirectLimit})
    }
    return "$0.00"
  }

  public property get FormattedDeductible() : String {
    if (this.Deductible != null) {
      return "$" + String.format("%.2f", {this.Deductible})
    }
    return "$0.00"
  }

  public function isDeductibleValid() : boolean {
    if (this.Deductible == null) return false
    return this.Deductible.compareTo(BigDecimal.ZERO) >= 0
  }
}
