package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*; // Import java.awt.Dialog here
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

//Dialog for Admin to edit an existing Section's details (Time, Room, Capacity, etc.)
public class EditSectionDialog extends JDialog {

    private final Section sectionToEdit;
    private final AdminService adminService;

    private final JComboBox<Course> courseComboBox;
    private final JComboBox<String> dayComboBox;
    private final JComboBox<Instructor> instructorComboBox;
    private final JTextField startTimeField;
    private final JTextField endTimeField;
    private final JTextField roomField;
    private final JTextField capacityField;
    private final JTextField semesterField;
    private final JTextField yearField;

    public EditSectionDialog(Dialog owner, Section section) {
        super(owner, "Edit Section: " + section.getCourseCode(), true);
        this.sectionToEdit = section;
        this.adminService = new AdminService();

        setSize(500, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        List<Course> allCourses = adminService.courseDao.getAllCourses();
        List<Instructor> allInstructors = adminService.getAllInstructors();

        Course currentCourse = allCourses.stream()
                .filter(c -> c.getCourseid() == section.getCourseid())
                .findFirst().orElse(null);

        Instructor currentInstructor = allInstructors.stream()
                .filter(i -> i.getUserid() == section.getInstructorid())
                .findFirst().orElse(null);


        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel headerLabel = new JLabel("Editing Section ID: " + section.getSectionid(), SwingConstants.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(headerLabel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        courseComboBox = new JComboBox<>(allCourses.toArray(new Course[0]));
        if (currentCourse != null) courseComboBox.setSelectedItem(currentCourse);
        courseComboBox.setEnabled(false);
        formPanel.add(courseComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        instructorComboBox = new JComboBox<>(allInstructors.toArray(new Instructor[0]));
        if (currentInstructor != null) instructorComboBox.setSelectedItem(currentInstructor);
        formPanel.add(instructorComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Day of Week:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        String[] days = {"1 (Mon)", "2 (Tue)", "3 (Wed)", "4 (Thu)", "5 (Fri)", "6 (Sat)", "7 (Sun)"};
        dayComboBox = new JComboBox<>(days);
        dayComboBox.setSelectedIndex(section.getDayofweek() - 1);
        formPanel.add(dayComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Start Time (HH:MM:SS):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.anchor = GridBagConstraints.WEST;
        startTimeField = new JTextField(section.getStarttime().toString(), 10);
        formPanel.add(startTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("End Time (HH:MM:SS):"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.anchor = GridBagConstraints.WEST;
        endTimeField = new JTextField(section.getEndtime().toString(), 10);
        formPanel.add(endTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.anchor = GridBagConstraints.WEST;
        roomField = new JTextField(section.getRoom(), 10);
        formPanel.add(roomField, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; gbc.gridy = 7; gbc.anchor = GridBagConstraints.WEST;
        capacityField = new JTextField(String.valueOf(section.getCapacity()), 5);
        formPanel.add(capacityField, gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Semester/Year:"), gbc);
        gbc.gridx = 1; gbc.gridy = 8; gbc.anchor = GridBagConstraints.WEST;
        semesterField = new JTextField(String.valueOf(section.getSemester()), 5);
        yearField = new JTextField(String.valueOf(section.getYear()), 5);
        semesterField.setEnabled(false);
        yearField.setEnabled(false);
        JPanel termPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        termPanel.add(semesterField);
        termPanel.add(new JLabel("/"));
        termPanel.add(yearField);
        formPanel.add(termPanel, gbc);

        add(formPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Section Changes");
        saveButton.addActionListener(e -> saveSection());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
    }

    private void saveSection() {
        String dayStr = (String) dayComboBox.getSelectedItem();
        int newDayOfWeek = Integer.parseInt(dayStr.substring(0, 1));

        String newRoom = roomField.getText().trim();
        String capacityStr = capacityField.getText().trim();
        int newInstructorId = ((Instructor) instructorComboBox.getSelectedItem()).getUserid();

        if (newRoom.isEmpty() || capacityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room and Capacity are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int newCapacity = Integer.parseInt(capacityStr);
            if (newCapacity < 0) {
                JOptionPane.showMessageDialog(this, "Capacity cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalTime newStartTime = LocalTime.parse(startTimeField.getText().trim());
            LocalTime newEndTime = LocalTime.parse(endTimeField.getText().trim());

            if (newStartTime.isAfter(newEndTime) || newStartTime.equals(newEndTime)) {
                JOptionPane.showMessageDialog(this, "Start time must be before end time.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            sectionToEdit.setDayofweek(newDayOfWeek);
            sectionToEdit.setStarttime(newStartTime);
            sectionToEdit.setEndtime(newEndTime);
            sectionToEdit.setRoom(newRoom);
            sectionToEdit.setCapacity(newCapacity);
            adminService.assignInstructor(sectionToEdit.getSectionid(), newInstructorId);
            boolean success = adminService.editSection(sectionToEdit);

            if (success) {
                JOptionPane.showMessageDialog(this, "Section details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update section.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException | DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Capacity, Time, Semester and Year must be valid formats.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}