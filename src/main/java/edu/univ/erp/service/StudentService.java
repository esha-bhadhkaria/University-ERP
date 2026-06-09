package edu.univ.erp.service;

// ... existing imports ...
import edu.univ.erp.access.AccessRuleChecker;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.*; // Imports all DAOs
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class StudentService {
    // ... existing logger and dao definitions ...
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private final StudentDao studentDao = new StudentDao();
    private final SectionDao sectionDao = new SectionDao();
    private final EnrollmentDao enrollmentDao = new EnrollmentDao();
    private final DropDeadlineDao dropDeadlineDao = new DropDeadlineDao();
    private final GradeDao gradeDao = new GradeDao();

    private final AccessRuleChecker accessChecker = new AccessRuleChecker();

    // These constants are now ignored by the Catalog methods as requested
    private static final int CURRENT_SEMESTER = 1;
    private static final int CURRENT_YEAR = 2024;

    // ... existing methods (getStudentProfile, registerForSection, etc.) ...
    // KEEP ALL EXISTING METHODS HERE.
    public Student getStudentProfile(int userid) {
        if (!accessChecker.checkDataOwnership(userid)) return null;
        return studentDao.getStudentByUserid(userid);
    }

    /**
     * Retrieves all sections across all terms (no filter), as requested by the user.
     * FIX: Calls SectionDao.getAllSections() to fetch all terms.
     */
    public List<Section> getCourseCatalog() {
        // Calls the DAO method that ignores term filters
        return sectionDao.getAllSections();
    }

    /**
     * Searches all sections across all terms (no filter), as requested by the user.
     * FIX: Calls searchSections with 0/0 to signal the DAO to ignore term filtering.
     */
    public List<Section> searchCourseCatalog(String query) {
        // Use 0, 0 to signal the DAO (which was previously updated) to ignore the term filter
        return sectionDao.searchSections(0, 0, query);
    }

    // ... Rest of existing methods (registerForSection, dropSection, getStudentTimetable, etc.) ...
    public RegistrationResult registerForSection(int studentid, int sectionid) {
        if (!accessChecker.checkWriteAccess(User.UserRole.STUDENT)) {
            if (accessChecker.isMaintenanceModeOn()) return new RegistrationResult(false, "Maintenance mode is ON.");
            return new RegistrationResult(false, "Registration blocked.");
        }
        if (!accessChecker.checkDataOwnership(studentid)) return new RegistrationResult(false, "Cannot register another user.");
        if (enrollmentDao.isAlreadyEnrolled(studentid, sectionid)) return new RegistrationResult(false, "Already registered.");
        if (sectionDao.getAvailableSeats(sectionid) <= 0) return new RegistrationResult(false, "Section is full.");
        int id = enrollmentDao.registerForSection(studentid, sectionid);
        if (id > 0) return new RegistrationResult(true, "Registration successful!");
        return new RegistrationResult(false, "Failed to register.");
    }

    public DropResult dropSection(int studentid, int sectionid) {
        if (!accessChecker.checkWriteAccess(User.UserRole.STUDENT)) {
            if (accessChecker.isMaintenanceModeOn()) return new DropResult(false, "Maintenance mode is ON.");
            return new DropResult(false, "Drop blocked.");
        }
        if (!accessChecker.checkDataOwnership(studentid)) return new DropResult(false, "Cannot drop for others.");
        if (!enrollmentDao.isAlreadyEnrolled(studentid, sectionid)) return new DropResult(false, "Not registered.");

        Section section = sectionDao.getSectionById(sectionid);
        if (section != null) {
            LocalDate deadline = dropDeadlineDao.getDropDeadline(section.getSemester(), section.getYear());
            if (deadline != null && LocalDate.now().isAfter(deadline)) return new DropResult(false, "Deadline passed.");
        }
        if (enrollmentDao.dropSection(studentid, sectionid)) return new DropResult(true, "Dropped successfully.");
        return new DropResult(false, "Failed to drop.");
    }

    public List<Section> getStudentTimetable(int studentid, int semester, int year) {
        if (!accessChecker.checkDataOwnership(studentid)) return Collections.emptyList();
        List<Section> timetable = new ArrayList<>();
        List<edu.univ.erp.domain.Enrollment> enrollments = enrollmentDao.getStudentEnrollments(studentid, semester, year);
        for (edu.univ.erp.domain.Enrollment enrollment : enrollments) {
            Section section = sectionDao.getSectionById(enrollment.getSectionid());
            if (section != null) timetable.add(section);
        }
        timetable.sort((s1, s2) -> {
            if (s1.getDayofweek() != s2.getDayofweek()) return Integer.compare(s1.getDayofweek(), s2.getDayofweek());
            return s1.getStarttime().compareTo(s2.getStarttime());
        });
        return timetable;
    }

    public List<TranscriptEntry> getStudentTranscriptData(int studentId) {
        if (!accessChecker.checkDataOwnership(studentId)) return Collections.emptyList();
        // Use hardcoded term for simplicity in this method, or ideally fetch all COMPLETED enrollments
        List<edu.univ.erp.domain.Enrollment> enrollments = enrollmentDao.getStudentEnrollments(studentId, CURRENT_SEMESTER, CURRENT_YEAR);
        List<Grade> allGrades = gradeDao.getGradesByStudentId(studentId);
        Map<Integer, List<Grade>> gradesByEnrollment = allGrades.stream().collect(Collectors.groupingBy(Grade::getEnrollmentid));
        List<TranscriptEntry> transcript = new ArrayList<>();
        for (edu.univ.erp.domain.Enrollment enr : enrollments) {
            Section sec = sectionDao.getSectionById(enr.getSectionid());
            if (sec == null) continue;
            TranscriptEntry entry = new TranscriptEntry();
            entry.courseCode = sec.getCourseCode();
            entry.courseTitle = sec.getCourseTitle();
            entry.semester = sec.getSemester();
            entry.year = sec.getYear();
            entry.status = enr.getStatus().toString();
            entry.grades = gradesByEnrollment.getOrDefault(enr.getEnrollmentid(), Collections.emptyList());
            Double finalGrade = gradeDao.calculateFinalGrade(enr.getEnrollmentid());
            entry.finalGradeNumeric = (finalGrade != null) ? finalGrade : 0.0;
            entry.finalGradeLetter = convertNumericToLetter(finalGrade);
            transcript.add(entry);
        }
        return transcript;
    }

    private String convertNumericToLetter(Double grade) {
        if (grade == null) return "N/A";
        if (grade >= 90) return "A";
        if (grade >= 80) return "B";
        if (grade >= 70) return "C";
        if (grade >= 60) return "D";
        return "F";
    }

    public static class RegistrationResult {
        public boolean success;
        public String message;
        public RegistrationResult(boolean success, String message) { this.success = success; this.message = message; }
    }
    public static class DropResult {
        public boolean success;
        public String message;
        public DropResult(boolean success, String message) { this.success = success; this.message = message; }
    }
    public static class TranscriptEntry {
        public String courseCode;
        public String courseTitle;
        public int semester;
        public int year;
        public String status;
        public List<Grade> grades;
        public Double finalGradeNumeric;
        public String finalGradeLetter;
    }
}