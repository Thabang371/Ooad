package com.bank.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the model classes on their own (no database). */
class AccountModelTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer("C900", "Test", "User", "Plot 1, Gaborone", "tester");
    }

    @Test
    void depositIncreasesBalance() {
        Account acc = new SavingsAccount("SAV-9", 100, "Main", customer);
        acc.deposit(50);
        assertEquals(150, acc.getBalance(), 0.001);
    }

    @Test
    void depositOfZeroOrNegativeIsIgnored() {
        Account acc = new SavingsAccount("SAV-9", 100, "Main", customer);
        acc.deposit(0);
        acc.deposit(-20);
        assertEquals(100, acc.getBalance(), 0.001);
    }

    @Test
    void savingsAccountDoesNotAllowWithdrawals() {
        Account acc = new SavingsAccount("SAV-9", 100, "Main", customer);
        assertFalse(acc.withdraw(10));
        assertEquals(100, acc.getBalance(), 0.001);
    }

    @Test
    void investmentAccountNeedsMinimumOpeningDeposit() {
        assertThrows(IllegalArgumentException.class,
                () -> new InvestmentAccount("INV-9", 499.99, "Main", customer));
    }

    @Test
    void investmentWithdrawalChecksBalance() {
        Account acc = new InvestmentAccount("INV-9", 600, "Main", customer);
        assertTrue(acc.withdraw(100));
        assertFalse(acc.withdraw(10_000));
        assertEquals(500, acc.getBalance(), 0.001);
    }

    @Test
    void chequeAccountKeepsEmployerDetails() {
        ChequeAccount acc = new ChequeAccount("CHQ-9", 0, "Main", customer, "BURS", "Gaborone");
        assertEquals("BURS", acc.getEmployerName());
        assertEquals("Gaborone", acc.getEmployerAddress());
        assertFalse(acc.withdraw(1));
    }

    @Test
    void interestRatesArePolymorphic() {
        InterestBearing savings = new SavingsAccount("SAV-9", 10_000, "Main", customer);
        InterestBearing investment = new InvestmentAccount("INV-9", 10_000, "Main", customer);
        assertEquals(5.0, savings.calculateInterest(), 0.001);      // 0.05 %
        assertEquals(500.0, investment.calculateInterest(), 0.001); // 5 %
    }

    @Test
    void accountTypeIsReportedBySubclass() {
        assertEquals("SAVINGS", new SavingsAccount("S", 0, "B", customer).getAccountType());
        assertEquals("INVESTMENT", new InvestmentAccount("I", 500, "B", customer).getAccountType());
        assertEquals("CHEQUE", new ChequeAccount("C", 0, "B", customer, "E", "A").getAccountType());
    }

    @Test
    void passwordIsHashedAndChecked() {
        customer.setPassword("secret123");
        assertEquals(64, customer.getPasswordHash().length());
        assertTrue(customer.checkPassword("secret123"));
        assertFalse(customer.checkPassword("wrong"));
        assertThrows(IllegalArgumentException.class, () -> customer.setPassword("123"));
    }
}
