package edu.univ.erp.service;

import edu.univ.erp.auth.PasswordUtil;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.AuthDao;
import edu.univ.erp.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

//Service layer responsible for all authentication, authorization setup, password management and security features.
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private final AuthDao authDao = new AuthDao();

    // Finds a user by username from the Auth DB. Used for Admin lookup.
    public User findUserByUsername(String username) {
        return authDao.findByUsername(username);
    }

    //Authenticate user with username and password, implementing lockout logic.
    public LoginResult login(String username, String password) {
        User user = authDao.findByUsername(username);
        if (user == null) {
            logger.warn("Login failed: user not found - {}", username);
            return new LoginResult(false, "Incorrect username or password.");
        }
        int userId = user.getUserid();

        // Checking for active account lock
        if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(LocalDateTime.now())) {
            long remainingMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), user.getLockoutUntil());
            logger.warn("Login blocked: User {} is locked until {}. Remaining: {} min", username, user.getLockoutUntil(), remainingMinutes);
            return new LoginResult(false, String.format("Account temporarily locked. Try again in %d minutes.", remainingMinutes + 1));
        }

        // Checking manually locked status
        if (user.getStatus() == User.UserStatus.LOCKED) {
            logger.warn("Login failed: account is permanently locked - {}", username);
            return new LoginResult(false, "Account is permanently locked. Contact administrator.");
        }

        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordhash())) {

            LocalDateTime lockoutTime = null;
            if (user.getFailedLoginAttempts() + 1 >= MAX_LOGIN_ATTEMPTS) {
                lockoutTime = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
                user.setLockoutUntil(lockoutTime);
                logger.warn("User {} reached max attempts. Account locked until {}", username, lockoutTime);
            }
            authDao.recordLoginFailure(userId, lockoutTime);
            if (lockoutTime != null) {
                return new LoginResult(false, String.format("Login failed. Account locked for %d minutes.", LOCKOUT_DURATION_MINUTES));
            }
            logger.warn("Login failed: incorrect password - {}. Attempt: {}/{}", username, user.getFailedLoginAttempts() + 1, MAX_LOGIN_ATTEMPTS);
            return new LoginResult(false, "Incorrect username or password.");
        }
        authDao.recordLoginSuccess(userId);
        SessionManager.getInstance().login(user);
        logger.info("User logged in successfully: {} ({})", username, user.getRole());
        return new LoginResult(true, "Login successful");
    }

    //Change password for current user
    public ChangePasswordResult changePassword(String oldPassword, String newPassword, String confirmPassword) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return new ChangePasswordResult(false, "Not logged in");
        if (!newPassword.equals(confirmPassword)) return new ChangePasswordResult(false, "New passwords do not match");
        if (newPassword.length() < 6) return new ChangePasswordResult(false, "Password must be at least 6 characters");
        String currentHash = currentUser.getPasswordhash();

        if (!PasswordUtil.verifyPassword(oldPassword, currentHash)) {
            return new ChangePasswordResult(false, "Current password is incorrect");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        List<String> history = authDao.getPasswordHistory(currentUser.getUserid());
        int historyCheckLimit = Math.min(3, history.size());

        for (int i = 0; i < historyCheckLimit; i++) {
            if (PasswordUtil.verifyPassword(newPassword, history.get(i))) {
                return new ChangePasswordResult(false, "Password was recently used. Please choose a new one.");
            }
        }

        if (authDao.changePassword(currentUser.getUserid(), currentHash, newHash)) {
            currentUser.setPasswordhash(newHash); // Update session hash
            logger.info("Password changed successfully for user: {}", currentUser.getUsername());
            return new ChangePasswordResult(true, "Password changed successfully.");
        }

        return new ChangePasswordResult(false, "Failed to change password due to a database error. Try again.");
    }

    //Retrieves the password history records for display in the Admin UI.
    public List<String> getPasswordHistoryForAdmin(int userId) {
        return authDao.getPasswordHistory(userId);
    }

    public static class LoginResult {
        public boolean success;
        public String message;

        public LoginResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static class ChangePasswordResult {
        public boolean success;
        public String message;

        public ChangePasswordResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}