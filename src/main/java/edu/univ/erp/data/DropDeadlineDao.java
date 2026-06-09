package edu.univ.erp.data;

import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDate;

public class DropDeadlineDao {
    private static final Logger logger = LoggerFactory.getLogger(DropDeadlineDao.class);

    //Get drop deadline for semester/year
    public LocalDate getDropDeadline(int semester, int year) {
        String sql = "SELECT deadline_date FROM drop_deadlines WHERE semester = ? AND year = ?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, semester);
            stmt.setInt(2, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDate("deadline_date").toLocalDate();
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching drop deadline", e);
        }
        return null;
    }
    // Set drop deadline
    public void setDropDeadline(int semester, int year, LocalDate deadline) throws SQLException {
        String sql = "INSERT INTO drop_deadlines (semester, year, deadline_date) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE deadline_date = ?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, semester);
            stmt.setInt(2, year);
            stmt.setDate(3, java.sql.Date.valueOf(deadline));
            stmt.setDate(4, java.sql.Date.valueOf(deadline));
            stmt.executeUpdate();
            logger.info("Drop deadline set for semester {} year {}: {}", semester, year, deadline);
        }
    }
}