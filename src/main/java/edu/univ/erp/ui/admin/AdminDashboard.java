package edu.univ.erp.ui.admin;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.ui.common.ChangePasswordDialog;
import edu.univ.erp.ui.common.NotificationPanel;
import edu.univ.erp.ui.login.LoginWindow;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.TitledBorder;
import java.awt.*;
public class AdminDashboard extends JFrame {
    private final User currentUser;
    private JCheckBox maintenanceModeToggle;
    private JLabel statusLabel;
    private final AdminService adminService;
    private final NotificationPanel notificationPanel;
    private JTextArea notificationMessageField;

    private static final Color NAVY_BLUE = new Color(0, 0, 128);
    public AdminDashboard(User user) {
        this.currentUser = user;
        this.adminService = new AdminService();
        this.notificationPanel = new NotificationPanel();
        setTitle("Admin Dashboard - Logged in as: " + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel notificationSidebar = createNotificationControlPanel();
        add(notificationSidebar, BorderLayout.EAST);

        JPanel navPanel = createNavPanel();
        add(navPanel, BorderLayout.CENTER);
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        loadMaintenanceStatus();
        notificationPanel.loadNotification();
        notificationMessageField.setText(notificationPanel.isVisible() ? notificationPanel.messageArea.getText() : "");
    }
//Load maintenance mode status into UI
    private void loadMaintenanceStatus() {
        boolean isMaintenance = SessionManager.getInstance().isMaintenanceModeOn();
        if (maintenanceModeToggle != null && statusLabel != null) {
            maintenanceModeToggle.setSelected(isMaintenance);
            statusLabel.setText("System Status: " + (isMaintenance ? "MAINTENANCE MODE ACTIVE" : "Normal Operation"));
        }
    }
//Creates top header bar with welcome, logout, change password
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel banner = new JLabel("ERP System Control Panel", SwingConstants.CENTER);
        banner.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(banner, BorderLayout.WEST);
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(e -> new ChangePasswordDialog(this, currentUser).setVisible(true));

        rightPanel.add(welcomeLabel);
        rightPanel.add(changePasswordButton);
        rightPanel.add(logoutButton);

        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }
// Creates right sidebar panel for publishing/clearing notifications
    private JPanel createNotificationControlPanel() {
        //Explicitly set preferred width and use a flexible MigLayout for filling the height
        JPanel panel = new JPanel(new MigLayout("insets 5 10 5 10, fillx, wrap 1", "[grow, fill]", "[]5[]5[]push[]"));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(NAVY_BLUE, 2),
                "Notification Panel"
        );
        titledBorder.setTitleColor(NAVY_BLUE);
        panel.setBorder(titledBorder);
        panel.setPreferredSize(new Dimension(250, 600));
        notificationMessageField = new JTextArea(3, 1);
        notificationMessageField.setLineWrap(true);
        notificationMessageField.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(notificationMessageField);
        panel.add(new JLabel("Message:"));
        panel.add(scrollPane, "h 100:100:150");


        JButton publishButton = new JButton("PUBLISH");
        publishButton.addActionListener(e -> publishNotification(notificationMessageField.getText().trim()));

        JButton clearButton = new JButton("CLEAR");
        clearButton.addActionListener(e -> clearNotification(notificationMessageField));
        JPanel controlButtons = new JPanel(new GridLayout(2, 1, 5, 5));
        controlButtons.add(publishButton);
        controlButtons.add(clearButton);
        panel.add(controlButtons);

        panel.add(new JLabel("Live Notifications:"));
        panel.add(notificationPanel, "grow, pushy");

