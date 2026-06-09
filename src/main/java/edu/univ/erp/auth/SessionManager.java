package edu.univ.erp.auth;
import edu.univ.erp.domain.User;

//Class to hold the application's current session state .The currently logged-in User and the current Maintenance Mode status.
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    // Default to false.
    private boolean isMaintenanceModeOn = false;
    private SessionManager() {
    }
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        System.out.println("Session started for: " + user.getUsername() + " (" + user.getRole() + ")");
    }

    public void logout() {
        this.currentUser = null;
        System.out.println("Session closed.");
    }

    public User getCurrentUser() {
        return currentUser;
    }
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole() == User.UserRole.ADMIN;
    }
    public boolean isInstructor() {
        return isLoggedIn() && currentUser.getRole() == User.UserRole.INSTRUCTOR;
    }
    public boolean isStudent() {
        return isLoggedIn() && currentUser.getRole() == User.UserRole.STUDENT;
    }
    public boolean isMaintenanceModeOn() {
        return isMaintenanceModeOn;
    }

    //Updates the maintenance mode status across the application.
    public void setMaintenanceModeOn(boolean status) {
        this.isMaintenanceModeOn = status;
        System.out.println("Maintenance Mode updated to: " + status);
    }
}