package edu.univ.erp.data;

import edu.univ.erp.domain.Notification;
import edu.univ.erp.domain.User;
import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class NotificationDao {
    private static final Logger logger = LoggerFactory.getLogger(NotificationDao.class);
    private static final int GLOBAL_NOTIFICATION_ID = 1;

    // Publishes the notification message.
    public boolean publishNotification(String message, int userId, User.UserRole role) {
        String sql = "INSERT INTO notifications (id, message, posted_by_user_id, posted_by_role) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE message = VALUES(message), posted_by_user_id = VALUES(posted_by_user_id), posted_by_role = VALUES(posted_by_role), posted_at = NOW()";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, GLOBAL_NOTIFICATION_ID);
            stmt.setString(2, message);
            stmt.setInt(3, userId);
            stmt.setString(4, role.toString());

            stmt.executeUpdate();
            logger.info("Notification published by user {}: {}", userId, message);
            return true;
        } catch (SQLException e) {
            logger.error("Error publishing global notification", e);
            return false;
        }
    }

    //Retrieves the single notification record, including the username
    public Notification getLatestNotification() {
        Notification notification = null;
        String sql = "SELECT n.id, n.message, n.posted_by_user_id, n.posted_by_role, n.posted_at, ua.username " +
                "FROM notifications n " +
                "LEFT JOIN erp_auth.users_auth ua ON n.posted_by_user_id = ua.user_id " +
                "WHERE n.id = ?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, GLOBAL_NOTIFICATION_ID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    notification = new Notification();
                    notification.setId(rs.getInt("id"));
                    notification.setMessage(rs.getString("message"));
                    notification.setPostedByUserId(rs.getInt("posted_by_user_id"));
                    notification.setPostedByRole(User.UserRole.valueOf(rs.getString("posted_by_role")));
                    notification.setPostedByUsername(rs.getString("username"));

                    Timestamp postedAt = rs.getTimestamp("posted_at");
                    if (postedAt != null) {
                        notification.setPostedAt(postedAt.toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching latest notification", e);
        }
        return notification;
    }

    //Clears the notification message.
    public boolean clearNotification() {
        return publishNotification("", 0, User.UserRole.ADMIN);
    }
}