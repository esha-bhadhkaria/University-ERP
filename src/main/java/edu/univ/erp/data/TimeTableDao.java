package edu.univ.erp.data;

import edu.univ.erp.domain.TimeTable;
import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class TimeTableDao {
    private static final Logger logger = LoggerFactory.getLogger(TimeTableDao.class);

    //Uploads a new timetable file
    public boolean uploadTimeTable(String filename, byte[] fileData) {
        String sql = "INSERT INTO timetables (filename, file_data) VALUES (?, ?)";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filename);
            stmt.setBytes(2, fileData);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error uploading timetable", e);
            return false;
        }
    }

    //Gets the most recently uploaded timetable.
    public TimeTable getLatestTimeTable() {
        String sql = "SELECT id, filename, file_data, upload_date FROM timetables ORDER BY upload_date DESC LIMIT 1";
        try (Connection conn = DbConnectionManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                TimeTable tt = new TimeTable();
                tt.setId(rs.getInt("id"));
                tt.setFilename(rs.getString("filename"));
                tt.setFileData(rs.getBytes("file_data"));
                tt.setUploadDate(rs.getTimestamp("upload_date").toLocalDateTime());
                return tt;
            }
        } catch (SQLException e) {
            logger.error("Error fetching latest timetable", e);
        }
        return null;
    }
}