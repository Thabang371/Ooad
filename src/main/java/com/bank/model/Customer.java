package com.bank.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String customerId;
    private String firstName;
    private String surname;
    private String address;
    private List<Account> accounts;
    private String username;
    private String passwordHash;   // only a SHA-256 hash is stored, never the plain password

    public Customer(String customerId, String firstName, String surname, String address) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    /** Overloaded constructor for a customer who can log in. */
    public Customer(String customerId, String firstName, String surname, String address, String username) {
        this(customerId, firstName, surname, address);
        this.username = username;
    }

    public void addAccount(Account account) {
        if (account != null) {
            this.accounts.add(account);
        }
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public String getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getAddress() { return address; }
    public String getFullName() { return firstName + " " + surname; }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /** Stores a SHA-256 hash of the password (security non-functional requirement). */
    public void setPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        this.passwordHash = PasswordUtil.hash(plainPassword);
    }

    public boolean checkPassword(String plainPassword) {
        return plainPassword != null && passwordHash != null && passwordHash.equals(PasswordUtil.hash(plainPassword));
    }
}