package edu.univ.erp.ui.common;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AuthenticationService;
import edu.univ.erp.service.AuthenticationService.ChangePasswordResult;

import javax.swing.*;
import java.awt.*;

//Common dialog for all user roles (Student, Instructor, Admin) to change their password.
//Implements security checks like old password verification and history check.
public class ChangePasswordDialog extends JDialog {
    private final User currentUser;
    private final AuthenticationService authService;
    private final JPasswordField oldPasswordField;
    private final JPasswordField newPasswordField;
    private final JPasswordField confirmPasswordField;
    public ChangePasswordDialog(Frame owner, User user) {
        super(owner, "Change Password", true);
        this.currentUser = user;
        this.authService = new AuthenticationService();
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Change Password for: " + user.getUsername(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        oldPasswordField = new JPasswordField(15);
        formPanel.add(oldPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("New Password (min 6 chars):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        newPasswordField = new JPasswordField(15);
        formPanel.add(newPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Confirm New Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        confirmPasswordField = new JPasswordField(15);
        formPanel.add(confirmPasswordField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> attemptChangePassword());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
    }

    private void attemptChangePassword() {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        ChangePasswordResult result = authService.changePassword(
                oldPassword, newPassword, confirmPassword);
        if (result.success) {
            JOptionPane.showMessageDialog(this,
                    "Password successfully updated.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            SessionManager.getInstance().logout();

            if (getOwner() instanceof JFrame) {
                JFrame ownerFrame = (JFrame) getOwner();
                ownerFrame.dispose();
                new edu.univ.erp.ui.login.LoginWindow().setVisible(true);
            }
            this.dispose();

        } else {
            JOptionPane.showMessageDialog(this, result.message, "Password Change Failed", JOptionPane.ERROR_MESSAGE);
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        }
    }
}