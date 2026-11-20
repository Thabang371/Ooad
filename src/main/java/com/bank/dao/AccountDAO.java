package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.ChequeAccount;
import com.bank.model.Customer;
import com.bank.model.InvestmentAccount;
import com.bank.model.SavingsAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the accounts table (CRUD).
 * The three account subclasses share one table; account_type says which class to build.
 */
public class AccountDAO {

    // ---------------------------------------------------------------- CREATE

    public void insert(Account account) {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, branch, balance, "
                   + "employer_name, employer_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, account.getAccountNumber());
            ps.setString(2, account.getCustomer().getCustomerId());
            ps.setString(3, account.getAccountType());          // polymorphic call
            ps.setString(4, account.getBranch());
            ps.setDouble(5, account.getBalance());
            if (account instanceof ChequeAccount cheque) {
                ps.setString(6, cheque.getEmployerName());
                ps.setString(7, cheque.getEmployerAddress());
            } else {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save account " + account.getAccountNumber(), e);
        }
    }

    /** Generates the next account number for a type, e.g. SAV-1006. */
    public String nextAccountNumber(String accountType) {
        String prefix = accountType.substring(0, 3);
        String sql = "SELECT MAX(CAST(SUBSTRING(account_number, 5) AS INT)) FROM accounts WHERE account_type = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountType);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int max = rs.getInt(1);
                int base = switch (accountType) {
                    case "SAVINGS" -> 1000;
                    case "INVESTMENT" -> 2000;
                    default -> 3000;
                };
                return prefix + "-" + Math.max(max + 1, base + 1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not generate an account number", e);
        }
    }

    // ---------------------------------------------------------------- READ

    /** Loads all accounts of a customer, builds the right subclass and attaches them to the customer. */
    public List<Account> loadAccountsFor(Customer customer) {
        List<Account> result = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ? ORDER BY account_number";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, customer.getCustomerId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account account = mapRow(rs, customer);
                    customer.addAccount(account);
                    result.add(account);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load accounts", e);
        }
        return result;
    }

    /** Reads only the stored balance (used by tests to prove the database really changed). */
    public double readBalance(String accountNumber) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT balance FROM accounts WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("No account " + accountNumber);
                }
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read balance", e);
        }
    }

    public boolean exists(String accountNumber) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM accounts WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not check account", e);
        }
    }

    // ---------------------------------------------------------------- UPDATE

    public void updateBalance(Account account) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance = ? WHERE account_number = ?")) {
            ps.setDouble(1, account.getBalance());
            ps.setString(2, account.getAccountNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update balance", e);
        }
    }

    // ---------------------------------------------------------------- DELETE

    public void delete(String accountNumber) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete account", e);
        }
    }

    // ---------------------------------------------------------------- helper

    private Account mapRow(ResultSet rs, Customer owner) throws SQLException {
        String number = rs.getString("account_number");
        String branch = rs.getString("branch");
        double balance = rs.getDouble("balance");
        return switch (rs.getString("account_type")) {
            case "SAVINGS" -> new SavingsAccount(number, balance, branch, owner);
            case "INVESTMENT" -> new InvestmentAccount(number, balance, branch, owner, true);
            case "CHEQUE" -> new ChequeAccount(number, balance, branch, owner,
                    rs.getString("employer_name"), rs.getString("employer_address"));
            default -> throw new IllegalStateException("Unknown account type");
        };
    }
}
