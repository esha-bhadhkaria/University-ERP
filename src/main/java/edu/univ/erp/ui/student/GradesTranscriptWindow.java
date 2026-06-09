package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.StudentService.TranscriptEntry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//Window for Student to view their grades and download a transcript.
public class GradesTranscriptWindow extends JDialog {

    private final StudentService studentService;
    private final Student currentStudent;
    private final TranscriptTableModel tableModel;
    private final JTable transcriptTable;
    private final List<TranscriptEntry> transcriptData;

    public GradesTranscriptWindow(Frame owner, Student student) {
        super(owner, "My Grades and Transcript", true);
        this.currentStudent = student;
        this.studentService = new StudentService();
        this.transcriptData = studentService.getStudentTranscriptData(currentStudent.getUserid());
        this.tableModel = new TranscriptTableModel(transcriptData);

        setSize(1000, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Transcript for " + currentStudent.getRollno(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        add(headerPanel, BorderLayout.NORTH);
        transcriptTable = new JTable(tableModel);
        transcriptTable.setRowHeight(25);
        transcriptTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        add(new JScrollPane(transcriptTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("Download Transcript (CSV)");
        exportButton.addActionListener(e -> exportTranscript());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    //Exports the current transcript view to a CSV file.
    private void exportTranscript() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript as CSV");
        fileChooser.setSelectedFile(new java.io.File(currentStudent.getRollno() + "_Transcript.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.append("Course Code,Course Title,Semester,Year,Status,Final Grade (Letter),Final Grade (%)\n");
                for (TranscriptEntry entry : transcriptData) {
                    writer.append(String.format("\"%s\",\"%s\",%d,%d,\"%s\",\"%s\",%.2f\n",
                            entry.courseCode,
                            entry.courseTitle,
                            entry.semester,
                            entry.year,
                            entry.status,
                            entry.finalGradeLetter,
                            entry.finalGradeNumeric
                    ));
                }
                JOptionPane.showMessageDialog(this, "Transcript saved successfully to:\n" + fileToSave.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //Custom Table Model for displaying the Student's Transcript.
    private static class TranscriptTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Course", "Title", "Term", "Status", "Components", "Final Grade", "Letter"};
        private List<TranscriptEntry> entries;

        public TranscriptTableModel(List<TranscriptEntry> entries) {
            this.entries = new ArrayList<>(entries);
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            TranscriptEntry e = entries.get(row);
            return switch (col) {
                case 0 -> e.courseCode;
                case 1 -> e.courseTitle;
                case 2 -> "S" + e.semester + " " + e.year;
                case 3 -> e.status;
                case 4 -> formatComponents(e.grades);
                case 5 -> String.format("%.2f%%", e.finalGradeNumeric);
                case 6 -> e.finalGradeLetter;
                default -> null;
            };
        }

        private String formatComponents(List<Grade> grades) {
            if (grades == null || grades.isEmpty()) return "N/A";
            return grades.stream()
                    .map(g -> String.format("%s (%.1f/%.1f)", g.getComponent(), g.getScore(), g.getMaxscore()))
                    .collect(Collectors.joining(", "));
        }
    }
}