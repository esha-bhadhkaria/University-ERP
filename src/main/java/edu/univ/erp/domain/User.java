package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class User {
    private int userid;
    private String username;
    private UserRole role;
    private String passwordhash;
    private UserStatus status;
    private LocalDateTime lastLogin;
    private int failedLoginAttempts;
    private LocalDateTime lockoutUntil;

    public User(int userid, String username, UserRole role, String passwordhash, UserStatus status) {
        this.userid = userid;
        this.username = username;
        this.role = role;
        this.passwordhash = passwordhash;
        this.status = status;
    }

    public User() {}
    public int getUserid() { return userid; }
    public void setUserid(int userid) { this.userid = userid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getPasswordhash() { return passwordhash; }
    public void setPasswordhash(String passwordhash) { this.passwordhash = passwordhash; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLockoutUntil() { return lockoutUntil; }
    public void setLockoutUntil(LocalDateTime lockoutUntil) { this.lockoutUntil = lockoutUntil; }

    @Override
    public String toString() {
        return "User{" +
                "userid=" + userid +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", status=" + status +
                '}';
    }

    public enum UserRole {
        ADMIN, INSTRUCTOR, STUDENT
    }

    public enum UserStatus {
        ACTIVE, INACTIVE, LOCKED
    }
}