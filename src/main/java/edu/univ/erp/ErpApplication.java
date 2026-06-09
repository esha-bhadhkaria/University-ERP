package edu.univ.erp;

import com.formdev.flatlaf.FlatIntelliJLaf;
import edu.univ.erp.ui.login.LoginWindow;
import edu.univ.erp.util.DbConnectionManager;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class ErpApplication {
    public static Image getAppIcon() {
        try {
            URL iconURL = ErpApplication.class.getResource("/icon/icon.jpg");
            if (iconURL != null) {
                return new ImageIcon(iconURL).getImage();
            } else {
                System.err.println("Icon resource not found ");
            }
        } catch (Exception e) {
            System.err.println("Error loading application icon: " + e.getMessage());
        }
        return null;
    }
    public static void main(String[] args) {
        // Initialize Database Connections
        DbConnectionManager.initialize();

        try {
            Color ACCENT_MEDIUM_BLUE = new Color(120, 160, 190);
            Color NAVY_BLUE = new Color(11, 0, 56);
            Color BORDER_GRAY = new Color(180, 180, 180);
            Color BLACK_BORDER = Color.BLACK;
            System.setProperty("flatlaf.uiScale", "1.0");
            FlatIntelliJLaf.setup();
            UIManager.put("Component.accentColor", ACCENT_MEDIUM_BLUE);
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.borderColor", BORDER_GRAY);
            Font defaultButtonFont = UIManager.getFont("Button.font");
            if (defaultButtonFont != null) {
                UIManager.put("Button.font", defaultButtonFont.deriveFont(defaultButtonFont.getSize() + 2f));
            } else {
                UIManager.put("Button.font", new Font("Lato", Font.PLAIN, 14));
            }

            UIManager.put("Button.borderColor", BLACK_BORDER);
            UIManager.put("Button.borderWidth", 2);
            UIManager.put("Button.default.borderWidth", 2);
            UIManager.put("TitlePane.background", Color.WHITE);

        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf Look and Feel: " + ex);
        }
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
        Runtime.getRuntime().addShutdownHook(new Thread(DbConnectionManager::close));
    }
}