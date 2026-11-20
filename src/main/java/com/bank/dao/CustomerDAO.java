package com.bank.dao;

import com.bank.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the customers table: Create, Read, Update and Delete.
 * Every query uses PreparedStatement placeholders to prevent SQL injection.
 */
public class CustomerDAO {

    // ---------------------------------------------------------------- CREATE

    public void insert(Customer customer) {
        String sql = "INSERT INTO customers (customer_id, first_name, surname, address, username, password_hash) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, customer.getCustomerId());
            ps.setString(2, customer.getFirstName());
            ps.setString(3, customer.getSurname());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getUsername());
            ps.setString(6, customer.getPasswordHash());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save customer " + customer.getUsername(), e);
        }
    }

    /** Generates the next customer id, e.g. C011. */
    public String nextCustomerId() {
        try (Connection c = Database.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT MAX(CAST(SUBSTRING(customer_id, 2) AS INT)) FROM customers")) {
            rs.next();
            return String.format("C%03d", rs.getInt(1) + 1);
        } catch (SQLException e) {
            throw new DataAccessException("Could not generate a customer id", e);
        }
    }

    // ---------------------------------------------------------------- READ

    public Customer findByUsername(String username) {
        return findOne("SELECT * FROM customers WHERE username = ?", username);
    }

    public Customer findById(String customerId) {
        return findOne("SELECT * FROM customers WHERE customer_id = ?", customerId);
    }

    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM customers ORDER BY customer_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not list customers", e);
        }
        return list;
    }

    // ---------------------------------------------------------------- UPDATE

    public void updateAddress(Customer customer, String newAddress) {
        String sql = "UPDATE customers SET address = ? WHERE customer_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newAddress);
            ps.setString(2, customer.getCustomerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update customer", e);
        }
    }

    // ---------------------------------------------------------------- DELETE

    /** Deletes a customer; ON DELETE CASCADE also removes their accounts (composition). */
    public void delete(String customerId) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
            ps.setString(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete customer", e);
        }
    }

    // ---------------------------------------------------------------- helpers

    private Customer findOne(String sql, String value) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not find customer", e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer(
                rs.getString("customer_id"),
                rs.getString("first_name"),
                rs.getString("surname"),
                rs.getString("address"),
                rs.getString("username"));
        customer.setPasswordHash(rs.getString("password_hash"));
        return customer;
    }
}
