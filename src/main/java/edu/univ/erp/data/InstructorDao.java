package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorDao {
    private static final Logger logger = LoggerFactory.getLogger(InstructorDao.class);
    //Create instructor profile
    public boolean createInstructor(int userid, String department, String title) {
        String sql = "INSERT INTO instructors (instructor_id, department, title) VALUES (?, ?, ?)";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userid);
            stmt.setString(2, department);
            stmt.setString(3, title);
            stmt.executeUpdate();
            logger.info("Instructor profile created for userid: {}", userid);
            return true;
        } catch (SQLException e) {
            logger.error("Error creating instructor profile", e);
            return false;
        }
    }
    // Get all instructors for admin assignment
    public List<Instructor> getAllInstructors() {
        List<Instructor> instructors = new ArrayList<>();
        String sql = "SELECT i.instructor_id, i.department, i.title, ua.username " +
                "FROM instructors i " +
                "JOIN erp_auth.users_auth ua ON i.instructor_id = ua.user_id " +
                "ORDER BY ua.username";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Instructor instructor = new Instructor();
                instructor.setUserid(rs.getInt("instructor_id"));
                instructor.setDepartment(rs.getString("department"));
                instructor.setTitle(rs.getString("title"));
                instructor.setUsername(rs.getString("username"));
                instructors.add(instructor);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all instructors", e);
        }
        return instructors;
    }
}