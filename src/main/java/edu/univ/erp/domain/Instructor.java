package edu.univ.erp.domain;

public class Instructor {
    private int userid;
    private String department;
    private String title;
    private String username;

    public Instructor(int userid, String department, String title) {
        this.userid = userid;
        this.department = department;
        this.title = title;
    }

    public Instructor() {}

    public int getUserid() { return userid; }
    public void setUserid(int userid) { this.userid = userid; }

    public void setDepartment(String department) { this.department = department; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Override
    public String toString() {
        if (username != null && !username.isEmpty()) {
            return username + " (" + title + ")";
        }
        return "Instructor{" +
                "userid=" + userid +
                ", department='" + department + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}