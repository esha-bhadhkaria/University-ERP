package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AdminService.CreateUserResult;
import javax.swing.*;
import java.awt.*;
public class AddUserWindow extends JDialog {

    private final JComboBox<User.UserRole> roleComboBox;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    private final JTextField profileField1; // Roll No (Student) or Department (Instructor)
    private final JTextField profileField2; // Program (Student) or Title (Instructor)
    private final JTextField profileField3; // Year (Student)

    private final JLabel profileLabel1;
    private final JLabel profileLabel2;
    private final JLabel profileLabel3;

    private final JPanel formPanel;
    private final AdminService adminService;

    public AddUserWindow(Frame owner) {
        super(owner, "Add New User", true);
        this.adminService = new AdminService();

        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //User Role selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        roleComboBox = new JComboBox<>(new User.UserRole[]{User.UserRole.STUDENT, User.UserRole.INSTRUCTOR});
        roleComboBox.addActionListener(e -> updateProfileFields());
        formPanel.add(roleComboBox, gbc);

        //Username
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);

        //Password
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);

        //Profile Field 1
        gbc.gridx = 0; gbc.gridy = 3;
        profileLabel1 = new JLabel();
        formPanel.add(profileLabel1, gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        profileField1 = new JTextField(20);
        formPanel.add(profileField1, gbc);

        //Profile Field 2
        gbc.gridx = 0; gbc.gridy = 4;
        profileLabel2 = new JLabel();
        formPanel.add(profileLabel2, gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        profileField2 = new JTextField(20);
        formPanel.add(profileField2, gbc);

        //Profile Field 3
        gbc.gridx = 0; gbc.gridy = 5;
        profileLabel3 = new JLabel();
        formPanel.add(profileLabel3, gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        profileField3 = new JTextField(20);
        formPanel.add(profileField3, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Create User");
        saveButton.addActionListener(e -> saveUser());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        updateProfileFields();
    }

    private void updateProfileFields() {
        User.UserRole selectedRole = (User.UserRole) roleComboBox.getSelectedItem();

        if (selectedRole == User.UserRole.STUDENT) {
            profileLabel1.setText("Roll No:");
            profileLabel2.setText("Program:");
            profileLabel3.setText("Year:");
            profileField1.setVisible(true);
            profileField2.setVisible(true);
            profileField3.setVisible(true);
            profileLabel1.setVisible(true);
            profileLabel2.setVisible(true);
            profileLabel3.setVisible(true);
        } else if (selectedRole == User.UserRole.INSTRUCTOR) {
            profileLabel1.setText("Department:");
            profileLabel2.setText("Title:");
            profileLabel3.setText(""); // Hide
            profileField1.setVisible(true);
            profileField2.setVisible(true);
            profileField3.setVisible(false); // Hide
            profileLabel1.setVisible(true);
            profileLabel2.setVisible(true);
            profileLabel3.setVisible(false); // Hide
        }
        pack();
    }

    private void saveUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        User.UserRole role = (User.UserRole) roleComboBox.getSelectedItem();
        CreateUserResult result;
        if (role == User.UserRole.STUDENT) {
            String rollNo = profileField1.getText().trim();
            String program = profileField2.getText().trim();
            String yearStr = profileField3.getText().trim();
            try {
                int year = Integer.parseInt(yearStr);
                result = adminService.createStudent(username, password, rollNo, program, year);
            } catch (NumberFormatException e) {
                result = new CreateUserResult(false, "Year must be a valid number.");
            }
        } else if (role == User.UserRole.INSTRUCTOR) {
            String department = profileField1.getText().trim();
            String title = profileField2.getText().trim();
            result = adminService.createInstructor(username, password, department, title);

        } else {
            result = new CreateUserResult(false, "Invalid role selected.");
        }

        if (result.success) {
            JOptionPane.showMessageDialog(this, result.message, "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.message, "Creation Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}