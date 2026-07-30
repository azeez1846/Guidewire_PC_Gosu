package gw.pc.plugin

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.UUID

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

  public function tokenizePaymentMethod(cardNumber : String, expiryMonth : int, expiryYear : int) : Map<String, Object> {
    var result = new HashMap<String, Object>()
    if (cardNumber == null or cardNumber.trim().length() < 13) {
      result.put("success", false)
      result.put("error", "Invalid credit card number")
      return result
    }

    var last4 = cardNumber.substring(cardNumber.length() - 4)
    var token = "tok_" + UUID.randomUUID().toString().substring(0, 12).replaceAll("-", "")

    result.put("success", true)
    result.put("token", token)
    result.put("cardLast4", last4)
    result.put("expiry", expiryMonth + "/" + expiryYear)
    result.put("brand", cardNumber.startsWith("4") ? "Visa" : "MasterCard")
    return result
  }

  public function generateInstallmentSchedule(totalPremium : BigDecimal, numberOfInstallments : int) : List<Map<String, Object>> {
    var schedule = new ArrayList<Map<String, Object>>()
    if (totalPremium == null or totalPremium.compareTo(BigDecimal.ZERO) <= 0 or numberOfInstallments <= 0) {
      return schedule
    }

    var downPaymentPercent = new BigDecimal("0.20") // 20% Down payment
    var downPayment = totalPremium.multiply(downPaymentPercent).setScale(2, RoundingMode.HALF_UP)
    var remainingBalance = totalPremium.subtract(downPayment)
    var installmentCount = numberOfInstallments - 1

    var installmentAmount = BigDecimal.ZERO
    if (installmentCount > 0) {
      installmentAmount = remainingBalance.divide(new BigDecimal(installmentCount), 2, RoundingMode.HALF_UP)
    }

    // Down Payment Record
    var dpRecord = new HashMap<String, Object>()
    dpRecord.put("installmentNumber", 1)
    dpRecord.put("type", "Down Payment (20%)")
    dpRecord.put("amount", downPayment)
    dpRecord.put("dueDate", "Immediate / Bind")
    schedule.add(dpRecord)

    // Installment Records
    for (i in 1..installmentCount) {
      var rec = new HashMap<String, Object>()
      rec.put("installmentNumber", i + 1)
      rec.put("type", "Monthly Installment #" + i)
      rec.put("amount", installmentAmount)
      rec.put("dueDate", "Month +" + i)
      schedule.add(rec)
    }

    return schedule
  }
}
