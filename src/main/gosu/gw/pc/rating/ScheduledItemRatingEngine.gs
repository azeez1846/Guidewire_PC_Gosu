package gw.pc.rating

import com.guidewire.pc.model.PolicyPeriod
import com.guidewire.pc.model.ScheduledItem
import java.math.BigDecimal
import java.math.RoundingMode

class ScheduledItemRatingEngine {

  public static function rateScheduledItems(period : PolicyPeriod) : BigDecimal {
    if (period == null or period.ScheduledItems == null or period.ScheduledItems.isEmpty()) {
      return BigDecimal.ZERO
    }

    var totalSchedulePrem = BigDecimal.ZERO

    for (item in period.ScheduledItems) {
      var categoryRate = 0.015 // 1.5% base rate
      if ("Jewelry".equalsIgnoreCase(item.Category)) {
        categoryRate = 0.020 // 2.0%
      } else if ("HeavyEquipment".equalsIgnoreCase(item.Category)) {
        categoryRate = 0.025 // 2.5%
      } else if ("FineArt".equalsIgnoreCase(item.Category)) {
        categoryRate = 0.010 // 1.0%
      }

      var val = item.StatedValue != null ? item.StatedValue.doubleValue() : 0.0
      var itemPremDouble = val * categoryRate
      var itemPrem = new BigDecimal(String.format("%.2f", {itemPremDouble})).setScale(2, RoundingMode.HALF_UP)

      item.setItemPremium(itemPrem)
      totalSchedulePrem = totalSchedulePrem.add(itemPrem)
    }

    return totalSchedulePrem
  }
}
