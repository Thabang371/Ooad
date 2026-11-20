package com.bank.integration;

import com.bank.controller.AccountController;
import com.bank.controller.LoginController;
import com.bank.dao.AccountDAO;
import com.bank.dao.CustomerDAO;
import com.bank.dao.Database;
import com.bank.dao.TransactionDAO;
import com.bank.model.Account;
import com.bank.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests: controllers + model + DAOs + a real (in-memory) H2 database.
 * Each test gets a fresh database loaded with the 10 sample customers.
 */
class BankingIntegrationTest {

    private LoginController login;
    private AccountController accounts;
    private AccountDAO accountDAO;

    @BeforeEach
    void freshDatabase() {
        Database.useInMemory("test-" + UUID.randomUUID());
        Database.initSchema();
        Database.loadSampleDataIfEmpty();
        login = new LoginController();
        accounts = new AccountController();
        accountDAO = new AccountDAO();
    }

    private Account find(Customer c, String number) {
        return c.getAccounts().stream()
                .filter(a -> a.getAccountNumber().equals(number))
                .findFirst().orElseThrow();
    }

    @Test
    void sampleDataHasTenCustomers() {
        assertEquals(10, new CustomerDAO().findAll().size());
    }

    @Test
    void loginLoadsCustomerAndAccounts() {
        Customer kabo = login.authenticate("kabo", "password123");
        assertEquals("Kabo Molefe", kabo.getFullName());
        assertEquals(3, kabo.getAccounts().size());
    }

    @Test
    void wrongPasswordIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> login.authenticate("kabo", "nope"));
        assertEquals("Incorrect username or password.", ex.getMessage());
    }

    @Test
    void depositIsSavedToDatabaseAndHistory() {
        Customer kabo = login.authenticate("kabo", "password123");
        Account savings = find(kabo, "SAV-1001");
        accounts.deposit(savings, "500");
        assertEquals(3000.00, accountDAO.readBalance("SAV-1001"), 0.001);
        assertEquals(1, new TransactionDAO().findByAccount("SAV-1001").size());
    }

    @Test
    void invalidAmountIsRejectedAndNothingChanges() {
        Customer kabo = login.authenticate("kabo", "password123");
        Account savings = find(kabo, "SAV-1001");
        assertThrows(IllegalArgumentException.class, () -> accounts.deposit(savings, "abc"));
        assertThrows(IllegalArgumentException.class, () -> accounts.deposit(savings, "-5"));
        assertThrows(IllegalArgumentException.class, () -> accounts.deposit(savings, "1.234"));
        assertEquals(2500.00, accountDAO.readBalance("SAV-1001"), 0.001);
    }

    @Test
    void savingsWithdrawalIsBlocked() {
        Customer kabo = login.authenticate("kabo", "password123");
        Account savings = find(kabo, "SAV-1001");
        assertThrows(IllegalArgumentException.class, () -> accounts.withdraw(savings, "100"));
        assertEquals(2500.00, accountDAO.readBalance("SAV-1001"), 0.001);
    }

    @Test
    void investmentWithdrawalIsSaved() {
        Customer kabo = login.authenticate("kabo", "password123");
        accounts.withdraw(find(kabo, "INV-2001"), "1000");
        assertEquals(9000.00, accountDAO.readBalance("INV-2001"), 0.001);
    }

    @Test
    void overdrawingIsRejected() {
        Customer kabo = login.authenticate("kabo", "password123");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> accounts.withdraw(find(kabo, "CHQ-3001"), "999999"));
        assertTrue(ex.getMessage().startsWith("Insufficient funds"));
        assertEquals(8200.00, accountDAO.readBalance("CHQ-3001"), 0.001);
    }

    @Test
    void investmentBelowMinimumCannotBeOpened() {
        Customer neo = login.authenticate("neo", "password123");
        assertThrows(IllegalArgumentException.class,
                () -> accounts.openAccount(neo, "INVESTMENT", "Tlokweng", "499", null, null));
        assertEquals(1, neo.getAccounts().size());
    }

    @Test
    void chequeAccountNeedsEmployer() {
        Customer neo = login.authenticate("neo", "password123");
        assertThrows(IllegalArgumentException.class,
                () -> accounts.openAccount(neo, "CHEQUE", "Tlokweng", "0", "", ""));
        Account chq = accounts.openAccount(neo, "CHEQUE", "Tlokweng", "100", "BURS", "Gaborone");
        assertEquals("CHQ-3005", chq.getAccountNumber());
        assertTrue(accountDAO.exists("CHQ-3005"));
    }

    @Test
    void openedAccountSurvivesNewLogin() {
        Customer neo = login.authenticate("neo", "password123");
        accounts.openAccount(neo, "INVESTMENT", "Tlokweng", "500", null, null);
        Customer again = login.authenticate("neo", "password123");
        assertEquals(2, again.getAccounts().size());
    }

    @Test
    void monthlyInterestIsPaidToInterestBearingAccountsOnly() {
        Customer kabo = login.authenticate("kabo", "password123");
        double total = accounts.payMonthlyInterest(kabo);
        // SAV-1001: 2500 * 0.0005 = 1.25 ; INV-2001: 10000 * 0.05 = 500 ; cheque gets nothing
        assertEquals(501.25, total, 0.001);
        assertEquals(2501.25, accountDAO.readBalance("SAV-1001"), 0.001);
        assertEquals(10500.00, accountDAO.readBalance("INV-2001"), 0.001);
        assertEquals(8200.00, accountDAO.readBalance("CHQ-3001"), 0.001);
    }

    @Test
    void registeredCustomerCanLogIn() {
        Customer created = login.register("Lesego", "Tau", "Plot 7, Molepolole",
                "lesego", "secret123", "secret123");
        assertEquals("C011", created.getCustomerId());
        Customer loggedIn = login.authenticate("lesego", "secret123");
        assertNotNull(loggedIn);
        assertTrue(loggedIn.getAccounts().isEmpty());
    }

    @Test
    void duplicateUsernameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> login.register("A", "B", "C", "kabo", "secret123", "secret123"));
    }

    @Test
    void deletingCustomerCascadesToAccounts() {
        new CustomerDAO().delete("C001");
        assertFalse(accountDAO.exists("SAV-1001"));
        assertFalse(accountDAO.exists("CHQ-3001"));
    }
}
