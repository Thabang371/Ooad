package com.bank.dao;

/**
 * Wraps SQLException so controllers and views never deal with JDBC types
 * (keeps database details inside the DAO layer).
 */
public class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
