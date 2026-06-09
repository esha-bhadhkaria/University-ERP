package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;
import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDao {
    private static final Logger logger = LoggerFactory.getLogger(GradeDao.class);

    //Get grades for an enrollment
    public List<Grade> getGradesByEnrollment(int enrollmentid) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT grade_id, enrollment_id, component, score, max_score, weight " +
                "FROM grades WHERE enrollment_id = ? ORDER BY component";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentid);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = new Grade();
                    grade.setGradeid(rs.getInt("grade_id"));
                    grade.setEnrollmentid(rs.getInt("enrollment_id"));
                    grade.setComponent(rs.getString("component"));
                    grade.setScore(rs.getDouble("score"));
                    grade.setMaxscore(rs.getDouble("max_score"));
                    grade.setWeight(rs.getDouble("weight"));
                    grades.add(grade);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching grades for enrollment {}", enrollmentid, e);
        }
        return grades;
    }
    //Update or insert grade component
    public boolean upsertGrade(int enrollmentid, String component, double score, double maxscore, double weight) {
        String sql = "INSERT INTO grades (enrollment_id, component, score, max_score, weight) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE score = ?, max_score = ?, weight = ?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentid);
            stmt.setString(2, component);
            stmt.setDouble(3, score);
            stmt.setDouble(4, maxscore);
            stmt.setDouble(5, weight);
            stmt.setDouble(6, score);
            stmt.setDouble(7, maxscore);
            stmt.setDouble(8, weight);
            stmt.executeUpdate();
            logger.info("Grade updated for enrollment {} component {}", enrollmentid, component);
            return true;
        } catch (SQLException e) {
            logger.error("Error updating grade for enrollment {}", enrollmentid, e);
            return false;
        }
    }

    //Calculate final grade for an enrollment
    public Double calculateFinalGrade(int enrollmentid) {
        String sql = "SELECT SUM((score / max_score) * weight) / SUM(weight) * 100 as final_grade_percentage " +
                "FROM grades WHERE enrollment_id = ?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Double finalGrade = rs.getDouble("final_grade_percentage");
                    if (!rs.wasNull()) {
                        return finalGrade;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating final grade for enrollment {}", enrollmentid, e);
        }
        return null;
    }
    //Get all grades for a section
    public List<Grade> getGradesBySection(int sectionid) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.grade_id, g.enrollment_id, g.component, g.score, g.max_score, g.weight " +
                "FROM grades g " +
                "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                "JOIN sections s ON e.section_id = s.section_id " +
                "WHERE s.section_id = ? AND e.status = 'REGISTERED' " +
                "ORDER BY e.student_id, g.component";

        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionid);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = new Grade();
                    grade.setGradeid(rs.getInt("grade_id"));
                    grade.setEnrollmentid(rs.getInt("enrollment_id"));
                    grade.setComponent(rs.getString("component"));
                    grade.setScore(rs.getDouble("score"));
                    grade.setMaxscore(rs.getDouble("max_score"));
                    grade.setWeight(rs.getDouble("weight"));
                    grades.add(grade);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching grades for section {}", sectionid, e);
        }
        return grades;
    }

    //Get all grades for a specific student (for transcript)
    public List<Grade> getGradesByStudentId(int studentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.grade_id, g.enrollment_id, g.component, g.score, g.max_score, g.weight " +
                "FROM grades g " +
                "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.student_id = ? " +
                "ORDER BY e.section_id, g.component";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = new Grade();
                    grade.setGradeid(rs.getInt("grade_id"));
                    grade.setEnrollmentid(rs.getInt("enrollment_id"));
                    grade.setComponent(rs.getString("component"));
                    grade.setScore(rs.getDouble("score"));
                    grade.setMaxscore(rs.getDouble("max_score"));
                    grade.setWeight(rs.getDouble("weight"));
                    grades.add(grade);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching all grades for student {}", studentId, e);
        }
        return grades;
    }
}