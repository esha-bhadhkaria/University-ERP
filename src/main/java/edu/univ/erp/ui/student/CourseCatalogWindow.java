package edu.univ.erp.ui.student;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.StudentService.RegistrationResult;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CourseCatalogWindow extends JDialog {

    private final JLabel maintenanceBanner;
    private final StudentService studentService;
    private final Student currentStudent;
    private final Runnable refreshDashboardCallback;
    private final CatalogTableModel tableModel;
    private final JTable catalogTable;
    private JTextField searchField;

    public CourseCatalogWindow(Frame owner, JLabel maintenanceBanner, Runnable refreshDashboardCallback) {
        super(owner, "Course Catalog and Registration", false);
        this.maintenanceBanner = maintenanceBanner;
        this.refreshDashboardCallback = refreshDashboardCallback;
        this.studentService = new StudentService();
        this.currentStudent = studentService.getStudentProfile(SessionManager.getInstance().getCurrentUser().getUserid());
        this.tableModel = new CatalogTableModel(Collections.emptyList());

        setSize(1200, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        add(createStatusPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        catalogTable = new JTable(tableModel);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabbedPane.addTab("Browse Available Sections", createCatalogPanel());

        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close Catalog");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadCatalog();
    }

    private void loadCatalog() {
        List<Section> sections = studentService.getCourseCatalog();
        tableModel.setSections(sections);
    }

    private void searchCatalog() {
        String query = searchField.getText().trim();
        List<Section> sections = studentService.searchCourseCatalog(query);
        tableModel.setSections(sections);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(maintenanceBanner, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Course (Code/Title):"));
        searchField = new JTextField(20);
        searchField.addActionListener(e -> searchCatalog());
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCatalog());
        searchPanel.add(searchButton);

        JButton refreshButton = new JButton("Show All");
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadCatalog();
        });
        searchPanel.add(refreshButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(catalogTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton registerButton = new JButton("Register for Selected Section");
        registerButton.addActionListener(e -> registerSection());
        buttonPanel.add(registerButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void registerSection() {
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to register.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (currentStudent == null) {
            JOptionPane.showMessageDialog(this, "Student profile data missing.", "System Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Section sectionToRegister = tableModel.getSectionAt(selectedRow);
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Register for " + sectionToRegister.getCourseCode() + " (" + sectionToRegister.getSectionid() + ")?",
                "Confirm Registration", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                RegistrationResult result = studentService.registerForSection(
                        currentStudent.getUserid(), sectionToRegister.getSectionid());

                if (result.success) {
                    JOptionPane.showMessageDialog(this, result.message, "Success", JOptionPane.INFORMATION_MESSAGE);
                    searchCatalog();
                    if (refreshDashboardCallback != null) refreshDashboardCallback.run();
                } else {
                    JOptionPane.showMessageDialog(this, result.message, "Registration Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private static class CatalogTableModel extends AbstractTableModel {
        private final String[] columnNames = {"ID", "Code", "Title", "Credits", "Schedule", "Room", "Capacity", "Seats Left", "Instructor"};
        private List<Section> sections;
        public CatalogTableModel(List<Section> sections) {
            this.sections = new ArrayList<>(sections);
        }
        public void setSections(List<Section> sections) {
            this.sections = new ArrayList<>(sections);
            fireTableDataChanged();
        }
        @Override
        public int getRowCount() { return sections.size(); }
        @Override
        public int getColumnCount() { return columnNames.length; }
        @Override
        public String getColumnName(int col) { return columnNames[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Section s = sections.get(row);
            return switch (col) {
                case 0 -> s.getSectionid();
                case 1 -> s.getCourseCode();
                case 2 -> s.getCourseTitle();
                case 3 -> s.getCredits();
                case 4 -> s.getDayName() + " " + s.getStarttime();
                case 5 -> s.getRoom();
                case 6 -> s.getCapacity();
                case 7 -> s.getSeatsLeft();
                case 8 -> s.getInstructorName();
                default -> null;
            };
        }
        public Section getSectionAt(int row) {
            return sections.get(row);
        }
    }
}