package gw.pc.producer

import com.guidewire.pc.model.PolicyPeriod
import com.guidewire.pc.model.ProducerCode
import java.math.BigDecimal
import java.math.RoundingMode

class ProducerCommissionService {

  public static function calculateCommission(period : PolicyPeriod, producer : ProducerCode) : BigDecimal {
    print("[GW-LOG] → ProducerCommissionService.calculateCommission")
    if (period == null or producer == null) return BigDecimal.ZERO

    var prem = period.TotalPremium != null ? period.TotalPremium : BigDecimal.ZERO
    var isRenewal = "Renewal".equalsIgnoreCase(period.JobType)
    var rate = isRenewal ? producer.RenewalCommissionRate : producer.NewBusinessCommissionRate

    if (rate == null) return BigDecimal.ZERO

    var pct = rate.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP)
    var commission = prem.multiply(pct).setScale(2, RoundingMode.HALF_UP)

    return commission
  }
}
