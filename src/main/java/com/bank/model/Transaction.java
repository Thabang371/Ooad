package com.bank.model;

import java.time.LocalDateTime;

/** One entry in an account's history (deposit, withdrawal or interest). Immutable. */
public class Transaction {

    private final String accountNumber;
    private final String type;          // DEPOSIT, WITHDRAWAL or INTEREST
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;

    public Transaction(String accountNumber, String type, double amount, double balanceAfter, LocalDateTime timestamp) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("%s  %-10s BWP %,10.2f   balance BWP %,.2f",
                timestamp.toLocalDate(), type, amount, balanceAfter);
    }
}