        return panel;
    }
    //Handles publishing new announcements.
    private void publishNotification(String message) {
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please use the 'CLEAR' button to remove the message.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean success = adminService.publishNotification(message, currentUser.getUserid());
        if (success) {
            notificationPanel.loadNotification();
            JOptionPane.showMessageDialog(this, "Announcement published successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to publish announcement.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
//Handles clearing the panel.
    private void clearNotification(JTextArea messageField) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm: Clear the announcements?",
                "Confirm Clear", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = adminService.clearNotification();
            if (success) {
                notificationPanel.loadNotification();
                messageField.setText("");
                JOptionPane.showMessageDialog(this, "Announcement cleared.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to clear announcement.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
// Center Navigation Menu (User mgmt, Course mgmt, Upload timetable, history)
    private JPanel createNavPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 20, 20));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(NAVY_BLUE, 2),
                "Navigation"
        );
        titledBorder.setTitleColor(NAVY_BLUE);
        panel.setBorder(titledBorder);

        JButton manageUsersButton = new JButton("Manage Users (Add Students/Instructors)");
        manageUsersButton.addActionListener(e -> new AddUserWindow(this).setVisible(true));

        JButton manageCoursesButton = new JButton("Manage Courses and Sections (Create/Edit/Deadlines)");
        manageCoursesButton.addActionListener(e -> new ManageCoursesWindow(this).setVisible(true));

        JButton uploadTimeTableButton = new JButton("Upload Time Table (PDF/Image)");
        uploadTimeTableButton.addActionListener(e -> uploadTimeTable());

        JButton viewHistoryButton = new JButton("View Password History");
        viewHistoryButton.addActionListener(e -> new AdminPasswordHistoryWindow(this).setVisible(true));

        Dimension maxButtonSize = new Dimension(450, 40);

        manageUsersButton.setMaximumSize(maxButtonSize);
        manageCoursesButton.setMaximumSize(maxButtonSize);
        uploadTimeTableButton.setMaximumSize(maxButtonSize);
        viewHistoryButton.setMaximumSize(maxButtonSize);

        panel.add(manageUsersButton);
        panel.add(manageCoursesButton);
        panel.add(uploadTimeTableButton);
        panel.add(viewHistoryButton);

        return panel;
    }
// Bottom panel for Maintenance toggle + Backup + Restore
    private JPanel createControlPanel() {
        this.maintenanceModeToggle = new JCheckBox("Maintenance Mode ON");
        this.statusLabel = new JLabel("System Status: Unknown", SwingConstants.CENTER);

        JPanel panel = new JPanel(new MigLayout("insets 10", "[grow, fill][grow, fill]", "[]10[]"));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(NAVY_BLUE, 2),
                "System & Data Control"
        );
        titledBorder.setTitleColor(NAVY_BLUE);
        panel.setBorder(titledBorder);
        JPanel maintenancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maintenanceModeToggle.addActionListener(e -> toggleMaintenanceMode(maintenanceModeToggle.isSelected()));
        maintenancePanel.add(maintenanceModeToggle);
        maintenancePanel.add(statusLabel);
        panel.add(maintenancePanel, "span 2, wrap");
        JButton backupButton = new JButton("BACKUP DB");
        backupButton.addActionListener(e -> backupDatabase());
        panel.add(backupButton, "growx");
        JButton restoreButton = new JButton("RESTORE LAST BACKUP");
        restoreButton.addActionListener(e -> restoreDatabase());
        panel.add(restoreButton, "growx, wrap");

        return panel;
    }
//Handles uploading time table
    private void uploadTimeTable() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Time Table File (PDF or Image)");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Documents and Images", "pdf", "png", "jpg", "jpeg");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try {
                byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
                boolean success = adminService.uploadTimeTable(file.getName(), fileBytes);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Time Table uploaded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to upload file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
//Backup of database
    private void backupDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm: Create a backup of all now?",
                "Confirm Full Database Backup", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = adminService.backupDatabaseState();

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "FULL Database Backup Complete.",
                        "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Backup failed. Check if backup tables are created .", "Backup Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
//restores the backed up database
    private void restoreDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This will overwrite ALL LIVE Course and Section data with the last saved backup. Continue?",
                "Confirm FULL Restore Operation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = adminService.restoreDatabaseState();

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Data reverted to backup state.",
                        "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Restore failed. Data integrity or missing backup detected.", "Restore Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
//Handles maintenance mode
    private void toggleMaintenanceMode(boolean isSelected) {
        try {
            adminService.toggleMaintenanceMode(isSelected);
            String status = isSelected ? "MAINTENANCE ACTIVE" : "Normal Operation";
            statusLabel.setText("System Status: " + status);
            JOptionPane.showMessageDialog(this,
                    "System Maintenance Mode is now " + (isSelected ? "ON" : "OFF"),
                    "System Status Change",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to toggle maintenance mode: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            maintenanceModeToggle.setSelected(!isSelected);
        }
    }
    private void logout() {
        SessionManager.getInstance().logout();
        new LoginWindow().setVisible(true);
        this.dispose();
    }
}