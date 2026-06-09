package edu.univ.erp.access;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.SettingsDao;
import edu.univ.erp.domain.User;

/* Central access control for role-based permissions and maintenance mode checks.
   All modifying actions in the Service layer must call checkWriteAccess() first. */
public class AccessRuleChecker {
    private final SettingsDao settingsDao;
    public AccessRuleChecker() {
        this.settingsDao = new SettingsDao();
    }
//Checks if the system is currently in Maintenance Mode by reading the DB.
    public boolean isMaintenanceModeOn() {
        return settingsDao.isMaintenanceOn();
    }
//Checks if the current user is allowed to perform a general WRITE action based on their role and the Maintenance Mode status.
    public boolean checkWriteAccess(User.UserRole roleRequired) {
        SessionManager session = SessionManager.getInstance();
        User currentUser = session.getCurrentUser();
        if (!session.isLoggedIn()) {
            System.err.println("Access Denied: User not logged in.");
            return false;
        }
        if (isMaintenanceModeOn() && currentUser.getRole() != User.UserRole.ADMIN) {
            System.err.println("Access Denied: Maintenance Mode is ON. Writes are blocked.");
            return false;
        }
        if (roleRequired == User.UserRole.ADMIN && session.isAdmin()) {
            return true;
        } else if (roleRequired == User.UserRole.INSTRUCTOR && (session.isAdmin() || session.isInstructor())) {
            return true;
        } else if (roleRequired == User.UserRole.STUDENT && (session.isAdmin() || session.isStudent() || session.isInstructor())) {
            return true;
        }
        System.err.printf("Access Denied: Insufficient role (%s required, %s current).%n", roleRequired, currentUser.getRole());
        return false;
    }
    //ensures a Student or Instructor only views/changes their own records.The current user's ID must match the requested ID.
    public boolean checkDataOwnership(int requestedUserId) {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) return false;
        if (session.isAdmin()) {
            return true;
        }if (session.getCurrentUser().getUserid() == requestedUserId) {
            return true;
        }
        System.err.println("Access Denied: Attempt to view or modify another user's data.");
        return false;
    }
}