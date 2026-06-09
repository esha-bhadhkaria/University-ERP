package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class Notification {
    private int id; // Always 1 for the global record
    private String message;
    private int postedByUserId;
    private String postedByUsername; // Joined field
    private User.UserRole postedByRole;
    private LocalDateTime postedAt;

    public Notification() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public void setPostedByUserId(int postedByUserId) { this.postedByUserId = postedByUserId; }

    public void setPostedByRole(User.UserRole postedByRole) { this.postedByRole = postedByRole; }

    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }

    public String getPostedByUsername() { return postedByUsername; }
    public void setPostedByUsername(String postedByUsername) { this.postedByUsername = postedByUsername; }

    @Override
    public String toString() {
        return message;
    }
}