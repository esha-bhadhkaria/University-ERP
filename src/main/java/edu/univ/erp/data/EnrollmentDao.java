package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDao {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentDao.class);

    //Check if student is already enrolled in a section
    public boolean isAlreadyEnrolled(int studentid, int sectionid) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'REGISTERED'";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentid);
            stmt.setInt(2, sectionid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking enrollment for student {} section {}", studentid, sectionid, e);
        }
        return false;
    }
    // Register student for a section
    public int registerForSection(int studentid, int sectionid) {
        String sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'REGISTERED')";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, studentid);
            stmt.setInt(2, sectionid);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int enrollmentid = rs.getInt(1);
                    logger.info("Student {} registered for section {}", studentid, sectionid);
                    return enrollmentid;
                }
            }
        } catch (SQLException e) {
            logger.error("Error registering student {} for section {}", studentid, sectionid, e);
        }
        return -1;
    }
    //Drop a section for student
    public boolean dropSection(int studentid, int sectionid) {
        String sql = "UPDATE enrollments SET status = 'DROPPED', dropped_date = NOW(), updated_at = NOW() " +
                "WHERE student_id = ? AND section_id = ? AND status = 'REGISTERED'";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentid);
            stmt.setInt(2, sectionid);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("Student {} dropped section {}", studentid, sectionid);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error dropping section for student {} section {}", studentid, sectionid, e);
        }
        return false;
    }
    // Get student's registered sections for a semester
    public List<Enrollment> getStudentEnrollments(int studentid, int semester, int year) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.student_id, e.section_id, e.status, e.enrolled_date " +
                "FROM enrollments e " +
                "JOIN sections s ON e.section_id = s.section_id " +
                "WHERE e.student_id = ? AND s.semester = ? AND s.year = ? AND e.status = 'REGISTERED'";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentid);
            stmt.setInt(2, semester);
            stmt.setInt(3, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentid(rs.getInt("enrollment_id"));
                    enrollment.setStudentid(rs.getInt("student_id"));
                    enrollment.setSectionid(rs.getInt("section_id"));
                    enrollment.setStatus(Enrollment.EnrollmentStatus.valueOf(rs.getString("status")));
                    Timestamp enrolledDate = rs.getTimestamp("enrolled_date");
                    if (enrolledDate != null) {
                        enrollment.setEnrolledDate(enrolledDate.toLocalDateTime());
                    }
                    enrollments.add(enrollment);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching enrollments for student {}", studentid, e);
        }
        return enrollments;
    }

    //Checks if there are any active enrollments for a section.
    public int countActiveEnrollmentsForSection(int sectionid) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status IN ('REGISTERED', 'COMPLETED')";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting enrollments for section {}", sectionid, e);
        }
        return -1;
    }
}