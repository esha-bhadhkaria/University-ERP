package edu.univ.erp.service;

import edu.univ.erp.access.AccessRuleChecker;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InstructorService {
    private static final Logger logger = LoggerFactory.getLogger(InstructorService.class);
    private final SectionDao sectionDao = new SectionDao();
    private final EnrollmentDao enrollmentDao = new EnrollmentDao();
    private final GradeDao gradeDao = new GradeDao();
    private final NotificationDao notificationDao = new NotificationDao();

    private final AccessRuleChecker accessChecker = new AccessRuleChecker();

    //Get instructor's sections for current semester
    public List<Section> getInstructorSections(int instructorid, int semester, int year) {
        return sectionDao.getSectionsByInstructor(instructorid, semester, year);
    }

    //Get enrolled students in a section
    public List<StudentGradeView> getStudentsInSection(int sectionid) {
        List<StudentGradeView> students = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, e.student_id, s.roll_no, a.username " +
                "FROM enrollments e " +
                "JOIN students s ON e.student_id = s.student_id " +
                "JOIN erp_auth.users_auth a ON s.student_id = a.user_id " +
                "WHERE e.section_id = ? AND e.status = 'REGISTERED' " +
                "ORDER BY s.roll_no";
        try (Connection conn = DbConnectionManager.getErpConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionid);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StudentGradeView view = new StudentGradeView();
                    view.enrollmentid = rs.getInt("enrollment_id");
                    view.studentid = rs.getInt("student_id");
                    view.rollno = rs.getString("roll_no");
                    view.username = rs.getString("username");
                    students.add(view);
                }
            }
        } catch (java.sql.SQLException e) {
            logger.error("Error fetching students in section {}", sectionid, e);
        }
        return students;
    }

    //Enter or update grades for a student
    public GradeEntryResult enterGrade(int enrollmentid, String component, double score, double maxscore, double weight) {
        if (!accessChecker.checkWriteAccess(User.UserRole.INSTRUCTOR)) {
            if (accessChecker.isMaintenanceModeOn()) {
                return new GradeEntryResult(false, "Maintenance mode is ON. Grades cannot be modified.");
            }
            return new GradeEntryResult(false, "Not allowed to enter grades. (Role/Login failed).");
        }
        if (score < 0 || maxscore <= 0 || score > maxscore) {
            return new GradeEntryResult(false, "Invalid score. Score must be between 0 and max score.");
        }
        if (weight > 1 && weight <= 100) {
            weight /= 100.0;
        } else if (weight < 0 || weight > 1) {
            return new GradeEntryResult(false, "Weight must be between 0 and 1 (or 0 and 100).");
        }

        if (gradeDao.upsertGrade(enrollmentid, component, score, maxscore, weight)) {
            logger.info("Grade entered for enrollment {} component {}: {}/{}", enrollmentid, component, score, maxscore);
            return new GradeEntryResult(true, "Grade saved successfully.");
        }

        return new GradeEntryResult(false, "Failed to save grade. Please try again.");
    }

    //Compute final grades and get class statistics for a section (Week 5)
    public ClassStatistics getClassStatistics(int sectionid) {
        List<StudentGradeView> students = getStudentsInSection(sectionid);
        ClassStatistics stats = new ClassStatistics();
        stats.totalEnrolled = students.size();

        Map<String, List<Double>> componentScores = new HashMap<>();

        for (StudentGradeView student : students) {
            Double finalGrade = gradeDao.calculateFinalGrade(student.enrollmentid);

            if (finalGrade != null && finalGrade >= 0) {
                stats.finalGrades.add(finalGrade);
            }

            List<Grade> grades = gradeDao.getGradesByEnrollment(student.enrollmentid);
            for (Grade grade : grades) {
                componentScores.computeIfAbsent(grade.getComponent(), k -> new ArrayList<>())
                        .add(grade.getPercentage());
            }
        }

        if (!stats.finalGrades.isEmpty()) {
            stats.averageFinalGrade = stats.finalGrades.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
        }

        for (Map.Entry<String, List<Double>> entry : componentScores.entrySet()) {
            double avg = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            stats.componentAverages.put(entry.getKey(), avg);
        }

        return stats;
    }

    // Get grades for a section
    public List<Grade> getSectionGrades(int sectionid) {
        return gradeDao.getGradesBySection(sectionid);
    }

    // Generates CSV content for a section's current gradebook state
    public List<String[]> exportGradebook(int sectionId) {
        if (!accessChecker.checkDataOwnership(SessionManager.getInstance().getCurrentUser().getUserid())) {
            return Collections.singletonList(new String[]{"Access Denied."});
        }
        List<StudentGradeView> students = getStudentsInSection(sectionId);
        if (students.isEmpty()) return Collections.singletonList(new String[]{"No students enrolled."});
        List<Grade> grades = gradeDao.getGradesBySection(sectionId);
        Map<Integer, List<Grade>> gradesByEnrollment = grades.stream()
                .collect(Collectors.groupingBy(Grade::getEnrollmentid));
        Map<String, Double> componentWeights = grades.stream()
                .collect(Collectors.toMap(Grade::getComponent, Grade::getWeight, (w1, w2) -> w1));
        List<String> assessmentNames = new ArrayList<>(componentWeights.keySet());
        assessmentNames.sort(String::compareTo);
        List<String[]> csvData = new ArrayList<>();

        List<String> headerList = new ArrayList<>(Arrays.asList("Roll No", "Student Name", "Enrollment ID"));
        headerList.addAll(assessmentNames.stream().map(name -> name + " (Score)").collect(Collectors.toList()));
        csvData.add(headerList.toArray(new String[0]));
        for (StudentGradeView student : students) {
            List<String> row = new ArrayList<>();
            row.add(student.rollno);
            row.add(student.username);
            row.add(String.valueOf(student.enrollmentid));

            List<Grade> studentGradesList = gradesByEnrollment.getOrDefault(student.enrollmentid, Collections.emptyList());
            Map<String, Grade> studentGradesMap = studentGradesList.stream()
                    .collect(Collectors.toMap(Grade::getComponent, g -> g));

            for (String component : assessmentNames) {
                Grade grade = studentGradesMap.get(component);
                row.add(grade != null ? String.valueOf(grade.getScore()) : "");
            }
            csvData.add(row.toArray(new String[0]));
        }

        return csvData;
    }

    // Processes an imported CSV file and saves updated scores to the database.
    public String importGrades(String filePath, int sectionId) {
        if (!accessChecker.checkWriteAccess(User.UserRole.INSTRUCTOR)) {
            if (accessChecker.isMaintenanceModeOn()) return "Import Failed: Maintenance Mode is ON.";
            return "Import Failed: Access Denied .";
        }
        List<Grade> currentGrades = gradeDao.getGradesBySection(sectionId);
        if (currentGrades.isEmpty()) {
            return "Import Failed: No existing assessment components found. Define components first.";
        }
        Map<String, Grade> templateGrades = currentGrades.stream()
                .collect(Collectors.toMap(Grade::getComponent, g -> g, (g1, g2) -> g1));
        int recordsUpdated = 0;
        int errors = 0;

        try (Reader reader = new FileReader(filePath);
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null || header.length < 4) return "Import Failed: Invalid CSV format.";
            List<String> assessmentNames = new ArrayList<>();
            for (int i = 3; i < header.length; i++) {
                assessmentNames.add(header[i].replace(" (Score)", "").trim());
            }

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < header.length) continue;

                int enrollmentId;
                try {
                    enrollmentId = Integer.parseInt(line[2]);
                } catch (NumberFormatException e) {
                    errors++;
                    logger.error("Skipping row. Invalid Enrollment ID format: {}", line[2]);
                    continue;
                }
                for (int i = 0; i < assessmentNames.size(); i++) {
                    String componentName = assessmentNames.get(i);
                    String scoreStr = line[i + 3].trim();
                    if (scoreStr.isEmpty()) continue;

                    Grade template = templateGrades.get(componentName);
                    if (template == null) continue;
                    try {
                        double score = Double.parseDouble(scoreStr);
                        gradeDao.upsertGrade(
                                enrollmentId, componentName, score,
                                template.getMaxscore(), template.getWeight()
                        );
                        recordsUpdated++;
                    } catch (NumberFormatException e) {
                        errors++;
                        logger.error("Skipping score for Enrollment {} in {} due to invalid number format: {}",
                                enrollmentId, componentName, scoreStr);
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            return "Import Complete: File read error: " + e.getMessage();
        }
        return String.format("Import Complete: %d scores updated. %d errors skipped.", recordsUpdated, errors);
    }

    public static class StudentGradeView {
        public int enrollmentid;
        public int studentid;
        public String rollno;
        public String username;
    }

    public static class GradeEntryResult {
        public boolean success;
        public String message;

        public GradeEntryResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static class ClassStatistics {
        public int totalEnrolled;
        public double averageFinalGrade;
        public List<Double> finalGrades = new ArrayList<>();
        public Map<String, Double> componentAverages = new HashMap<>();
    }
}