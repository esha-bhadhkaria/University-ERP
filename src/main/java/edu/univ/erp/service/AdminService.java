package edu.univ.erp.service;

import edu.univ.erp.auth.PasswordUtil;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

//Service layer for Admin operations, including user/course management and system control (Maintenance/Backup).
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final AuthDao authDao = new AuthDao();
    private final StudentDao studentDao = new StudentDao();
    private final InstructorDao instructorDao = new InstructorDao();
    public final CourseDao courseDao = new CourseDao(); // Made public for ManageCoursesWindow reference
    private final SectionDao sectionDao = new SectionDao();
    private final SettingsDao settingsDao = new SettingsDao();
    private final TimeTableDao timeTableDao = new TimeTableDao();
    private final EnrollmentDao enrollmentDao = new EnrollmentDao(); // Added for section deletion check
    private final NotificationDao notificationDao = new NotificationDao(); // NEW

    //Create new student user
    public CreateUserResult createStudent(String username, String password, String rollno, String program, int year) {
        if (username.isEmpty() || password.isEmpty() || rollno.isEmpty() || program.isEmpty()) {
            return new CreateUserResult(false, "All fields are required.");
        }
        if (password.length() < 6) {
            return new CreateUserResult(false, "Password must be at least 6 characters.");
        }
        String passwordhash = PasswordUtil.hashPassword(password);
        int userid = authDao.createUser(username, User.UserRole.STUDENT, passwordhash);

        if (userid > 0) {
            authDao.changePassword(userid, passwordhash, passwordhash);
            if (studentDao.createStudent(userid, rollno, program, year)) {
                logger.info("New student created: {} ({})", username, rollno);
                return new CreateUserResult(true, "Student user created successfully.");
            } else {
                logger.error("Failed to create student profile for userid: {}", userid);
                return new CreateUserResult(false, "Failed to create student profile.");
            }
        }
        return new CreateUserResult(false, "Failed to create user. Username may already exist.");
    }

    //Create new instructor user
    public CreateUserResult createInstructor(String username, String password, String department, String title) {
        if (username.isEmpty() || password.isEmpty() || department.isEmpty()) {
            return new CreateUserResult(false, "All fields are required.");
        }
        if (password.length() < 6) {
            return new CreateUserResult(false, "Password must be at least 6 characters.");
        }
        String passwordhash = PasswordUtil.hashPassword(password);
        int userid = authDao.createUser(username, User.UserRole.INSTRUCTOR, passwordhash);
        if (userid > 0) {
            authDao.changePassword(userid, passwordhash, passwordhash);
            if (instructorDao.createInstructor(userid, department, title != null ? title : "Instructor")) {
                logger.info("New instructor created: {} ({})", username, department);
                return new CreateUserResult(true, "Instructor user created successfully.");
            } else {
                logger.error("Failed to create instructor profile for userid: {}", userid);
                return new CreateUserResult(false, "Failed to create instructor profile.");
            }
        }
        return new CreateUserResult(false, "Failed to create user. Username may already exist.");
    }

    //Create new course
    public CreateCourseResult createCourse(String code, String title, int credits) {
        if (code.isEmpty() || title.isEmpty() || credits <= 0) {
            return new CreateCourseResult(false, "All fields must be valid.", -1);
        }
        int courseid = courseDao.createCourse(code, title, credits);
        if (courseid > 0) {
            logger.info("New course created: {} - {}", code, title);
            return new CreateCourseResult(true, "Course created successfully.", courseid);
        }
        return new CreateCourseResult(false, "Failed to create course. Code may already exist.", -1);
    }

    // Edit existing course.
    public boolean editCourse(Course course) {
        if (course.getCode().isEmpty() || course.getTitle().isEmpty() || course.getCredits() <= 0) {
            logger.warn("Attempt to edit course {} with invalid data.", course.getCourseid());
            return false;
        }
        return courseDao.updateCourse(course);
    }

    //Toggle maintenance mode
    public void toggleMaintenanceMode(boolean on) {
        settingsDao.toggleMaintenanceMode(on);
        logger.info("Maintenance mode toggled: {}", on);
        SessionManager.getInstance().setMaintenanceModeOn(on);
    }

    public List<Section> getAllSections(int semester, int year) {
        return sectionDao.getSectionsBySemesterYear(semester, year);
    }
    public List<Instructor> getAllInstructors() {
        return instructorDao.getAllInstructors();
    }
    public boolean createSection(Section section) {
        try {
            sectionDao.createSection(section);
            return true;
        } catch (SQLException e) {
            logger.error("Failed to create section", e);
            return false;
        }
    }

    //Edit existing section.
    public boolean editSection(Section section) {
        if (section.getCapacity() < 0) {
            logger.warn("Attempt to edit section {} with invalid capacity.", section.getSectionid());
            return false;
        }
        try {
            return sectionDao.updateSection(section);
        } catch (SQLException e) {
            logger.error("Failed to update section {}", section.getSectionid(), e);
            return false;
        }
    }
    public boolean assignInstructor(int sectionId, int instructorId) {
        try {
            sectionDao.assignInstructorToSection(sectionId, instructorId);
            return true;
        } catch (SQLException e) {
            logger.error("Failed to assign instructor {} to section {}", instructorId, sectionId, e);
            return false;
        }
    }

    //Deletes a section after checking for active enrollments.
    public DeleteSectionResult deleteSection(int sectionId) {
        int count = enrollmentDao.countActiveEnrollmentsForSection(sectionId);
        if (count == -1) {
            return new DeleteSectionResult(false, "Database error during enrollment check.");
        }
        if (count > 0) {
            return new DeleteSectionResult(false,
                    "Cannot delete section: " + count + " active students are currently enrolled .");
        }
        try {
            if (sectionDao.deleteSection(sectionId)) {
                logger.info("Section ID {} successfully deleted.", sectionId);
                return new DeleteSectionResult(true, "Section successfully deleted.");
            } else {
                return new DeleteSectionResult(false, "Section not found or database failed to delete.");
            }
        } catch (SQLException e) {
            logger.error("Failed to delete section {}", sectionId, e);
            return new DeleteSectionResult(false, "Database error during deletion.");
        }
    }

    public boolean uploadTimeTable(String filename, byte[] fileBytes) {
        return timeTableDao.uploadTimeTable(filename, fileBytes);
    }

    public boolean publishNotification(String message, int userId) {
        if (message == null || message.trim().isEmpty()) {
            return notificationDao.clearNotification();
        }
        return notificationDao.publishNotification(message, userId, User.UserRole.ADMIN);
    }

    public boolean clearNotification() {
        return notificationDao.clearNotification();
    }


    public boolean backupDatabaseState() {
        try {
            courseDao.createFullBackup();
            sectionDao.CreateFullBackup();

            logger.info("FULL ERP DATABASE BACKUP COMPLETE.");
            return true;
        } catch (SQLException e) {
            logger.error("FULL DATABASE BACKUP FAILED. Check if backup tables are created.", e);
            return false;
        }
    }

    public boolean restoreDatabaseState() {
        try {
            int sectionCount = sectionDao.restoreFullSnapshot();
            int courseCount = courseDao.RestoreFullBackup();
            if (courseCount > 0 || sectionCount > 0) {
                logger.info("FULL ERP DATABASE RESTORE COMPLETE. Courses: {}, Sections: {}.",
                        courseCount, sectionCount);
                return true;
            } else {
                logger.warn("Restore attempted, but no data was restored. Snapshot tables may have been empty.");
                return true;
            }
        } catch (SQLException e) {
            logger.error("FULL DATABASE RESTORE FAILED. Ensure no pending enrollments/grades block the restore.", e);
            return false;
        }
    }

    public static class CreateUserResult {
        public boolean success;
        public String message;

        public CreateUserResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static class CreateCourseResult {
        public boolean success;
        public String message;
        public int courseid;

        public CreateCourseResult(boolean success, String message, int courseid) {
            this.success = success;
            this.message = message;
            this.courseid = courseid;
        }
    }

    public static class DeleteSectionResult {
        public boolean success;
        public String message;
        public DeleteSectionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}