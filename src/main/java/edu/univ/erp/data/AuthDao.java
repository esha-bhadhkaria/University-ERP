package edu.univ.erp.data;

import edu.univ.erp.domain.User;
import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuthDao {
    private static final Logger logger = LoggerFactory.getLogger(AuthDao.class);
    private static final String LOGIN_QUERY =
            "SELECT user_id, username, role, password_hash, status, last_login, failed_login_attempts, lockout_until " +
                    "FROM users_auth WHERE username = ?";
    private static final String UPDATE_LOGIN_SUCCESS =
            "UPDATE users_auth SET last_login = NOW(), failed_login_attempts = 0, lockout_until = NULL WHERE user_id = ?";
    private static final String UPDATE_LOGIN_FAILURE =
            "UPDATE users_auth SET failed_login_attempts = failed_login_attempts + 1, lockout_until = ? WHERE user_id = ?";
    private static final String INSERT_USER =
            "INSERT INTO users_auth (username, role, password_hash, status) VALUES (?, ?, ?, 'ACTIVE')";
    private static final String UPDATE_PASSWORD_HASH =
            "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
    private static final String INSERT_PASSWORD_HISTORY =
            "INSERT INTO password_history (user_id, password_hash) VALUES (?, ?)";

    //Finds a user by username, including security metadata.
    public User findByUsername(String username) {
        User user = null;
        try (Connection conn = DbConnectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(LOGIN_QUERY)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setUserid(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(User.UserRole.valueOf(rs.getString("role")));
                    user.setPasswordhash(rs.getString("password_hash"));
                    user.setStatus(User.UserStatus.valueOf(rs.getString("status")));

                    Timestamp lastLogin = rs.getTimestamp("last_login");
                    if (lastLogin != null) user.setLastLogin(lastLogin.toLocalDateTime());

                    // NEW: Security fields
                    user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
                    Timestamp lockoutUntil = rs.getTimestamp("lockout_until");
                    if (lockoutUntil != null) user.setLockoutUntil(lockoutUntil.toLocalDateTime());

                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
        }
        return null;
    }

    //Inserts a new user record into the Auth DB.
    public int createUser(String username, User.UserRole role, String passwordhash) {
        try (Connection conn = DbConnectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, role.toString());
            stmt.setString(3, passwordhash);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating user: {}", username, e);
        }
        return -1;
    }

     //Records a successful login attempt, resetting failure counter.
    public void recordLoginSuccess(int userId) {
        try (Connection conn = DbConnectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LOGIN_SUCCESS)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error recording login success for user {}", userId, e);
        }
    }

    //Records a failed login attempt, updating counter and potential lockout time.
    public void recordLoginFailure(int userId, LocalDateTime lockoutTime) {
        if (userId < 0) return;
        try (Connection conn = DbConnectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LOGIN_FAILURE)) {
            if (lockoutTime != null) {
                stmt.setTimestamp(1, Timestamp.valueOf(lockoutTime));
            } else {
                stmt.setNull(1, Types.TIMESTAMP);
            }
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error recording login failure for user {}", userId, e);
        }
    }

    // Updates the password hash and records the old hash in history.
    public boolean changePassword(int userId, String oldPasswordHash, String newPasswordHash) {
        try (Connection conn = DbConnectionManager.getAuthConnection()) {
            conn.setAutoCommit(false);
            // 1. Insert old hash into history
            try (PreparedStatement historyStmt = conn.prepareStatement(INSERT_PASSWORD_HISTORY)) {
                historyStmt.setInt(1, userId);
                historyStmt.setString(2, oldPasswordHash);
                historyStmt.executeUpdate();
            }
            // 2. Update new hash in main table
            try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_PASSWORD_HASH)) {
                updateStmt.setString(1, newPasswordHash);
                updateStmt.setInt(2, userId);
                int rows = updateStmt.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.error("Error changing password for user {}", userId, e);
            return false;
        }
    }

    //Retrieves the password history for a given user.
    public List<String> getPasswordHistory(int userId) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT password_hash FROM password_history WHERE user_id = ? ORDER BY changed_at DESC";
        try (Connection conn = DbConnectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(rs.getString("password_hash"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving password history for user {}", userId, e);
        }
        return history;
    }
}