package com.bank.controller;

import com.bank.dao.AccountDAO;
import com.bank.dao.CustomerDAO;
import com.bank.model.Customer;

/**
 * Controller for logging in and registering (MVC: sits between LoginView/RegisterView and the model).
 * It validates input, checks the password hash and loads the customer's accounts.
 */
public class LoginController {

    private final CustomerDAO customerDAO;
    private final AccountDAO accountDAO;

    public LoginController() {
        this(new CustomerDAO(), new AccountDAO());
    }

    public LoginController(CustomerDAO customerDAO, AccountDAO accountDAO) {
        this.customerDAO = customerDAO;
        this.accountDAO = accountDAO;
    }

    /**
     * Returns the logged-in customer with their accounts loaded.
     * @throws IllegalArgumentException with a message the view can show when login fails
     */
    public Customer authenticate(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new IllegalArgumentException("Please enter your username and password.");
        }
        Customer customer = customerDAO.findByUsername(username.trim());
        if (customer == null || !customer.checkPassword(password)) {
            // same message for both cases so an attacker cannot tell which usernames exist
            throw new IllegalArgumentException("Incorrect username or password.");
        }
        accountDAO.loadAccountsFor(customer);
        return customer;
    }

    /** Registers a new customer and returns it. */
    public Customer register(String firstName, String surname, String address,
                             String username, String password, String confirmPassword) {
        if (isBlank(firstName) || isBlank(surname) || isBlank(address) || isBlank(username)) {
            throw new IllegalArgumentException("All fields are required.");
        }
        if (!username.trim().matches("[A-Za-z0-9_]{3,30}")) {
            throw new IllegalArgumentException("Username must be 3-30 letters, digits or underscores.");
        }
        if (password == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("The passwords do not match.");
        }
        if (customerDAO.usernameExists(username.trim())) {
            throw new IllegalArgumentException("That username is already taken.");
        }
        Customer customer = new Customer(customerDAO.nextCustomerId(), firstName.trim(), surname.trim(),
                address.trim(), username.trim());
        customer.setPassword(password);   // throws if shorter than 6 characters
        customerDAO.insert(customer);
        return customer;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
