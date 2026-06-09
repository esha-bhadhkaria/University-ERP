package edu.univ.erp.data;

import edu.univ.erp.domain.Student;
import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

public class StudentDao {
    private static final Logger logger = LoggerFactory.getLogger(StudentDao.class);

    //Get student by userid
    public Student getStudentByUserid(int userid) {
        String sql = "SELECT student_id, roll_no, program, year FROM students WHERE student_id = ?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setUserid(rs.getInt("student_id"));
                    student.setRollno(rs.getString("roll_no"));
                    student.setProgram(rs.getString("program"));
                    student.setYear(rs.getInt("year"));
                    return student;
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching student by userid: {}", userid, e);
        }
        return null;
    }

    //Create student profile
    public boolean createStudent(int userid, String rollno, String program, int year) {
        String sql = "INSERT INTO students (student_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userid);
            stmt.setString(2, rollno);
            stmt.setString(3, program);
            stmt.setInt(4, year);
            stmt.executeUpdate();
            logger.info("Student profile created for userid: {}", userid);
            return true;
        } catch (SQLException e) {
            logger.error("Error creating student profile", e);
            return false;
        }
    }
}