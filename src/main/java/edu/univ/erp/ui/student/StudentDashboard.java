package edu.univ.erp.ui.student;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.CommonService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.StudentService.DropResult;
import edu.univ.erp.ui.common.ChangePasswordDialog;
import edu.univ.erp.ui.common.NotificationPanel;
import edu.univ.erp.ui.login.LoginWindow;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class StudentDashboard extends JFrame {

    private final User currentUser;
    private Student studentProfile;
    private final StudentService studentService;
    private final TimetableTableModel tableModel;
    private final JTable registeredTable;
    private final JLabel maintenanceBanner;
    private final CommonService commonService = new CommonService();
    private final NotificationPanel notificationPanel;
    private static final Color NAVY_BLUE = new Color(0, 0, 128);

    private static final int CURRENT_SEMESTER = 1;
    private static final int CURRENT_YEAR = 2024;


    public StudentDashboard(User user) {
        this.currentUser = user;
        this.studentService = new StudentService();
        this.tableModel = new TimetableTableModel(Collections.emptyList());
        this.notificationPanel = new NotificationPanel();

        loadStudentProfile();
        setTitle("Student Dashboard - " + (studentProfile != null ? studentProfile.getRollno() : currentUser.getUsername()));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        boolean isMaintenance = SessionManager.getInstance().isMaintenanceModeOn();
        this.maintenanceBanner = new JLabel(
                isMaintenance ? "SYSTEM IS IN MAINTENANCE MODE (VIEW ONLY)" : "Normal Operation.",
                SwingConstants.CENTER);
        maintenanceBanner.setOpaque(true);
        maintenanceBanner.setBackground(isMaintenance ? new Color(255, 100, 100) : new Color(200, 255, 200));
        maintenanceBanner.setForeground(Color.BLACK);

        JPanel headerPanel = createHeaderPanel();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(maintenanceBanner, BorderLayout.NORTH);
        topPanel.add(headerPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        registeredTable = new JTable(tableModel);
        registeredTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createNavPanel(), createContentPanel());
        mainSplitPane.setDividerLocation(200);
        JSplitPane notificationSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainSplitPane, notificationPanel);
        notificationSplitPane.setDividerLocation(950);
        notificationSplitPane.setResizeWeight(1.0);

        add(notificationSplitPane, BorderLayout.CENTER);

        loadTimetable();
        notificationPanel.loadNotification();
    }

    private void loadStudentProfile() {
        if (currentUser != null) {
            this.studentProfile = studentService.getStudentProfile(currentUser.getUserid());
            if (this.studentProfile == null) {
                JOptionPane.showMessageDialog(this, "Could not load student profile data.", "Data Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadTimetable() {
        if (studentProfile == null) return;
        List<Section> sections = studentService.getStudentTimetable(
                studentProfile.getUserid(), CURRENT_SEMESTER, CURRENT_YEAR);
        tableModel.setSections(sections);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String profileInfo = (studentProfile != null) ? studentProfile.getRollno() + " (" + currentUser.getUsername() + ")" : currentUser.getUsername();
        JLabel welcomeLabel = new JLabel("Welcome, " + profileInfo + " | Role: Student");
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        panel.add(welcomeLabel);
        panel.add(logoutButton);
        return panel;
    }

    private JPanel createNavPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(NAVY_BLUE, 2),
                "Navigation"
        );
        titledBorder.setTitleColor(NAVY_BLUE);
        panel.setBorder(titledBorder);
        Dimension maxButtonSize = new Dimension(200, 40);

        JButton catalogButton = new JButton("Browse Course Catalog");
        catalogButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        catalogButton.setMaximumSize(maxButtonSize);
        catalogButton.addActionListener(e -> {
            new CourseCatalogWindow(this, maintenanceBanner, this::loadTimetable).setVisible(true);
            notificationPanel.loadNotification();
        });

        JButton gradesButton = new JButton("View Grades & Transcript");
        gradesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        gradesButton.setMaximumSize(maxButtonSize);
        gradesButton.addActionListener(e -> viewGrades());
        JButton viewTimeTableButton = new JButton("View Time Table");
        viewTimeTableButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewTimeTableButton.setMaximumSize(maxButtonSize);
        viewTimeTableButton.addActionListener(e -> openTimeTable());

        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        changePasswordButton.setMaximumSize(maxButtonSize);
        changePasswordButton.addActionListener(e -> new ChangePasswordDialog(this, currentUser).setVisible(true));
        panel.add(Box.createVerticalStrut(20));
        panel.add(catalogButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(gradesButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(viewTimeTableButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(changePasswordButton);

        return panel;
    }

    private void openTimeTable() {
        java.io.File file = commonService.downloadLatestTimeTable();
        if (file != null && file.exists()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "File downloaded to: " + file.getAbsolutePath(), "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No time table has been uploaded by the Admin yet.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(NAVY_BLUE, 2),
                "My Registered Sections (Timetable View)"
        );
        titledBorder.setTitleColor(NAVY_BLUE);
        panel.setBorder(titledBorder);

        JScrollPane scrollPane = new JScrollPane(registeredTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton dropButton = new JButton("Drop Selected Section");
        dropButton.addActionListener(e -> dropSection());
        buttonPanel.add(dropButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void dropSection() {
        int selectedRow = registeredTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to drop.", "Drop Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Section sectionToDrop = tableModel.getSectionAt(selectedRow);
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to drop " + sectionToDrop.getCourseCode() + "?",
                "Confirm Drop", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            DropResult result = studentService.dropSection(studentProfile.getUserid(), sectionToDrop.getSectionid());
            if (result.success) {
                JOptionPane.showMessageDialog(this, result.message, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadTimetable();
            } else {
                JOptionPane.showMessageDialog(this, result.message, "Drop Failed", JOptionPane.ERROR_MESSAGE);
            }
            notificationPanel.loadNotification();
        }
    }

    private void viewGrades() {
        if (studentProfile == null) {
            JOptionPane.showMessageDialog(this, "Student profile is not loaded.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new GradesTranscriptWindow(this, studentProfile).setVisible(true);
        notificationPanel.loadNotification();
    }

    private void logout() {
        SessionManager.getInstance().logout();
        new LoginWindow().setVisible(true);
        this.dispose();
    }

    private static class TimetableTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Section ID", "Code", "Course Title", "Schedule", "Room", "Instructor"};
        private List<Section> sections;
        public TimetableTableModel(List<Section> sections) { this.sections = new ArrayList<>(sections); }
        public void setSections(List<Section> sections) { this.sections = new ArrayList<>(sections); fireTableDataChanged(); }
        @Override public int getRowCount() { return sections.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int col) { return columnNames[col]; }
        @Override public Object getValueAt(int row, int col) {
            Section s = sections.get(row);
            return switch (col) {
                case 0 -> s.getSectionid();
                case 1 -> s.getCourseCode();
                case 2 -> s.getCourseTitle();
                case 3 -> s.getDayName() + " " + s.getStarttime() + "-" + s.getEndtime();
                case 4 -> s.getRoom();
                case 5 -> s.getInstructorName();
                default -> null;
            };
        }
        public Section getSectionAt(int row) { return sections.get(row); }
    }
}