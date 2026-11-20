package com.bank.controller;

import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.model.Account;
import com.bank.model.ChequeAccount;
import com.bank.model.Customer;
import com.bank.model.InterestBearing;
import com.bank.model.InvestmentAccount;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;

import java.util.List;

/**
 * Controller for everything done on the account screen: deposit, withdraw,
 * open an account, pay monthly interest and view history.
 * Each method validates the input, calls the model, then saves the change through the DAOs.
 * Validation failures are reported with IllegalArgumentException carrying a user-friendly message.
 */
public class AccountController {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public AccountController() {
        this(new AccountDAO(), new TransactionDAO());
    }

    public AccountController(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    // ---------------------------------------------------------------- deposit / withdraw

    public String deposit(Account account, String amountText) {
        requireAccount(account);
        double amount = parsePositiveAmount(amountText);
        account.deposit(amount);
        accountDAO.updateBalance(account);
        transactionDAO.record(account, "DEPOSIT", amount);
        return String.format("Deposited BWP %,.2f into %s. New balance: BWP %,.2f",
                amount, account.getAccountNumber(), account.getBalance());
    }

    public String withdraw(Account account, String amountText) {
        requireAccount(account);
        double amount = parsePositiveAmount(amountText);
        if (account instanceof SavingsAccount) {
            throw new IllegalArgumentException("Withdrawals are not allowed from a Savings account.");
        }
        boolean ok = account.withdraw(amount);          // polymorphism: each subclass applies its own rule
        if (!ok) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient funds. Available balance: BWP %,.2f", account.getBalance()));
        }
        accountDAO.updateBalance(account);
        transactionDAO.record(account, "WITHDRAWAL", amount);
        return String.format("Withdrew BWP %,.2f from %s. New balance: BWP %,.2f",
                amount, account.getAccountNumber(), account.getBalance());
    }

    // ---------------------------------------------------------------- open account

    public Account openAccount(Customer customer, String type, String branch, String amountText,
                               String employerName, String employerAddress) {
        if (customer == null) {
            throw new IllegalArgumentException("No customer is logged in.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Please choose an account type.");
        }
        if (branch == null || branch.isBlank()) {
            throw new IllegalArgumentException("Please enter a branch.");
        }
        double amount = parseAmount(amountText);

        String number = accountDAO.nextAccountNumber(type);
        Account account;
        switch (type) {
            case "SAVINGS" -> account = new SavingsAccount(number, amount, branch.trim(), customer);
            case "INVESTMENT" -> {
                if (amount < InvestmentAccount.getMinimumInitialDeposit()) {
                    throw new IllegalArgumentException(String.format(
                            "An Investment account needs an opening deposit of at least BWP %,.2f.",
                            InvestmentAccount.getMinimumInitialDeposit()));
                }
                account = new InvestmentAccount(number, amount, branch.trim(), customer);
            }
            case "CHEQUE" -> {
                if (employerName == null || employerName.isBlank()
                        || employerAddress == null || employerAddress.isBlank()) {
                    throw new IllegalArgumentException(
                            "A Cheque account needs the employer's name and address.");
                }
                account = new ChequeAccount(number, amount, branch.trim(), customer,
                        employerName.trim(), employerAddress.trim());
            }
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        }
        accountDAO.insert(account);
        customer.addAccount(account);
        if (amount > 0) {
            transactionDAO.record(account, "DEPOSIT", amount);
        }
        return account;
    }

    // ---------------------------------------------------------------- interest

    /**
     * Pays monthly interest into every interest-bearing account of the customer.
     * Cheque accounts are skipped because they do not implement InterestBearing.
     * @return total interest paid
     */
    public double payMonthlyInterest(Customer customer) {
        double total = 0;
        for (Account account : customer.getAccounts()) {
            if (account instanceof InterestBearing bearing) {
                double interest = bearing.calculateInterest();
                if (interest > 0) {
                    account.deposit(interest);
                    accountDAO.updateBalance(account);
                    transactionDAO.record(account, "INTEREST", interest);
                    total += interest;
                }
            }
        }
        return total;
    }

    // ---------------------------------------------------------------- history

    public List<Transaction> getHistory(Account account) {
        requireAccount(account);
        return transactionDAO.findByAccount(account.getAccountNumber());
    }

    // ---------------------------------------------------------------- validation helpers

    private static void requireAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Please select an account first.");
        }
    }

    private static double parsePositiveAmount(String text) {
        double amount = parseAmount(text);
        if (amount <= 0) {
            throw new IllegalArgumentException("The amount must be greater than zero.");
        }
        return amount;
    }

    private static double parseAmount(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Please enter an amount.");
        }
        java.math.BigDecimal value;
        try {
            value = new java.math.BigDecimal(text.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The amount must be a number, e.g. 250.00");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException("The amount cannot be negative.");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Use at most two decimal places.");
        }
        return value.doubleValue();
    }
}
