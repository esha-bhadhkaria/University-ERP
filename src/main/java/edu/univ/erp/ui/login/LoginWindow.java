package edu.univ.erp.ui.login;

import edu.univ.erp.service.AuthenticationService;
import edu.univ.erp.service.AuthenticationService.LoginResult;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;
import net.miginfocom.swing.MigLayout;
import edu.univ.erp.ErpApplication;
import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private final AuthenticationService authService;

    private static final Color ACCENT_LIGHT_BLUE = new Color(120, 160, 190);
    private static final Color NAVY_BLUE = new Color(13, 8, 64);
    private static final Color TEXT_COLOR = new Color(51, 51, 51);
    private static final Color BORDER_GRAY = new Color(180, 180, 180);
    private static final Color LABEL_COLOR = new Color(100, 100, 100);
    private static final Color SUB_TEXT_COLOR_DARK = new Color(200, 200, 200);


    public LoginWindow() {
        setTitle("University ERP - Welcome");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        Image icon = ErpApplication.getAppIcon();
        if (icon != null) {
            setIconImage(icon);
        }

        this.authService = new AuthenticationService();
        setLayout(new MigLayout("fill, insets 0, gap 0", "[50%][50%]", "[grow]"));
        JPanel leftPanel = createLeftPanel();
        add(leftPanel, "grow, height 100%");
        JPanel rightPanel = createRightPanel();
        add(rightPanel, "grow, height 100%");
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 40 40 40 40, fill", "[center]", "[]20[]push[]20[]20[]push[]"));
        panel.setBackground(NAVY_BLUE);
        panel.setBorder(null);
        JLabel logoLabel = new JLabel();
        logoLabel.setText("IIIT");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        panel.add(logoLabel, "align left, w 100!");
        JLabel illustration = new JLabel("<html><div style='text-align: center; color: white; font-size: 100px;'>🎓</div></html>");
        panel.add(illustration, "center");
        JLabel welcomeTitle = new JLabel("UNIVERSITY ERP");
        welcomeTitle.setForeground(Color.WHITE);
        welcomeTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        panel.add(welcomeTitle, "center");
        JLabel subText = new JLabel("<html><div style='text-align: center; color: " + String.format("#%06x", SUB_TEXT_COLOR_DARK.getRGB() & 0xFFFFFF) + ";'>Please sign in using your credentials.<br></div></html>");
        subText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(subText, "center");
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 60 80 60 80, fillx", "[fill]", "[]30[]15[]15[]30[]push"));
        panel.setBackground(Color.WHITE);
        JLabel title = new JLabel("SIGN-IN");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title, "center, gapbottom 30");

        usernameField = new JTextField();
        styleTextField(usernameField, "Username");
        panel.add(usernameField, "h 45!");

        passwordField = new JPasswordField();
        styleTextField(passwordField, "Password");
        panel.add(passwordField, "h 45!");

        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setBackground(NAVY_BLUE);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.putClientProperty("JButton.buttonType", "roundRect");
        loginButton.addActionListener(e -> attemptLogin());

        panel.add(loginButton, "h 45!, gaptop 15");

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(Color.WHITE);
        JLabel copyright = new JLabel("©University ERP System");
        copyright.setForeground(LABEL_COLOR);
        footer.add(copyright);
        panel.add(footer, "center, gaptop 20");

        return panel;
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JComponent.roundRect", true);
        field.putClientProperty("JComponent.outline", "gray");
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        LoginResult result = authService.login(username, password);
        if (result.success) {
            User authenticatedUser = SessionManager.getInstance().getCurrentUser();
            openDashboard(authenticatedUser);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.message, "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
    private void openDashboard(User user) {
        switch (user.getRole()) {
            case ADMIN: new AdminDashboard(user).setVisible(true); break;
            case INSTRUCTOR: new InstructorDashboard(user).setVisible(true); break;
            case STUDENT: new StudentDashboard(user).setVisible(true); break;
            default: SessionManager.getInstance().logout(); break;
        }
    }
}