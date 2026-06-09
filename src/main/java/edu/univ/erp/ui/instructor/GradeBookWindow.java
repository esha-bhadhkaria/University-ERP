package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.GradeEntryResult;
import edu.univ.erp.service.InstructorService.StudentGradeView;
import com.opencsv.CSVWriter;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

//Window for Instructor to enter scores and compute final grades for a specific section.
public class GradeBookWindow extends JDialog {

    private final Section section;
    private final InstructorService instructorService;
    private GradebookTableModel tableModel;
    private JTable gradesTable;
    private List<StudentGradeView> enrolledStudents;
    private Map<Integer, Map<String, Grade>> studentGradesMap;
    private List<AssessmentComponent> assessmentComponents = new ArrayList<>();

    public GradeBookWindow(Frame owner, Section section) {
        super(owner, "Grade Book for " + section.getCourseCode(), true);
        this.section = section;
        this.instructorService = new InstructorService();
        assessmentComponents.add(new AssessmentComponent("Quiz", 20.0, 100.0));
        assessmentComponents.add(new AssessmentComponent("Midterm", 30.0, 100.0));
        assessmentComponents.add(new AssessmentComponent("EndSem", 50.0, 100.0));

        setSize(1000, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        add(new JLabel("Section: " + section.getCourseCode() + " (" + section.getSectionid() + ") - " + section.getCourseTitle(), SwingConstants.CENTER), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Enter Scores", createScoresEntryPanel());
        tabbedPane.addTab("Final Grade Calculation", createFinalGradePanel());

        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadStudentGradebook();
    }
    private void exportGrades() {
        List<String[]> csvData = instructorService.exportGradebook(section.getSectionid());

        if (csvData.size() <= 1 && csvData.get(0).length <= 1) {
            JOptionPane.showMessageDialog(this, "No students enrolled to export.", "Export Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Grades to CSV");
        fileChooser.setSelectedFile(new File(section.getCourseCode() + "_S" + section.getSemester() + "_Grades.csv"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter(fileToSave))) {
                writer.writeAll(csvData);
                JOptionPane.showMessageDialog(this, "Grades exported successfully to:\n" + fileToSave.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error writing file: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importGrades() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Grades from CSV");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();

            String message = instructorService.importGrades(fileToImport.getAbsolutePath(), section.getSectionid());

            if (message.startsWith("Import Complete")) {
                JOptionPane.showMessageDialog(this, message, "Import Successful", JOptionPane.INFORMATION_MESSAGE);
                loadStudentGradebook();
            } else {
                JOptionPane.showMessageDialog(this, message, "Import Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadStudentGradebook() {
        enrolledStudents = instructorService.getStudentsInSection(section.getSectionid());
        List<Grade> allGrades = instructorService.getSectionGrades(section.getSectionid());
        studentGradesMap = allGrades.stream()
                .collect(Collectors.groupingBy(
                        Grade::getEnrollmentid,
                        Collectors.toMap(Grade::getComponent, g -> g)
                ));

        if (!allGrades.isEmpty() && assessmentComponents.size() == 3 && assessmentComponents.get(0).name.equals("Quiz")) {
            List<String> distinctComponents = allGrades.stream()
                    .map(Grade::getComponent)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            if (!distinctComponents.isEmpty()) {
                assessmentComponents.clear();
                for (String compName : distinctComponents) {
                    Grade g = allGrades.stream().filter(gr -> gr.getComponent().equals(compName)).findFirst().orElse(null);
                    if (g != null) {
                        assessmentComponents.add(new AssessmentComponent(compName, g.getWeight() * 100.0, g.getMaxscore()));
                    }
                }
            }
        }

        tableModel = new GradebookTableModel(enrolledStudents, assessmentComponents, studentGradesMap);
        gradesTable.setModel(tableModel);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 2; i < tableModel.getColumnCount(); i++) {
            gradesTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }
    }
    private JPanel createScoresEntryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        gradesTable = new JTable();
        gradesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gradesTable.setRowHeight(25);

        panel.add(new JScrollPane(gradesTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveButton = new JButton("Save All Scores");
        saveButton.addActionListener(e -> saveAllScores());

        JButton defineButton = new JButton("Define Assessments");
        defineButton.addActionListener(e -> defineAssessments());

        JButton exportButton = new JButton("Export Grade CSV");
        exportButton.addActionListener(e -> exportGrades());

        JButton importButton = new JButton("Import Grade CSV");
        importButton.addActionListener(e -> importGrades());

        buttonPanel.add(saveButton);
        buttonPanel.add(defineButton);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(exportButton);
        buttonPanel.add(importButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void defineAssessments() {
        String newComponentsStr = JOptionPane.showInputDialog(
                this,
                "Enter components (Name,Weight,MaxScore) separated by semicolons:\n(e.g. Midterm,30,100;Final,50,100)",
                "Quiz,20,100;Midterm,30,100;Final,50,100"
        );

        if (newComponentsStr != null && !newComponentsStr.isEmpty()) {
            try {
                List<AssessmentComponent> newComps = new ArrayList<>();
                String[] parts = newComponentsStr.split(";");
                for (String part : parts) {
                    String[] fields = part.split(",");
                    newComps.add(new AssessmentComponent(fields[0].trim(), Double.parseDouble(fields[1].trim()), Double.parseDouble(fields[2].trim())));
                }
                this.assessmentComponents = newComps;
                loadStudentGradebook();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid format. Please use: Name,Weight,MaxScore;", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveAllScores() {
        if (gradesTable.isEditing()) {
            gradesTable.getCellEditor().stopCellEditing();
        }

        int saved = 0;

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            StudentGradeView student = enrolledStudents.get(row);
            int enrollmentId = student.enrollmentid;

            for (int col = 2; col < tableModel.getColumnCount(); col++) {
                AssessmentComponent component = assessmentComponents.get(col - 2);
                Object value = tableModel.getValueAt(row, col);

                try {
                    double score = Double.parseDouble(value.toString());

                    GradeEntryResult result = instructorService.enterGrade(
                            enrollmentId,
                            component.name,
                            score,
                            component.maxScore,
                            component.weight / 100.0
                    );

                    if (!result.success) {
                        JOptionPane.showMessageDialog(this, result.message, "Save Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    saved++;

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid score '" + value + "' for " + student.username + " in " + component.name,
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Successfully saved " + saved + " grade entries.", "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        loadStudentGradebook();
    }

    private JPanel createFinalGradePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea statsArea = new JTextArea("You can press 'Compute Final Grades' to load statistics");
        statsArea.setEditable(false);

        panel.add(new JScrollPane(statsArea), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton computeButton = new JButton("Compute Final Grades & Stats");
        computeButton.addActionListener(e -> {
            InstructorService.ClassStatistics stats = instructorService.getClassStatistics(section.getSectionid());

            StringBuilder sb = new StringBuilder();
            sb.append("Class Statistics for ").append(section.getCourseCode()).append(" (").append(section.getSectionid()).append(")\n");
            sb.append("Total Enrolled: ").append(stats.totalEnrolled).append("\n");
            sb.append(String.format("Average Final Grade: %.2f%%\n", stats.averageFinalGrade));
            sb.append("\nComponent Averages %:\n");
            stats.componentAverages.forEach((name, avg) -> {
                sb.append(String.format("- %s: %.2f%%\n", name, avg));
            });

            statsArea.setText(sb.toString());
        });

        controlPanel.add(computeButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private static class AssessmentComponent {
        String name;
        double weight;
        double maxScore;

        AssessmentComponent(String name, double weight, double maxScore) {
            this.name = name;
            this.weight = weight;
            this.maxScore = maxScore;
        }
    }

    private static class GradebookTableModel extends AbstractTableModel {

        private List<StudentGradeView> students;
        private List<AssessmentComponent> components;
        private Map<Integer, Map<String, Grade>> gradesMap;
        private List<String> columnNames;

        public GradebookTableModel(List<StudentGradeView> students, List<AssessmentComponent> components, Map<Integer, Map<String, Grade>> gradesMap) {
            updateData(students, components, gradesMap);
        }

        public void updateData(List<StudentGradeView> students, List<AssessmentComponent> components, Map<Integer, Map<String, Grade>> gradesMap) {
            this.students = students;
            this.components = components;
            this.gradesMap = gradesMap;

            this.columnNames = new ArrayList<>();
            this.columnNames.add("Roll No");
            this.columnNames.add("Student Name");
            for (AssessmentComponent comp : components) {
                this.columnNames.add(String.format("%s (%.0f%%, /%.1f)", comp.name, comp.weight, comp.maxScore));
            }
            fireTableStructureChanged();
        }

        @Override
        public int getRowCount() { return students.size(); }
        @Override
        public int getColumnCount() { return columnNames.size(); }
        @Override
        public String getColumnName(int col) { return columnNames.get(col); }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col >= 2;
        }

        @Override
        public Object getValueAt(int row, int col) {
            StudentGradeView student = students.get(row);
            if (col == 0) return student.rollno;
            if (col == 1) return student.username;

            AssessmentComponent component = components.get(col - 2);

            Grade grade = gradesMap.getOrDefault(student.enrollmentid, Collections.emptyMap())
                    .get(component.name);

            return (grade != null) ? grade.getScore() : 0.0;
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            if (col < 2) return;

            StudentGradeView student = students.get(row);
            AssessmentComponent component = components.get(col - 2);
            double score;

            try {
                score = Double.parseDouble(aValue.toString());
            } catch (NumberFormatException e) {
                return;
            }

            Map<String, Grade> studentGrades = gradesMap.computeIfAbsent(student.enrollmentid, k -> new java.util.HashMap<>());
            Grade grade = studentGrades.get(component.name);

            if (grade == null) {
                grade = new Grade(student.enrollmentid, component.name, score, component.maxScore, component.weight / 100.0);
                studentGrades.put(component.name, grade);
            } else {
                grade.setScore(score);
            }
            fireTableCellUpdated(row, col);
        }
    }
}