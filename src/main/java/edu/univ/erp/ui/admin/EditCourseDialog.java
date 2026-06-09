package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Course;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;

//Dialog for Admin to edit an existing Course's details.
public class EditCourseDialog extends JDialog {
    private final Course courseToEdit;
    private final AdminService adminService;
    private final JTextField codeField;
    private final JTextField titleField;
    private final JTextField creditsField;

    public EditCourseDialog(Dialog owner, Course course) {
        super(owner, "Edit Course: " + course.getCode(), true);
        this.courseToEdit = course;
        this.adminService = new AdminService();

        setSize(400, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel headerLabel = new JLabel("Edit Course Details");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(headerLabel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        codeField = new JTextField(course.getCode(), 15);
        formPanel.add(codeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Course Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        titleField = new JTextField(course.getTitle(), 15);
        formPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        creditsField = new JTextField(String.valueOf(course.getCredits()), 5);
        formPanel.add(creditsField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveCourse());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
    }

    private void saveCourse() {
        String newCode = codeField.getText().trim();
        String newTitle = titleField.getText().trim();
        String creditsStr = creditsField.getText().trim();

        if (newCode.isEmpty() || newTitle.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Code and Title are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int newCredits = Integer.parseInt(creditsStr);
            if (newCredits <= 0) {
                JOptionPane.showMessageDialog(this, "Credits must be positive.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            courseToEdit.setCode(newCode);
            courseToEdit.setTitle(newTitle);
            courseToEdit.setCredits(newCredits);

            boolean success = adminService.editCourse(courseToEdit);

            if (success) {
                JOptionPane.showMessageDialog(this, "Course updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update course.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Credits must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}