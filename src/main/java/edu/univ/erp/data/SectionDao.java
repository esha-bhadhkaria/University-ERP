package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectionDao {
    private static final Logger logger = LoggerFactory.getLogger(SectionDao.class);
    private static final String SECTION_BASE_COLUMNS =
            "s.section_id, s.course_id, s.instructor_id, s.day_of_week, s.start_time, s.end_time, " +
                    "s.room, s.capacity, s.semester, s.year, c.code, c.title, c.credits, ua.username as instructor_name ";

    //Gets all sections, filtered by search query.
    public List<Section> searchSections(int semester, int year, String query) {
        List<Section> sections = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT " + SECTION_BASE_COLUMNS);
        sql.append(", COUNT(CASE WHEN e.status = 'REGISTERED' THEN 1 END) as enrolled, ");
        sql.append("(s.capacity - COUNT(CASE WHEN e.status = 'REGISTERED' THEN 1 END)) AS seats_left ");
        sql.append("FROM sections s ");
        sql.append("JOIN courses c ON s.course_id = c.course_id ");
        sql.append("LEFT JOIN instructors i ON s.instructor_id = i.instructor_id ");
        sql.append("LEFT JOIN erp_auth.users_auth ua ON i.instructor_id = ua.user_id ");
        sql.append("LEFT JOIN enrollments e ON s.section_id = e.section_id ");

        boolean filterByTerm = semester > 0 && year > 0;
        sql.append("WHERE (c.code LIKE ? OR c.title LIKE ?)");
        if (filterByTerm) {
            sql.append(" AND s.semester = ? AND s.year = ?");
        }
        sql.append(" GROUP BY s.section_id ORDER BY c.code");
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            if (filterByTerm) {
                stmt.setInt(3, semester);
                stmt.setInt(4, year);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Section section = buildSectionFromResultSet(rs, true);
                    sections.add(section);
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching sections for term {}/{}", semester, year, e);
        }
        return sections;
    }


    /**
     * Get all sections with enrollment count for a specific semester/year (for Admin/Instructor views).
     */
    public List<Section> getSectionsBySemesterYear(int semester, int year) {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT " + SECTION_BASE_COLUMNS +
                ", COUNT(CASE WHEN e.status = 'REGISTERED' THEN 1 END) as enrolled, " +
                "(s.capacity - COUNT(CASE WHEN e.status = 'REGISTERED' THEN 1 END)) AS seats_left " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors i ON s.instructor_id = i.instructor_id " +
                "LEFT JOIN erp_auth.users_auth ua ON i.instructor_id = ua.user_id " +
                "LEFT JOIN enrollments e ON s.section_id = e.section_id " +
                "WHERE s.semester = ? AND s.year = ? " +
                "GROUP BY s.section_id " +
                "ORDER BY s.day_of_week, s.start_time";

        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, semester);
            stmt.setInt(2, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(buildSectionFromResultSet(rs, true));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching sections by term", e);
        }
        return sections;
    }

    // Get all sections regardless of semester/year (for Student Catalog "Show All").
    public List<Section> getAllSections() {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT " + SECTION_BASE_COLUMNS +
                ", COUNT(CASE WHEN e.status = 'REGISTERED' THEN 1 END) as enrolled, " +
                "(s.capacity - COUNT(CASE WHEN e.status = 'REGISTERED' THEN 1 END)) AS seats_left " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors i ON s.instructor_id = i.instructor_id " +
                "LEFT JOIN erp_auth.users_auth ua ON i.instructor_id = ua.user_id " +
                "LEFT JOIN enrollments e ON s.section_id = e.section_id " +
                "GROUP BY s.section_id " +
                "ORDER BY s.semester, s.year, s.day_of_week, s.start_time";

        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sections.add(buildSectionFromResultSet(rs, true));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching all sections", e);
        }
        return sections;
    }

    //Get available seats in a section.
    public int getAvailableSeats(int sectionid) {
        String sql = "SELECT s.capacity - COUNT(e.enrollment_id) as available " +
                "FROM sections s " +
                "LEFT JOIN enrollments e ON s.section_id = e.section_id AND e.status = 'REGISTERED' " +
                "WHERE s.section_id = ? GROUP BY s.section_id";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("available");
            }
        } catch (SQLException e) { logger.error("Error getting seats", e); }
        return 0;
    }

    //Get section by ID with display info.
    public Section getSectionById(int sectionid) {
        String sql = "SELECT " + SECTION_BASE_COLUMNS +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors i ON s.instructor_id = i.instructor_id " +
                "LEFT JOIN erp_auth.users_auth ua ON i.instructor_id = ua.user_id " +
                "WHERE s.section_id = ?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return buildSectionFromResultSet(rs, false);
            }
        } catch (SQLException e) { logger.error("Error fetching section", e); }
        return null;
    }

    //Get sections for a specific instructor.
    public List<Section> getSectionsByInstructor(int instructorid, int semester, int year) {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT " + SECTION_BASE_COLUMNS +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors i ON s.instructor_id = i.instructor_id " +
                "LEFT JOIN erp_auth.users_auth ua ON i.instructor_id = ua.user_id " +
                "WHERE s.instructor_id = ? AND s.semester = ? AND s.year = ? " +
                "ORDER BY s.day_of_week, s.start_time";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorid);
            stmt.setInt(2, semester);
            stmt.setInt(3, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) sections.add(buildSectionFromResultSet(rs, false));
            }
        } catch (SQLException e) { logger.error("Error fetching instructor sections", e); }
        return sections;
    }

    //Create a new section.
    public void createSection(Section section) throws SQLException {
        String sql = "INSERT INTO sections (course_id, day_of_week, start_time, end_time, room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, section.getCourseid());
            stmt.setInt(2, section.getDayofweek());
            stmt.setTime(3, Time.valueOf(section.getStarttime()));
            stmt.setTime(4, Time.valueOf(section.getEndtime()));
            stmt.setString(5, section.getRoom());
            stmt.setInt(6, section.getCapacity());
            stmt.setInt(7, section.getSemester());
            stmt.setInt(8, section.getYear());
            stmt.executeUpdate();
        }
    }

    // Update an existing section
    public boolean updateSection(Section section) throws SQLException {
        String sql = "UPDATE sections SET day_of_week=?, start_time=?, end_time=?, room=?, capacity=?, semester=?, year=? WHERE section_id=?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, section.getDayofweek());
            stmt.setTime(2, Time.valueOf(section.getStarttime()));
            stmt.setTime(3, Time.valueOf(section.getEndtime()));
            stmt.setString(4, section.getRoom());
            stmt.setInt(5, section.getCapacity());
            stmt.setInt(6, section.getSemester());
            stmt.setInt(7, section.getYear());
            stmt.setInt(8, section.getSectionid());
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }
    //Assigns an instructor to an existing section (Admin feature).
    public void assignInstructorToSection(int sectionId, int instructorId) throws SQLException {
        String sql = "UPDATE sections SET instructor_id = ? WHERE section_id = ?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorId);
            stmt.setInt(2, sectionId);
            stmt.executeUpdate();
        }
    }

    // Deletes a section by ID. Must be called only after checking for enrollments.
    public boolean deleteSection(int sectionId) throws SQLException {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    //Creates a full backup of the sections table state into erp_backup_sections.
    public void CreateFullBackup() throws SQLException {
        String insertSql = "INSERT INTO erp_sections_backup (section_id, course_id, instructor_id, day_of_week, start_time, end_time, room, capacity, semester, year, snapshot_time) " +
                "SELECT section_id, course_id, instructor_id, day_of_week, start_time, end_time, room, capacity, semester, year, NOW() FROM sections";
        try (Connection conn = DbConnectionManager.getErpConnection();
             Statement truncateStmt = conn.createStatement();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            truncateStmt.executeUpdate("TRUNCATE TABLE erp_sections_backup");
            insertStmt.executeUpdate();
            logger.info("Section snapshot successfully created.");
        }
    }

    //Restores sections data from the backup table into the live sections table.
    public int restoreFullSnapshot() throws SQLException {
        String restoreSql = "INSERT INTO sections (section_id, course_id, instructor_id, day_of_week, start_time, end_time, room, capacity, semester, year) " +
                "SELECT section_id, course_id, instructor_id, day_of_week, start_time, end_time, room, capacity, semester, year FROM erp_sections_backup";
        try (Connection conn = DbConnectionManager.getErpConnection();
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            int restoredCount = 0;

            try {
                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                stmt.executeUpdate("TRUNCATE TABLE sections");
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

    //Helper method to map ResultSet rows to Section domain objects.
    private Section buildSectionFromResultSet(ResultSet rs, boolean includeCalculatedFields) throws SQLException {
        Section section = new Section();
        section.setSectionid(rs.getInt("section_id"));
        section.setCourseid(rs.getInt("course_id"));
        Object instructorIdObj = rs.getObject("instructor_id");
        if (instructorIdObj != null) {
            section.setInstructorid(rs.getInt("instructor_id"));
        } else {
            section.setInstructorid(-1);
        }
        section.setDayofweek(rs.getInt("day_of_week"));
        section.setStarttime(rs.getTime("start_time").toLocalTime());
        section.setEndtime(rs.getTime("end_time").toLocalTime());
        section.setRoom(rs.getString("room"));
        section.setCapacity(rs.getInt("capacity"));
        section.setSemester(rs.getInt("semester"));
        section.setYear(rs.getInt("year"));
        section.setCourseCode(rs.getString("code"));
        section.setCourseTitle(rs.getString("title"));
        section.setInstructorName(rs.getString("instructor_name"));
        section.setCredits(rs.getInt("credits"));
        if (includeCalculatedFields) {
            section.setSeatsLeft(rs.getInt("seats_left"));
        }
        return section;
    }
}