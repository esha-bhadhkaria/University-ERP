package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.CommonService;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.common.ChangePasswordDialog;
import edu.univ.erp.ui.common.NotificationPanel;
import edu.univ.erp.ui.login.LoginWindow;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstructorDashboard extends JFrame {

    private final User currentUser;
    private final JTable sectionTable;
    private final SectionTableModel tableModel;
    private final InstructorService instructorService;
    private final CommonService commonService = new CommonService();
    private final NotificationPanel notificationPanel;
    private static final Color NAVY_BLUE = new Color(0, 0, 128);
    private static final int CURRENT_SEMESTER = 1;
    private static final int CURRENT_YEAR = 2024;

    public InstructorDashboard(User user) {
        this.currentUser = user;
        this.instructorService = new InstructorService();
        this.tableModel = new SectionTableModel(Collections.emptyList());
        this.notificationPanel = new NotificationPanel();

        setTitle("Instructor Dashboard - Logged in as: " + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        add(createNotificationDisplayPanel(), BorderLayout.EAST);

        this.sectionTable = new JTable(tableModel);
        this.sectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel mySectionsPanel = createMySectionsPanel();
        add(mySectionsPanel, BorderLayout.CENTER);

        loadInstructorSections();
        notificationPanel.loadNotification();
    }

    private void loadInstructorSections() {
        List<Section> sections = instructorService.getInstructorSections(
                currentUser.getUserid(), CURRENT_SEMESTER, CURRENT_YEAR);
        tableModel.setSections(sections);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        boolean isMaintenance = SessionManager.getInstance().isMaintenanceModeOn();
        JLabel maintenanceBanner = new JLabel(
                isMaintenance ? "SYSTEM IS IN MAINTENANCE MODE (VIEW ONLY)" : "Normal Operation.",
                SwingConstants.CENTER);
        maintenanceBanner.setOpaque(true);
        maintenanceBanner.setBackground(isMaintenance ? new Color(255, 100, 100) : new Color(200, 255, 200));
        maintenanceBanner.setForeground(Color.BLACK);
        panel.add(maintenanceBanner, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + " | Role: Instructor");
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        infoPanel.add(welcomeLabel);
        infoPanel.add(logoutButton);
        panel.add(infoPanel, BorderLayout.SOUTH);
        return panel;
    }

    //Creates a minimal panel containing only the NotificationPanel for view-only access.
    private JPanel createNotificationDisplayPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 5 10 5 10, fillx, wrap 1", "[grow, fill]", "[]push[]"));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(NAVY_BLUE, 2),
                "Announcements"
        );
        titledBorder.setTitleColor(NAVY_BLUE);
        panel.setBorder(titledBorder);
        panel.setPreferredSize(new Dimension(250, 600));
        panel.add(notificationPanel, "grow, pushy");

        return panel;
    }

    private JPanel createMySectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(NAVY_BLUE, 2),
                "My Sections for Current Term"
        );
        titledBorder.setTitleColor(NAVY_BLUE);
        panel.setBorder(titledBorder);

        JScrollPane scrollPane = new JScrollPane(sectionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        JButton gradeBookButton = new JButton("Open Grade Book");
        gradeBookButton.addActionListener(e -> openGradeBook());

        JButton viewTimeTableButton = new JButton("View Time Table");
        viewTimeTableButton.addActionListener(e -> openTimeTable());

        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(e -> new ChangePasswordDialog(this, currentUser).setVisible(true));

        buttonPanel.add(gradeBookButton);
        buttonPanel.add(viewTimeTableButton);
        buttonPanel.add(changePasswordButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void openGradeBook() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Section selectedSection = tableModel.getSectionAt(selectedRow);
        new GradeBookWindow(this, selectedSection).setVisible(true);
        notificationPanel.loadNotification();
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


    private void logout() {
        SessionManager.getInstance().logout();
        new LoginWindow().setVisible(true);
        this.dispose();
    }

    private static class SectionTableModel extends AbstractTableModel {
        private final String[] columnNames = {"ID", "Code", "Course Title", "Schedule", "Room"};
        private List<Section> sections;
        public SectionTableModel(List<Section> sections) { this.sections = new ArrayList<>(sections); }
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
                default -> null;
            };
        }
        public Section getSectionAt(int row) { return sections.get(row); }
    }
}