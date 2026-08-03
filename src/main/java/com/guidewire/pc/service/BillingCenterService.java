package com.guidewire.pc.service;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BillingCenterService {
    private static final Logger LOGGER = Logger.getLogger(BillingCenterService.class.getName());
    private static final BillingCenterService instance = new BillingCenterService();

    public static class PaymentInstallment {
        private final int installmentNumber;
        private final String dueDate;
        private BigDecimal amount;
        private String status; // Pending, Paid, PastDue

        public PaymentInstallment(int installmentNumber, String dueDate, BigDecimal amount, String status) {
            this.installmentNumber = installmentNumber;
            this.dueDate = dueDate;
            this.amount = amount;
            this.status = status;
        }

        public int getInstallmentNumber() { return installmentNumber; }
        public String getDueDate() { return dueDate; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class BillingSchedule {
        private final String policyNumber;
        private final String planName; // FullPay, FourPay, Monthly
        private final BigDecimal totalPremium;
        private final List<PaymentInstallment> installments;

        public BillingSchedule(String policyNumber, String planName, BigDecimal totalPremium, List<PaymentInstallment> installments) {
            this.policyNumber = policyNumber;
            this.planName = planName;
            this.totalPremium = totalPremium;
            this.installments = installments;
        }

        public String getPolicyNumber() { return policyNumber; }
        public String getPlanName() { return planName; }
        public BigDecimal getTotalPremium() { return totalPremium; }
        public List<PaymentInstallment> getInstallments() { return installments; }
    }

    private BillingCenterService() {}

    public static BillingCenterService getInstance() {
        return instance;
    }

    public BillingSchedule generateSchedule(PolicyPeriod period, String planName) {
        if (period == null) throw new IllegalArgumentException("Policy period cannot be null");
        BigDecimal totalPrem = period.getTotalPremium() != null ? period.getTotalPremium() : PCConstants.DEFAULT_BASE_PREMIUM;
        String pName = planName != null ? planName : "FourPay";

        List<PaymentInstallment> installments = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long baseTime = System.currentTimeMillis();

        if ("FullPay".equalsIgnoreCase(pName)) {
            installments.add(new PaymentInstallment(1, sdf.format(new Date(baseTime)), totalPrem, "Paid"));
        } else if ("FourPay".equalsIgnoreCase(pName)) {
            BigDecimal downPayment = totalPrem.multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal remaining = totalPrem.subtract(downPayment);
            BigDecimal installmentAmt = remaining.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);

            installments.add(new PaymentInstallment(1, sdf.format(new Date(baseTime)), downPayment, "Paid"));
            for (int i = 2; i <= 4; i++) {
                long futureMs = baseTime + (long) (i - 1) * 90L * 24L * 3600L * 1000L;
                installments.add(new PaymentInstallment(i, sdf.format(new Date(futureMs)), installmentAmt, "Pending"));
            }
        } else { // Monthly
            BigDecimal downPayment = totalPrem.multiply(new BigDecimal("0.1667")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal remaining = totalPrem.subtract(downPayment);
            BigDecimal monthlyAmt = remaining.divide(new BigDecimal("11"), 2, RoundingMode.HALF_UP);

            installments.add(new PaymentInstallment(1, sdf.format(new Date(baseTime)), downPayment, "Paid"));
            for (int i = 2; i <= 12; i++) {
                long futureMs = baseTime + (long) (i - 1) * 30L * 24L * 3600L * 1000L;
                installments.add(new PaymentInstallment(i, sdf.format(new Date(futureMs)), monthlyAmt, "Pending"));
            }
        }

        LOGGER.log(Level.INFO, "Generated {0} billing schedule for policy: {1}", new Object[]{pName, period.getPolicyNumber()});
        return new BillingSchedule(period.getPolicyNumber(), pName, totalPrem, installments);
    }

    public boolean applyPayment(BillingSchedule schedule, int installmentNumber, BigDecimal paymentAmount) {
        if (schedule == null || paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) return false;
        for (PaymentInstallment inst : schedule.getInstallments()) {
            if (inst.getInstallmentNumber() == installmentNumber) {
                inst.setStatus("Paid");
                LOGGER.log(Level.INFO, "Payment of ${0} applied to installment #{1} for policy: {2}",
                        new Object[]{paymentAmount, installmentNumber, schedule.getPolicyNumber()});
                return true;
            }
        }
        return false;
    }

    public boolean applyLateFees(BillingSchedule schedule, int installmentNumber, BigDecimal lateFeeAmount) {
        if (schedule == null || lateFeeAmount == null) return false;
        for (PaymentInstallment inst : schedule.getInstallments()) {
            if (inst.getInstallmentNumber() == installmentNumber && "Pending".equalsIgnoreCase(inst.getStatus())) {
                inst.setStatus("PastDue");
                inst.setAmount(inst.getAmount().add(lateFeeAmount));
                LOGGER.log(Level.INFO, "Late fee of ${0} assessed on installment #{1} for policy: {2}",
                        new Object[]{lateFeeAmount, installmentNumber, schedule.getPolicyNumber()});
                return true;
            }
        }
        return false;
    }

    public BigDecimal calculateOutstandingBalance(BillingSchedule schedule) {
        if (schedule == null || schedule.getInstallments() == null) return BigDecimal.ZERO;
        BigDecimal balance = BigDecimal.ZERO;
        for (PaymentInstallment inst : schedule.getInstallments()) {
            if (!"Paid".equalsIgnoreCase(inst.getStatus())) {
                balance = balance.add(inst.getAmount());
            }
        }
        return balance;
    }

    public String generateInvoiceHtml(PolicyPeriod period, BillingSchedule schedule) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><style>")
          .append("body { font-family: system-ui, sans-serif; background: #0f172a; color: #f8fafc; padding: 20px; }")
          .append(".invoice { max-width: 600px; margin: 0 auto; background: #1e293b; padding: 24px; border-radius: 12px; border: 1px solid #334155; }")
          .append("h2 { color: #38bdf8; font-size: 24px; }")
          .append("table { width: 100%; border-collapse: collapse; margin-top: 16px; }")
          .append("th, td { text-align: left; padding: 8px 12px; border-bottom: 1px solid #334155; }")
          .append("th { background: #0f172a; color: #94a3b8; }")
          .append("</style></head><body>")
          .append("<div class='invoice'>")
          .append("<h2>Guidewire BillingCenter Invoice</h2>")
          .append("<p><strong>Policy Number:</strong> ").append(period != null ? period.getPolicyNumber() : "N/A").append("</p>")
          .append("<p><strong>Account Holder:</strong> ").append(period != null && period.getAccount() != null ? period.getAccount().getAccountHolderName() : "Valued Customer").append("</p>")
          .append("<p><strong>Payment Plan:</strong> ").append(schedule.getPlanName()).append("</p>")
          .append("<p><strong>Total Premium:</strong> $").append(schedule.getTotalPremium()).append("</p>")
          .append("<p><strong>Outstanding Balance:</strong> $").append(calculateOutstandingBalance(schedule)).append("</p>")
          .append("<h3>Installment Schedule</h3><table>")
          .append("<tr><th>#</th><th>Due Date</th><th>Amount</th><th>Status</th></tr>");

        for (PaymentInstallment inst : schedule.getInstallments()) {
            sb.append("<tr><td>").append(inst.getInstallmentNumber())
              .append("</td><td>").append(inst.getDueDate())
              .append("</td><td>$").append(inst.getAmount())
              .append("</td><td>").append(inst.getStatus())
              .append("</td></tr>");
        }

        sb.append("</table></div></body></html>");
        return sb.toString();
    }
}
