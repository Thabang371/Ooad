package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Data Access Object for the transactions table (account history). */
public class TransactionDAO {

    /** Records a deposit, withdrawal or interest payment together with the balance after it. */
    public void record(Account account, String type, double amount) {
        String sql = "INSERT INTO transactions (account_number, txn_type, amount, balance_after, created_at) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, account.getAccountNumber());
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.setDouble(4, account.getBalance());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not record transaction", e);
        }
    }

    /** Most recent first. */
    public List<Transaction> findByAccount(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_id DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getString("account_number"),
                            rs.getString("txn_type"),
                            rs.getDouble("amount"),
                            rs.getDouble("balance_after"),
                            rs.getTimestamp("created_at").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load transactions", e);
        }
        return list;
    }
}
