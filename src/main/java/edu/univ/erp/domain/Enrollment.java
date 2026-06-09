package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class Enrollment {
    private int enrollmentid;
    private int studentid;
    private int sectionid;
    private EnrollmentStatus status;
    private LocalDateTime enrolledDate;
    private LocalDateTime droppedDate;

    public enum EnrollmentStatus {
        REGISTERED, DROPPED, COMPLETED
    }

    public Enrollment() {}


    public int getEnrollmentid() { return enrollmentid; }
    public void setEnrollmentid(int enrollmentid) { this.enrollmentid = enrollmentid; }

    public void setStudentid(int studentid) { this.studentid = studentid; }

    public int getSectionid() { return sectionid; }
    public void setSectionid(int sectionid) { this.sectionid = sectionid; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    public void setEnrolledDate(LocalDateTime enrolledDate) { this.enrolledDate = enrolledDate; }


    @Override
    public String toString() {
        return "Enrollment{" +
                "studentid=" + studentid +
                ", sectionid=" + sectionid +
                ", status=" + status +
                '}';
    }
}
