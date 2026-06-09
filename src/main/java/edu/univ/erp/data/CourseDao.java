package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    private static final Logger logger = LoggerFactory.getLogger(CourseDao.class);
    //Helps in creating backup of database.
    public void createFullBackup() throws SQLException {
        String insertSql = "INSERT INTO erp_courses_backup (course_id, code, title, credits, snapshot_time) " +
                "SELECT course_id, code, title, credits, NOW() FROM courses";
        try (Connection conn = DbConnectionManager.getErpConnection();
             Statement truncateStmt = conn.createStatement();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            truncateStmt.executeUpdate("TRUNCATE TABLE erp_courses_backup"); //remove old backup
            insertStmt.executeUpdate(); //add new backup
            logger.info("Course backup successfully created.");
        }
    }
    //Restore the backup data when asked to do so
    public int RestoreFullBackup() throws SQLException {
        String restoreSql = "INSERT INTO courses (course_id, code, title, credits) " +
                "SELECT course_id, code, title, credits FROM erp_courses_backup";
        try (Connection conn = DbConnectionManager.getErpConnection();
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            int restoredCount = 0;
            try {
                // 1.Clear table data
                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                stmt.executeUpdate("TRUNCATE TABLE courses");

                // 2.Restore data from backup
                restoredCount = stmt.executeUpdate(restoreSql);
                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            return restoredCount;
        }
    }
    //Get all courses
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, code, title, credits FROM courses ORDER BY code";
        try (Connection conn = DbConnectionManager.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Course course = new Course();
                course.setCourseid(rs.getInt("course_id"));
                course.setCode(rs.getString("code"));
                course.setTitle(rs.getString("title"));
                course.setCredits(rs.getInt("credits"));
                courses.add(course);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all courses", e);
        }
        return courses;
    }
    //Create new course
    public int createCourse(String code, String title, int credits) {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setInt(3, credits);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    logger.info("Course created: {} - {}", code, title);
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating course", e);
        }
        return -1;
    }
    //Update an existing course
    public boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET code = ?, title = ?, credits = ? WHERE course_id = ?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getCode());
            stmt.setString(2, course.getTitle());
            stmt.setInt(3, course.getCredits());
            stmt.setInt(4, course.getCourseid());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error updating course {}", course.getCourseid(), e);
            return false;
        }
    }
}