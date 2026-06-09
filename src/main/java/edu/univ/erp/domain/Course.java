package edu.univ.erp.domain;

public class Course {
    private int courseid;
    private String code;
    private String title;
    private int credits;

    public Course(int courseid, String code, String title, int credits) {
        this.courseid = courseid;
        this.code = code;
        this.title = title;
        this.credits = credits;
    }

    public Course(Course other) {
        this.courseid = other.courseid;
        this.code = other.code;
        this.title = other.title;
        this.credits = other.credits;
    }

    public Course() {}

    public int getCourseid() { return courseid; }
    public void setCourseid(int courseid) { this.courseid = courseid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    @Override
    public String toString() {
        return code + " - " + title;
    }
}