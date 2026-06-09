package edu.univ.erp.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

//Manages two separate Hikari Connection Pools for the Auth DB and the ERP DB.
public class DbConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DbConnectionManager.class);
    private static HikariDataSource authDataSource;
    private static HikariDataSource erpDataSource;

    public static void initialize() {
        if (authDataSource != null && erpDataSource != null) return; // Already initialized

        try {
            Properties props = new Properties();
            // Load properties
            try (InputStream input = DbConnectionManager.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (input == null) {
                    throw new RuntimeException("db.properties not found in classpath. Please create it.");
                }
                props.load(input);
            }

            HikariConfig authConfig = new HikariConfig();
            authConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC",
                    props.getProperty("auth.db.host"),
                    props.getProperty("auth.db.port"),
                    props.getProperty("auth.db.name")));
            authConfig.setUsername(props.getProperty("auth.db.user"));
            authConfig.setPassword(props.getProperty("auth.db.password"));
            authConfig.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size", "5")));
            authConfig.setConnectionTimeout(10000); // 10 seconds
            authDataSource = new HikariDataSource(authConfig);
            logger.info("Auth database connection pool initialized");

            HikariConfig erpConfig = new HikariConfig();
            erpConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC",
                    props.getProperty("erp.db.host"),
                    props.getProperty("erp.db.port"),
                    props.getProperty("erp.db.name")));
            erpConfig.setUsername(props.getProperty("erp.db.user"));
            erpConfig.setPassword(props.getProperty("erp.db.password"));
            erpConfig.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size", "5")));
            erpConfig.setConnectionTimeout(10000); // 10 seconds
            erpDataSource = new HikariDataSource(erpConfig);
            logger.info("ERP database connection pool initialized");

        } catch (Exception e) {
            logger.error("Failed to initialize database connections", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getAuthConnection() throws SQLException {
        if (authDataSource == null) initialize();
        return authDataSource.getConnection();
    }

    public static Connection getErpConnection() throws SQLException {
        if (erpDataSource == null) initialize();
        return erpDataSource.getConnection();
    }

    public static void close() {
        if (authDataSource != null && !authDataSource.isClosed()) {
            authDataSource.close();
        }
        if (erpDataSource != null && !erpDataSource.isClosed()) {
            erpDataSource.close();
        }
        logger.info("Database connections closed");
    }
}