package com.guidewire.pc.service;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseService {
    private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_URL = "jdbc:h2:./data/guidewire_pc;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private static final DatabaseService INSTANCE = new DatabaseService();

    private DatabaseService() {
        LOGGER.log(Level.FINE, "DatabaseService initializing...");
        initDatabase();
    }

    public static DatabaseService getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        LOGGER.log(Level.FINE, "→ DatabaseService.getConnection");
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("H2 Driver not found on classpath", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void initDatabase() {
        LOGGER.log(Level.FINE, "→ DatabaseService.initDatabase");
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            LOGGER.info("Initializing H2 Database tables for Guidewire PolicyCenter...");

            // Create ACCOUNTS table
            stmt.execute("CREATE TABLE IF NOT EXISTS ACCOUNTS (" +
                    "account_number VARCHAR(50) PRIMARY KEY, " +
                    "account_holder_name VARCHAR(255), " +
                    "account_holder_type VARCHAR(50), " +
                    "fein VARCHAR(50), " +
                    "address_line1 VARCHAR(255), " +
                    "address_line2 VARCHAR(255), " +
                    "city VARCHAR(100), " +
                    "state VARCHAR(50), " +
                    "postal_code VARCHAR(20), " +
                    "phone VARCHAR(50), " +
                    "email VARCHAR(100), " +
                    "account_status VARCHAR(50), " +
                    "producer_code VARCHAR(50), " +
                    "industry_code VARCHAR(100), " +
                    "org_type VARCHAR(100), " +
                    "create_time VARCHAR(50)" +
                    ")");

            // Create POLICY_PERIODS table
            stmt.execute("CREATE TABLE IF NOT EXISTS POLICY_PERIODS (" +
                    "job_number VARCHAR(50) PRIMARY KEY, " +
                    "policy_number VARCHAR(50), " +
                    "product_code VARCHAR(50), " +
                    "status VARCHAR(50), " +
                    "job_type VARCHAR(50), " +
                    "effective_date VARCHAR(50), " +
                    "expiration_date VARCHAR(50), " +
                    "term_months INT, " +
                    "base_state VARCHAR(50), " +
                    "producer_code VARCHAR(50), " +
                    "account_number VARCHAR(50), " +
                    "bodily_injury_limit VARCHAR(100), " +
                    "property_damage_limit VARCHAR(100), " +
                    "comprehensive_deductible VARCHAR(100), " +
                    "collision_deductible VARCHAR(100), " +
                    "base_premium DECIMAL(15,2), " +
                    "taxes_and_fees DECIMAL(15,2), " +
                    "total_premium DECIMAL(15,2), " +
                    "create_time VARCHAR(50)" +
                    ")");

            // Create ACTIVITIES table
            stmt.execute("CREATE TABLE IF NOT EXISTS ACTIVITIES (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "subject VARCHAR(255), " +
                    "description VARCHAR(1000), " +
                    "priority VARCHAR(50), " +
                    "status VARCHAR(50), " +
                    "due_date VARCHAR(50), " +
                    "assigned_user VARCHAR(50), " +
                    "related_account_id VARCHAR(50), " +
                    "related_job_number VARCHAR(50), " +
                    "create_time VARCHAR(50)" +
                    ")");

            LOGGER.info("H2 Database tables initialized successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize H2 database", e);
        }
    }
}
