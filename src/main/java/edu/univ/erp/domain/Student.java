package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class Student {
    private int userid;
    private String rollno;
    private String program;
    private int year;
    private LocalDateTime createdAt;

    public Student(int userid, String rollno, String program, int year) {
        this.userid = userid;
        this.rollno = rollno;
        this.program = program;
        this.year = year;
    }

    public Student() {}

    public int getUserid() { return userid; }
    public void setUserid(int userid) { this.userid = userid; }

    public String getRollno() { return rollno; }
    public void setRollno(String rollno) { this.rollno = rollno; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Student{" +
                "userid=" + userid +
                ", rollno='" + rollno + '\'' +
                ", program='" + program + '\'' +
                ", year=" + year +
                '}';
    }
}
