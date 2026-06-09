package edu.univ.erp.data;

import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDao {
    private static final Logger logger = LoggerFactory.getLogger(SettingsDao.class);

    //Check if maintenance mode is ON
    public boolean isMaintenanceOn() {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_on'";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return "true".equalsIgnoreCase(rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            logger.error("Error checking maintenance mode", e);
        }
        return false;
    }

    // Toggle maintenance mode
    public void toggleMaintenanceMode(boolean on) {
        String sql = "UPDATE settings SET setting_value = ? WHERE setting_key = 'maintenance_on'";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, on ? "true" : "false");
            stmt.executeUpdate();
            logger.info("Maintenance mode set to: {}", on);
        } catch (SQLException e) {
            logger.error("Error toggling maintenance mode", e);
        }
    }
}