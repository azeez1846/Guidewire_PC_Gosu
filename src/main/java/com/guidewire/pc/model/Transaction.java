package com.guidewire.pc.model;

import com.guidewire.pc.orm.GosuORMSession;

import java.math.BigDecimal;
import java.util.Date;

public class Transaction {
    private Long id;
    private Cost cost;
    private String jobNumber;
    private BigDecimal amount = BigDecimal.ZERO;
    private Date postedDate;
    private String transactionType; // PremiumCharge, AdjustmentCredit, TaxCharge

    public Transaction() {
        this.id = GosuORMSession.getInstance().nextID();
        this.postedDate = new Date();
    }

    public Transaction(Cost cost, String jobNumber, BigDecimal amount, String transactionType) {
        this();
        this.cost = cost;
        this.jobNumber = jobNumber;
        this.amount = amount;
        this.transactionType = transactionType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cost getCost() { return cost; }
    public void setCost(Cost cost) { this.cost = cost; }

    public String getJobNumber() { return jobNumber; }
    public void setJobNumber(String jobNumber) { this.jobNumber = jobNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Date getPostedDate() { return postedDate; }
    public void setPostedDate(Date postedDate) { this.postedDate = postedDate; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
}
