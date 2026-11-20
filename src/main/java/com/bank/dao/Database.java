package com.bank.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates JDBC connections to the H2 database and sets up the schema.
 * The app uses a file database (./data/bankdb) so data survives restarts;
 * tests switch to a fresh in-memory database with useInMemory().
 */
public final class Database {

    private static String url = "jdbc:h2:./data/bankdb";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private Database() { }

    public static void useInMemory(String name) {
        url = "jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1";
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, USER, PASSWORD);
    }

    /** Creates the tables if they do not exist yet. */
    public static void initSchema() {
        runScript("/schema.sql");
    }

    /** Inserts the 10 sample customers, but only into an empty database. */
    public static void loadSampleDataIfEmpty() {
        try (Connection c = getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM customers")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                runScript("/sample-data.sql");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not check for sample data", e);
        }
    }

    private static void runScript(String resource) {
        String sql;
        try (InputStream in = Database.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException(resource + " not found");
            }
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + resource, e);
        }
        StringBuilder cleaned = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (!line.trim().startsWith("--")) {
                cleaned.append(line).append('\n');
            }
        }
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            for (String statement : cleaned.toString().split(";")) {
                if (!statement.isBlank()) {
                    s.execute(statement);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error running " + resource, e);
        }
    }
}
