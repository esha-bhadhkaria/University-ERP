package edu.univ.erp.domain;

import java.time.LocalTime;

public class Section {
    private int sectionid;
    private int courseid;
    private int instructorid;
    private int dayofweek;
    private LocalTime starttime;
    private LocalTime endtime;
    private String room;
    private int capacity;
    private int semester;
    private int year;
    private String courseCode;
    private String courseTitle;
    private String instructorName;
    private int seatsLeft;
    private int credits;

    public Section() {}

    public Section(int sectionid, int courseid, int instructorid, int dayofweek,
                   LocalTime starttime, LocalTime endtime, String room, int capacity,
                   int semester, int year) {
        this.sectionid = sectionid;
        this.courseid = courseid;
        this.instructorid = instructorid;
        this.dayofweek = dayofweek;
        this.starttime = starttime;
        this.endtime = endtime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }
    public int getSectionid() { return sectionid; }
    public void setSectionid(int sectionid) { this.sectionid = sectionid; }

    public int getCourseid() { return courseid; }
    public void setCourseid(int courseid) { this.courseid = courseid; }

    public int getInstructorid() { return instructorid; }
    public void setInstructorid(int instructorid) { this.instructorid = instructorid; }

    public int getDayofweek() { return dayofweek; }
    public void setDayofweek(int dayofweek) { this.dayofweek = dayofweek; }

    public LocalTime getStarttime() { return starttime; }
    public void setStarttime(LocalTime starttime) { this.starttime = starttime; }

    public LocalTime getEndtime() { return endtime; }
    public void setEndtime(LocalTime endtime) { this.endtime = endtime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public int getSeatsLeft() { return seatsLeft; }
    public void setSeatsLeft(int seatsLeft) { this.seatsLeft = seatsLeft; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }


    public String getDayName() {
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        if (dayofweek >= 1 && dayofweek <= 7) {
            return days[dayofweek];
        }
        return "Invalid Day";
    }

    @Override
    public String toString() {
        return courseCode + " (" + getDayName() + " " + starttime + "-" + endtime + ")";
    }
}