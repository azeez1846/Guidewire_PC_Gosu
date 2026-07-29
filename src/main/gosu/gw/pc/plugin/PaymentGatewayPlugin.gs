package gw.pc.plugin

import java.math.BigDecimal

class PaymentGatewayPlugin implements IGosuPlugin {

  override function getPluginName() : String {
    return "PaymentGatewayPlugin"
  }

  override function isAvailable() : boolean {
    return true
  }

  public function processDownPayment(accountNumber : String, amount : BigDecimal, paymentMethod : String) : String {
    if (amount == null or amount.compareTo(BigDecimal.ZERO) <= 0) {
      return "FAILED: Down payment amount must be greater than $0.00."
    }
    var txnId = "PAY-TXN-" + System.currentTimeMillis()
    return "SUCCESS: Processed payment of $" + String.format("%.2f", {amount}) + " via " + paymentMethod + ". Transaction ID: " + txnId
  }
}
